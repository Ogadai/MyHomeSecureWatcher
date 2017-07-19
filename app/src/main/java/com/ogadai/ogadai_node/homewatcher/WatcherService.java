package com.ogadai.ogadai_node.homewatcher;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.ogadai.ogadai_node.homewatcher.messages.Ping;
import com.ogadai.ogadai_node.homewatcher.messages.SetState;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by alee on 14/07/2017.
 */

public class WatcherService extends Service {
    private NotificationManager mNM;

    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private int NOTIFICATION = 798;

    private HomeSecureClient mClient;
    private Camera2 mCamera2;

    private PowerManager mPowerManager;
    private PowerManager.WakeLock mWakeLock;

    private ScheduledExecutorService mScheduler;
    private ScheduledFuture mWakeRefreshHandle;

    private static final String TAG = "WatcherService";

    public static final String BROADCAST_NODE_STATUS = "com.ogadai.ogadai_node.homewatcher.BROADCAST";
    // Defines the key for the status "extra" in an Intent
    public static final String EXTENDED_DATA_STATUS = "com.ogadai.ogadai_node.homewatcher.STATUS";

    private static final int WAKEREFRESHSECONDS = 10*60;

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        WatcherService getService() {
            return WatcherService.this;
        }
    }

    @Override
    public void onCreate() {
        close();
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        mScheduler = Executors.newScheduledThreadPool(1);

        mClient = new HomeSecureClient();
        final Context broadcastContext = this;
        mClient.setCallback(new HomeSecureClient.ClientCallback() {
            @Override
            public void updateState(final boolean connectionOpen) {
                /*
                 * Creates a new Intent containing a Uri object
                 * BROADCAST_ACTION is a custom Intent action
                 */
                Intent localIntent =
                        new Intent(BROADCAST_NODE_STATUS)
                                // Puts the status into the Intent
                                .putExtra(EXTENDED_DATA_STATUS, connectionOpen);
                // Broadcasts the Intent to receivers in this app.
                LocalBroadcastManager.getInstance(broadcastContext).sendBroadcast(localIntent);
            }
        });

        mCamera2 = new Camera2(this);
        mClient.setCameraControls(mCamera2);

        Configuration config = Configuration.readSettings(this);
        mClient.connect(config);

        mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);
        getWakeLock();
        refreshWakeLock();

        // Display a notification about us starting.  We put an icon in the status bar.
        showNotification(config.getName());

        // Tell the user we started.
        Toast.makeText(this, R.string.watcher_service_started, Toast.LENGTH_SHORT).show();
    }

    private void testSnapshot() {
        mCamera2.start();

        mScheduler.schedule(new Runnable() {
            @Override
            public void run() {
                mCamera2.takePicture(false, new Camera2.TakePictureCallback() {
                    @Override
                    public void result(Image image) {
                        Log.i(TAG, "Image received");
                        saveImage(image);
                        mCamera2.stop();
                    }
                });
            }
        }, 1000, TimeUnit.MILLISECONDS);
    }

    private void saveImage(Image image) {
        File file = new File(this.getExternalFilesDir(null), "pic.jpg");

        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(file);
            output.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            image.close();
            if (null != output) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            MediaStore.Images.Media.insertImage(getContentResolver(), file.getPath(), "pic.jpg", "snapshot");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Received start id " + startId + ": " + intent);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        mNM.cancel(NOTIFICATION);

        close();

        // Tell the user we stopped.
        Toast.makeText(this, R.string.watcher_service_stopped, Toast.LENGTH_SHORT).show();
    }

    private void close() {
        if (mClient != null) {
            mClient.disconnect();
            mClient = null;
        }

        if (mWakeRefreshHandle != null) {
            mWakeRefreshHandle.cancel(false);
            mWakeRefreshHandle = null;
        }

        if (mWakeLock != null) {
            mWakeLock.release();
            mWakeLock = null;
        }
    }

    private void getWakeLock() {
        mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "HomeSecureWatcherLock");
        mWakeLock.acquire();
    }


    private void refreshWakeLock() {
        mWakeRefreshHandle = mScheduler.schedule(new Runnable() {
            @Override
            public void run() {
                mWakeRefreshHandle = null;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (mWakeLock != null) {
                            mWakeLock.release();
                        }
                        getWakeLock();
                    }
                }).start();

                refreshWakeLock();
            }
        }, WAKEREFRESHSECONDS, TimeUnit.SECONDS);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();

    /**
     * Show a notification while this service is running.
     */
    private void showNotification(String name) {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.watcher_service_started) + " - " + name;

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        // Set the info for the views that show in the notification panel.
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_notification)  // the status icon
                .setTicker(text)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle(getText(R.string.watcher_service_label))  // the label of the entry
                .setContentText(text)  // the contents of the entry
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                .build();

        // Send the notification.
        mNM.notify(NOTIFICATION, notification);
    }
}