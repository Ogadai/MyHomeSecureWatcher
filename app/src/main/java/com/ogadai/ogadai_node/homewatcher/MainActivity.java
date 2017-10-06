package com.ogadai.ogadai_node.homewatcher;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.os.IBinder;
import android.renderscript.ScriptGroup;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {
    private Configuration mConfig;
    private boolean mIsBound;

    private TextView mConnectionState;
    private ImageView mPreviewImage;

    private static final String TAG = "MainActivity";

    static final int REQUEST_CAMERA_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mConnectionState = (TextView)findViewById(R.id.connectionState);
        mPreviewImage = (ImageView)findViewById(R.id.previewImage);

        // Register for status updates
        IntentFilter statusIntentFilter = new IntentFilter(WatcherService.BROADCAST_NODE_STATUS);
        ConnectionStateReceiver mStateReceiver = new ConnectionStateReceiver(
                new ConnectionStateReceiver.Callback() {
                    @Override
                    public void updatedConnectionState(final boolean isConnected) {
                        runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mConnectionState.setText(isConnected ? "connected" : "not connected");
                                }
                            });
                    }
                });
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mStateReceiver, statusIntentFilter);

        // Register for preview images
        IntentFilter previewIntentFilter = new IntentFilter(WatcherService.BROADCAST_NODE_PREVIEW);
        PreviewReceiver mPreviewReceiver = new PreviewReceiver(
                new PreviewReceiver.Callback() {
                    @Override
                    public void preview(final int width, final int height, final byte[] imageBytes) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showPreview(width, height, imageBytes);
                            }
                        });
                    }
                });
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mPreviewReceiver, previewIntentFilter);

        requestPermissions(new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.WAKE_LOCK
        }, REQUEST_CAMERA_PERMISSION);
    }

    void doBindService() {
        if (!mIsBound) {
            // Establish a connection with the service.  We use an explicit
            // class name because we want a specific service implementation that
            // we know will be running in our own process (and thus won't be
            // supporting component replacement by other applications).
//            bindService(new Intent(this, WatcherService.class), mConnection, Context.BIND_AUTO_CREATE);
            startService(new Intent(this, WatcherService.class));
            mIsBound = true;
        }
    }

    void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
//            unbindService(mConnection);
            stopService(new Intent(this, WatcherService.class));
            mConfig = null;
            mIsBound = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Configuration config = Configuration.readSettings(this);
        if (mConfig != null &&
                !(mConfig.getName().equals(config.getName()) && mConfig.getAddress().equals(config.getAddress())) ) {
            doUnbindService();
            doBindService();
        }
        mConfig = config;

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(config.getName());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length != 3
                    || grantResults[0] != PackageManager.PERMISSION_GRANTED
                    || grantResults[1] != PackageManager.PERMISSION_GRANTED
                    || grantResults[2] != PackageManager.PERMISSION_GRANTED
                    ) {
                // Tell the user we stopped.
                Toast.makeText(this, R.string.no_camera_permission, Toast.LENGTH_SHORT).show();

                Log.e(TAG, "Wasn't granted camera permissions");
            } else {
                doBindService();
                RunningCheckReceiver.setupAlarm(this);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                showSettings();
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    private void showPreview(int width, int height, byte[] imageBytes) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(imageBytes));

        Log.i(TAG, "Showing preview image: (" + width + ", " + height + ")");
        mPreviewImage.setImageBitmap(bitmap);
    }

    private void showSettings() {
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivityForResult(settingsIntent, 0);
    }
}
