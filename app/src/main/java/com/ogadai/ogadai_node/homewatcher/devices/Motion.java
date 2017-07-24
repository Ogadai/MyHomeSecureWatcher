package com.ogadai.ogadai_node.homewatcher.devices;

import android.content.Context;
import android.graphics.Point;
import android.util.Log;

import com.ogadai.ogadai_node.homewatcher.Configuration;

import java.nio.ByteBuffer;

/**
 * Created by alee on 24/07/2017.
 */

public class Motion {
    private ImageData mLastImage;
    private int mCurrentSequence;

    private Configuration mConfig;

    private static final String TAG = "Motion";

    private static Context mMotionContext;
    public static void setContext(Context context) {
        mMotionContext = context;
    }

    public Motion() {
        mLastImage = null;
        mCurrentSequence = 0;

        mConfig = Configuration.readSettings(mMotionContext);
    }

    public boolean checkImage(int width, int height, ByteBuffer byteBuffer) {
        ImageData lastImage = mLastImage;
        ImageData imageData = new ImageData(width, height, byteBuffer);
        mLastImage = imageData;

        Log.d(TAG, "Checking image (" + imageData.getWidth() + " x " + imageData.getHeight() + ") - " + imageData.getBytes().length + " bytes");

        if (lastImage != null) {
            if (compare(lastImage, imageData)) {
                mCurrentSequence++;
            } else {
                mCurrentSequence = 0;
            }

            if (mCurrentSequence >= mConfig.getSequence()) {
                mCurrentSequence = 0;
                return true;
            }
        }
        return false;
    }

    private boolean compare(ImageData file1, ImageData file2) {
        if ((file1.getWidth() != file2.getWidth())
            || (file1.getHeight() != file2.getHeight())) {
            return false;
        }

        int countedPixels = 0;
        int changedPixels = 0;

        for(int y = 0; y < file1.getHeight(); y++) {
            for(int x = 0; x < file1.getWidth(); x++) {
                countedPixels++;
                if (isChanged(new Point(x, y), file1, file2)) {
                    changedPixels++;
                }
            }
        }

        int percentChanged = changedPixels * 100 / countedPixels;
        Log.d(TAG, "Percent: " + percentChanged);

        return percentChanged >= mConfig.getMinPercent()
            && percentChanged <= mConfig.getMaxPercent();
    }

    private boolean isChanged(Point point, ImageData file1, ImageData file2) {
        RGB rgb1 = getRGB(file1, point);
        RGB rgb2 = getRGB(file2, point);

        if (rgb1 != null && rgb2 != null) {
            return isColourChanged(rgb1.r, rgb2.r)
                || isColourChanged(rgb1.g, rgb2.g)
                || isColourChanged(rgb1.b, rgb2.b);
        }
        return false;
    }

    private boolean isColourChanged(int colour1, int colour2) {
        return Math.abs(colour1 - colour2) > mConfig.getColourThreshold();
    }

    private RGB getRGB(ImageData file, Point point) {
        int pos = file.getWidth() * point.y + point.x;
        byte[] bytes = file.getBytes();
        if (pos <= bytes .length - 4) {
            return new RGB(bytes[pos], bytes[pos + 1], bytes[pos + 2]);
        }
        return null;
    }

    private class ImageData {
        private int mWidth;
        private int mHeight;
        private ByteBuffer mByteBuffer;

        public ImageData(int width, int height, ByteBuffer byteBuffer) {
           mWidth = width;
           mHeight = height;
            mByteBuffer = byteBuffer;
        }

        public int getWidth() {
            return mWidth;
        }

        public int getHeight() {
            return mHeight;
        }

        public byte[] getBytes() {
            return mByteBuffer.array();
        }
    }

    private class RGB {
        public int r;
        public int g;
        public int b;

        public RGB(int r, int g, int b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }
    }
}
