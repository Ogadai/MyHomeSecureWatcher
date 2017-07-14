package com.ogadai.ogadai_node.homewatcher.devices;

import android.media.Image;
import android.util.Log;

import com.ogadai.ogadai_node.homewatcher.Camera2BasicFragment;
import com.ogadai.ogadai_node.homewatcher.CameraControls;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by alee on 03/07/2017.
 */

public class CameraDevice extends DeviceBase implements Camera2BasicFragment.TakePictureCallback {
    private HashMap<String, String> mStates;

    private boolean mTimelapseOn;
    private boolean mVideoOn;
    private boolean mMotionOn;

    private static final String TAG = "CameraDevice";

    private static final String TIMELAPSE = "timelapse";
    private static final String H264 = "h264";
    private static final String MOTION = "motion";
    private static final String OFF = "off";

    private CameraControls mCameraControls;

    private ScheduledExecutorService mScheduler;
    private ScheduledFuture mStopTimer;
    private ScheduledFuture mSnapshotTimer;

    public CameraDevice() {
        super();
        mStates = new HashMap<>();
        mScheduler = Executors.newScheduledThreadPool(1);
    }

    public void setCameraControls(CameraControls controls) {
        mCameraControls = controls;
    }

    @Override
    public void setState(String state) {
        int index = state.indexOf('.');
        if (index != -1) {
            String name = state.substring(0, index);
            String value = state.substring(index + 1);

            mStates.put(name, value);
        } else if (state.equalsIgnoreCase("night")) {
        } else if (state.equalsIgnoreCase("day")) {
        } else {
            mStates.put("_default", state);
        }

        String onValue = OFF;
        for (String value: mStates.values()) {
            if (onValue.equalsIgnoreCase(OFF) || value.equalsIgnoreCase(TIMELAPSE)) {
                onValue = value;
            }
        }

        if (onValue.equalsIgnoreCase(TIMELAPSE)) {
            stopVideo();
            stopMotion();
            startTimelapse();
        } else if (onValue.equalsIgnoreCase(H264)) {
            stopTimelapse();
            stopMotion();
            startVideo();
        } else if (onValue.equalsIgnoreCase(MOTION)) {
            stopTimelapse();
            stopVideo();
            startMotion();
        } else {
            stopTimelapse();
            stopVideo();
            stopMotion();
        }
    }

    private void startTimelapse() {
        if (!mTimelapseOn) {
            mTimelapseOn = true;
            Log.i(TAG, "timelapse on");
            emit("timelapse");

            start();
        }
    }
    private void stopTimelapse() {
        if (mTimelapseOn) {
            mTimelapseOn = false;
            Log.i(TAG, "timelapse off");
            emit("off");

            stop();
        }
    }

    private void imageTimeLapse(Image image) {
        Log.i(TAG, "Image for timelapse");

        // Get the image bytes
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);

        post("UploadSnapshot", bytes);
    }

    private void startVideo() {
        if (!mVideoOn) {
            mVideoOn = true;
            Log.i(TAG, "video on");
            emit("h264");
        }
    }
    private void stopVideo() {
        if (mVideoOn) {
            mVideoOn = false;
            Log.i(TAG, "video off");
            emit("off");
        }
    }

    private void startMotion() {
        if (!mMotionOn) {
            mMotionOn = true;
            Log.i(TAG, "motion detection on");

            start();
        }
    }
    private void stopMotion() {
        if (mMotionOn) {
            mMotionOn = false;
            Log.i(TAG, "motion detection off");

            stop();
        }
    }

    private void imageMotion(Image image) {
        Log.i(TAG, "Image for motion detection");
    }

    private void start() {
        if (mStopTimer != null) {
            mStopTimer.cancel(false);
            mStopTimer = null;
        } else {
            mCameraControls.start();
        }

        snapshotAfterDelay();
    }

    private void stop() {
        if (mSnapshotTimer != null) {
            mSnapshotTimer.cancel(false);
            mSnapshotTimer = null;
        }

        mStopTimer = mScheduler.schedule(new Runnable() {
            @Override
            public void run() {
                mStopTimer = null;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCameraControls != null) {
                            mCameraControls.stop();
                        }
                    }
                }).start();
            }
        }, 1000, TimeUnit.MILLISECONDS);
    }


    private void snapshotAfterDelay() {
        final Camera2BasicFragment.TakePictureCallback callback = this;

        mSnapshotTimer = mScheduler.schedule(new Runnable() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (mSnapshotTimer != null && mCameraControls != null) {
                            mCameraControls.takePicture(callback);
                        }
                        mSnapshotTimer = null;
                    }
                }).start();
            }
        }, 1000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void result(final Image image) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mTimelapseOn) {
                    imageTimeLapse(image);
                } else if (mMotionOn) {
                    imageMotion(image);
                }

                if (mTimelapseOn || mMotionOn) {
                    snapshotAfterDelay();
                }
            }
        }).start();
    }
}