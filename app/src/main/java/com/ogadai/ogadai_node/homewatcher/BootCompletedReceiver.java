package com.ogadai.ogadai_node.homewatcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by alee on 27/07/2017.
 */

public class BootCompletedReceiver extends BroadcastReceiver {
    private static final String TAG = "BootCompletedReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Boot action completed");
        context.startService(new Intent(context, WatcherService.class));
        RunningCheckReceiver.setupAlarm(context);
    }
}
