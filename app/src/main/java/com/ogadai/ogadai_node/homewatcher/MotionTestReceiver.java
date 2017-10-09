package com.ogadai.ogadai_node.homewatcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by alee on 09/10/2017.
 */

public class MotionTestReceiver extends BroadcastReceiver {
    private MotionTestReceiver.Callback mCallback;
    public MotionTestReceiver(MotionTestReceiver.Callback callback) {
        mCallback = callback;
    }

    // Called when the BroadcastReceiver gets an Intent it's registered to receive
    @Override
    public void onReceive(Context context, Intent intent) {
        boolean testOn = intent.getBooleanExtra(WatcherService.EXTENDED_DATA_TESTMOTIONON, false);
        mCallback.motionTest(testOn);
    }

    public interface Callback {
        void motionTest(boolean testOn);
    }
}
