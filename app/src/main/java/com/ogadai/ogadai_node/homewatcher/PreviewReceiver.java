package com.ogadai.ogadai_node.homewatcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by alee on 06/10/2017.
 */

public class PreviewReceiver extends BroadcastReceiver {
    private PreviewReceiver.Callback mCallback;
    public PreviewReceiver(PreviewReceiver.Callback callback) {
        mCallback = callback;
    }

    // Called when the BroadcastReceiver gets an Intent it's registered to receive
    @Override
    public void onReceive(Context context, Intent intent) {
        int width = intent.getIntExtra(WatcherService.EXTENDED_DATA_IMAGEWIDTH, 0);
        int height = intent.getIntExtra(WatcherService.EXTENDED_DATA_IMAGEHEIGHT, 0);
        byte[] imageBytes = intent.getByteArrayExtra(WatcherService.EXTENDED_DATA_IMAGEBYTES);

        if (imageBytes != null) {
            mCallback.preview(width, height, imageBytes);
        }
    }

    public interface Callback {
        void preview(int width, int height, byte[] imageBytes);
    }
}
