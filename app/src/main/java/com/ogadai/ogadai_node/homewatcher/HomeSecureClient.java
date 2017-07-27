package com.ogadai.ogadai_node.homewatcher;

import android.util.Log;

import com.ogadai.ogadai_node.homewatcher.devices.CameraDevice;
import com.ogadai.ogadai_node.homewatcher.devices.Device;
import com.ogadai.ogadai_node.homewatcher.messages.Initialise;
import com.ogadai.ogadai_node.homewatcher.messages.Message;
import com.ogadai.ogadai_node.homewatcher.messages.Ping;
import com.ogadai.ogadai_node.homewatcher.messages.Sensor;
import com.ogadai.ogadai_node.homewatcher.messages.SetState;
import com.ogadai.ogadai_node.homewatcher.messages.Settings;
import com.ogadai.ogadai_node.homewatcher.utility.HttpClient;

import java.net.URI;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by alee on 30/06/2017.
 */

public class HomeSecureClient implements WebsocketClientEndpoint.MessageHandler {
    private WebsocketClientEndpoint mClient;
    private Configuration mConfig;
    private Settings.SettingsData mSettings;

    private HashMap<String, Device> mDevices;
    private CameraDevice mCameraDevice;

    private ClientCallback mCallback;

    private ScheduledExecutorService mScheduler;
    private ScheduledFuture mTimerHandle;
    private ScheduledFuture mPingHandle;

    private static final String TAG = "HomeSecureClient";
    private static final int RETRYDELAYSECONDS = 10;
    private static final int PINGSECONDS = 10;

    public HomeSecureClient() {
        mClient = new WebsocketClientEndpoint();
        mClient.addMessageHandler(this);

        mScheduler = Executors.newScheduledThreadPool(1);

        mDevices = new HashMap<>();
        mCameraDevice = new CameraDevice();
        addDevice("camera", mCameraDevice);
    }

    public void setCallback(ClientCallback callback) {
        mCallback = callback;
    }
    public void setCameraControls(CameraControls controls) {
        mCameraDevice.setCameraControls(controls);
    }

    public void connect(Configuration config) {
        mConfig = config;
        new Thread(new Runnable() {
            @Override
            public void run() {
                connectSocket();
            }
        }).start();
    }

    public void disconnect() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                disconnectSocket();
            }
        }).start();
    }

    public void reconnect() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                disconnectSocket();
                connectSocket();
            }
        }).start();
    }

    public void send(Message message) {
        String strMessage = message.toJSON();
        Log.d(TAG, "send: " + strMessage);
        mClient.sendMessage(strMessage);
    }

    public void postToServer(String relativePath, byte[] data) {
        if (mSettings != null) {
            String wsUrl = mSettings.getAddr();
            String httpUrl = wsUrl.replaceFirst("ws", "http").replaceFirst("localhost", "10.0.2.2");
            String fullQuery = httpUrl
                        + (httpUrl.endsWith("/") ? "" : "/")
                        + relativePath
                        + "?hub=" + mSettings.getIdentification().getName()
                        + "&token=" + mSettings.getIdentification().getToken()
                        + "&node=" + mConfig.getName();

            Log.i(TAG, "Posting image to " + fullQuery);
            HttpClient.post(fullQuery, data);
        }
    }

    private void connectSocket() {
        try {
            Log.i(TAG, "connecting");
            mClient.connect(new URI(mConfig.getAddress()));
        } catch (Exception e) {
            Log.e(TAG, "Error connecting websocket", e);
            reconnectAfterDelay();
        }
    }

    private void disconnectSocket() {
        Log.i(TAG, "disconnecting");
        if (mTimerHandle != null) {
            mTimerHandle.cancel(false);
            mTimerHandle = null;
        }

        mClient.disconnect();

        if (mCallback != null) {
            mCallback.updateState(false);
        }
    }

    private void reconnectAfterDelay() {
        Log.i(TAG, "retry in " + RETRYDELAYSECONDS + " seconds");
        mTimerHandle = mScheduler.schedule(new Runnable() {
            @Override
            public void run() {
                mTimerHandle = null;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        connectSocket();
                    }
                }).start();
            }
        }, RETRYDELAYSECONDS, TimeUnit.SECONDS);

    }

    @Override
    public void handleMessage(String message) {
        Log.d(TAG, "receive: " + message);
        Message decoded = Message.fromJSON(message);

        if (decoded != null) {
            if (decoded.getClass() == Settings.class) {
                handleMessage((Settings)decoded);
            } else if (decoded.getClass() == SetState.class) {
                handleMessage((SetState)decoded);
            }
        }
    }

    private void handleMessage(Settings settings) {
        mSettings = settings.getSettings();
    }

    private void handleMessage(SetState setState) {
        if (mDevices.containsKey(setState.getName())) {
            Device target = mDevices.get(setState.getName());
            target.setState(setState.getState());
        } else {
            Log.i(TAG, "Unknown device - " + setState.getName());
        }
    }

    private void addDevice(final String name, Device device) {
        device.setHandler(new Device.Emitter() {
            @Override
            public void changed(String state) {
                send(new Sensor(name, state));
            }

            @Override
            public void post(String relativePath, byte[] data) {
                postToServer(relativePath, data);
            }
        });
        mDevices.put(name, device);
    }

    @Override
    public void handleOpen() {
        Log.i(TAG, "websocket opened");

        if (mCallback != null) {
            mCallback.updateState(true);
        }

        send(new Initialise(mConfig.getName()));
        schedulePing();
    }

    @Override
    public void handleClose(boolean error) {
        Log.i(TAG, "websocket closed");

        if (mPingHandle != null) {
            mPingHandle.cancel(false);
        }

        if (mCallback != null) {
            mCallback.updateState(false);
        }

        reconnectAfterDelay();
    }

    private void schedulePing() {
        mPingHandle = mScheduler.schedule(new Runnable() {
            @Override
            public void run() {
                mPingHandle = null;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        send(new Ping());
                    }
                }).start();

                schedulePing();
            }
        }, PINGSECONDS, TimeUnit.SECONDS);
    }

    public interface ClientCallback {
        void updateState(boolean connectionOpen);
    }
}
