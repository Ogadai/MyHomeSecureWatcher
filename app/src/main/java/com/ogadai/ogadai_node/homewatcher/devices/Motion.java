package com.ogadai.ogadai_node.homewatcher.devices;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.Log;

import com.ogadai.ogadai_node.homewatcher.Configuration;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by alee on 24/07/2017.
 */

public class Motion {
    private ImageData mLastImage;
    private int mCurrentSequence;

    private Configuration mConfig;
    private int mColourThreshold;

    private ArrayList<Date> mDetectionTimes = new ArrayList<>();

    private static final String TAG = "Motion";

    private static Context mMotionContext;
    public static void setContext(Context context) {
        mMotionContext = context;
    }

    public Motion() {
        mLastImage = null;
        mCurrentSequence = 0;

        mConfig = Configuration.readSettings(mMotionContext);
        mColourThreshold = mConfig.getColourThreshold() * 255 / 100;
    }

    public MotionResult checkImage(int width, int height, ByteBuffer byteBuffer) {
        ImageData lastImage = mLastImage;
        ImageData imageData = getImageData(width, height, byteBuffer);
        mLastImage = imageData;

        Log.d(TAG, "Checking image (" + imageData.getWidth() + " x " + imageData.getHeight() + ") - " + imageData.getBytes().length + " bytes");

        if (lastImage != null) {
            MotionResult compareResult = compare(lastImage, imageData);
            if (compareResult.movementDetected) {
                mCurrentSequence++;
            } else {
                mCurrentSequence = 0;
            }

            if (mCurrentSequence >= mConfig.getSequence()) {
                mCurrentSequence = 0;
                compareResult.movementDetected = throttleMotionDetected();
            } else {
                compareResult.movementDetected = false;
            }
            return compareResult;
        }
        return new MotionResult(false);
    }

    private boolean throttleMotionDetected() {
        Date now = new Date();

        int lastHour = 0;
        int lastMinute = 0;

        int index = mDetectionTimes.size();
        while(index > 0) {
            index--;

            Date captureTime = mDetectionTimes.get(index);
            long seconds = (now.getTime() - captureTime.getTime()) / 1000;
            if (seconds < 60) {
                lastHour++;
                lastMinute++;
            } else if (seconds < 60 * 60) {
                lastHour++;
            } else {
                mDetectionTimes.remove(index);
            }
        }

        Log.i(TAG, "Motion detected - " + lastMinute + " in last minute, " + lastHour + " in last hour");
        boolean reportMotion = (
                lastMinute < mConfig.getThrottleMinute() &&
                lastHour < mConfig.getThrottleHour());

        if (reportMotion) {
            mDetectionTimes.add(now);
        }
        return reportMotion;
    }

    private ImageData getImageData(int width, int height, ByteBuffer byteBuffer) {
        int scale = mConfig.getScale();
        if (scale > 1) {
            // Scale this image
            int sWidth = (int)Math.ceil((double)width / (double)scale);
            int sHeight = (int)Math.ceil((double)height / (double)scale);

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(byteBuffer.array()));

            Bitmap scaled = Bitmap.createScaledBitmap(bitmap, sWidth, sHeight, false);
            ByteBuffer scaledBuffer = ByteBuffer.allocate(scaled.getHeight() * scaled.getRowBytes());
            scaled.copyPixelsToBuffer(scaledBuffer);

            return new ImageData(sWidth, sHeight, scaledBuffer);

        } else {
            return new ImageData(width, height, byteBuffer);
        }
    }

    private MotionResult compare(ImageData file1, ImageData file2) {
        if ((file1.getWidth() != file2.getWidth())
            || (file1.getHeight() != file2.getHeight())) {
            return new MotionResult(false);
        }

        int countedPixels = 0;
        int changedPixels = 0;

        ImageData compareImage = new ImageData(file1.getWidth(), file1.getHeight());

        for(int y = 0; y < file1.getHeight(); y++) {
            for(int x = 0; x < file1.getWidth(); x++) {
                countedPixels++;
                Point point = new Point(x, y);
                RGB imageRgb = getRGB(file2, point);
                if (isChanged(point, file1, file2)) {
                    changedPixels++;
                    setRGB(compareImage, point, new RGB(Math.min(imageRgb.r + 100, 255), imageRgb.g, imageRgb.b));
                } else {
                    setRGB(compareImage, point, imageRgb);
                }
            }
        }

        int percentChanged = changedPixels * 100 / countedPixels;
        Log.d(TAG, "Percent: " + percentChanged);

        boolean movement = percentChanged >= mConfig.getMinPercent()
            && percentChanged <= mConfig.getMaxPercent();

        return new MotionResult(movement, compareImage);
    }

    private boolean isChanged(Point point, ImageData file1, ImageData file2) {
        RGB rgb1 = getRGB(file1, point);
        RGB rgb2 = getRGB(file2, point);

        if (rgb1 != null && rgb2 != null) {
            int change = ((int)Math.abs(rgb1.r - rgb2.r) + (int)Math.abs(rgb1.g - rgb2.g) + (int)Math.abs(rgb1.b - rgb2.b)) / 3;
            return change > mColourThreshold;
        }
        return false;
    }

    private RGB getRGB(ImageData file, Point point) {
        int pos = (file.getWidth() * point.y + point.x) * 4;
        byte[] bytes = file.getBytes();
        if (pos <= bytes.length - 4) {
            return new RGB(bytes[pos], bytes[pos + 1], bytes[pos + 2]);
        }
        return null;
    }

    private void setRGB(ImageData file, Point point, RGB rgb) {
        int pos = (file.getWidth() * point.y + point.x) * 4;
        byte[] bytes = file.getBytes();
        if (pos <= bytes.length - 4) {
            bytes[pos] = (byte)rgb.r;
            bytes[pos + 1] = (byte)rgb.g;
            bytes[pos + 2] = (byte)rgb.b;
            bytes[pos + 3] = (byte)255;
        }
    }

    public class ImageData {
        private int mWidth;
        private int mHeight;
        private ByteBuffer mByteBuffer;

        public ImageData(int width, int height) {
            mWidth = width;
            mHeight = height;
            mByteBuffer = ByteBuffer.allocate(width * height * 4);
        }
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

    public class MotionResult {
        public boolean movementDetected;

        public ImageData imageData;

        public MotionResult(boolean movementDetected) {
            this.movementDetected = movementDetected;
            this.imageData = null;
        }

        public MotionResult(boolean movementDetected, ImageData imageData) {
            this.movementDetected = movementDetected;
            this.imageData = imageData;
        }
    }
}
