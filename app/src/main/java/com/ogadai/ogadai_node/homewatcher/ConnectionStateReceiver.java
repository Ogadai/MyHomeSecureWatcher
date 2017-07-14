package com.ogadai.ogadai_node.homewatcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by alee on 14/07/2017.
 */

public class ConnectionStateReceiver extends BroadcastReceiver {
    private Callback mCallback;
    public ConnectionStateReceiver(Callback callback) {
        mCallback = callback;
    }

    // Called when the BroadcastReceiver gets an Intent it's registered to receive
    @Override
    public void onReceive(Context context, Intent intent) {
        boolean connected = intent.getBooleanExtra(WatcherService.EXTENDED_DATA_STATUS, false);
        mCallback.updatedConnectionState(connected);
    }

    public interface Callback {
        void updatedConnectionState(boolean isConnected);
    }
}
