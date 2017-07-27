package com.ogadai.ogadai_node.homewatcher;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

/**
 * Created by alee on 27/07/2017.
 */

public class RunningCheckReceiver extends BroadcastReceiver {
    private static final String TAG = "RunningCheckReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Running check completed");
        context.startService(new Intent(context, WatcherService.class));
    }

    public static void setupAlarm(Context context) {
        cancelAlarm(context);

        Log.i(TAG, "Setup running check");
        int interval = 1000 * 60 * 30; // 30 minutes
        alarmManager(context).setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + interval, interval, getIntent(context));

    }

    public static void cancelAlarm(Context context) {
        Log.i(TAG, "Cancelled running check");
        alarmManager(context).cancel(getIntent(context));
    }

    private static AlarmManager alarmManager(Context context) {
        return (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
    }

    private static PendingIntent getIntent(Context context) {
        // Retrieve a PendingIntent that will perform a broadcast
        Intent alarmIntent = new Intent(context, RunningCheckReceiver.class);
        return PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
    }
}
