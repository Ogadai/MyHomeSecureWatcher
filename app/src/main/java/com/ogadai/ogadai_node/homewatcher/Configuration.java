package com.ogadai.ogadai_node.homewatcher;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by alee on 30/06/2017.
 */

public class Configuration {
    private String mAddress;
    private String mName;

    private int mColourThreshold;
    private int mMinPercent;
    private int mMaxPercent;
    private int mSequence;
    private int mScale;

    public Configuration(String address, String name) {
        mAddress = address;
        mName = name;
    }

    public static final String ADDRESSPREF = "hub_address";
    public static final String NAMEPREF = "node_name";

    public static final String THRESHOLDPREF = "colour_threshold";
    public static final String MINPRECENTPREF = "min_percent";
    public static final String MAXPERCENTPREF = "max_percent";
    public static final String SEQUENCEPREF = "sequence";
    public static final String SCALEPREF = "scale";

    private static final String DEFAULT_ADDRESS = "ws://10.0.2.2:45738";
    private static final String DEFAULT_NAME = "mytestnode";

    private static final int DEFAULT_THRESHOLD = 15;
    private static final int DEFAULT_MINPERCENT = 5;
    private static final int DEFAULT_MAXPERCENT = 40;
    private static final int DEFAULT_SEQUENCE = 2;
    private static final int DEFAULT_SCALE = 4;

    public String getAddress() { return mAddress; }
    public void setAddress(String address) { mAddress = address; }

    public String getName() { return mName; }
    public void setName(String name) { mName = name; }

    private static final String TAG = "Configuration";

    public static Configuration readSettings(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        String address = prefs.getString(ADDRESSPREF, DEFAULT_ADDRESS);
        String name = prefs.getString(NAMEPREF, DEFAULT_NAME);

        Configuration config = new Configuration(address, name);
        config.setColourThreshold(getInt(prefs, THRESHOLDPREF, DEFAULT_THRESHOLD));
        config.setMinPercent(getInt(prefs, MINPRECENTPREF, DEFAULT_MINPERCENT));
        config.setMaxPercent(getInt(prefs, MAXPERCENTPREF, DEFAULT_MAXPERCENT));
        config.setSequence(getInt(prefs, SEQUENCEPREF, DEFAULT_SEQUENCE));
        config.setScale(getInt(prefs, SCALEPREF, DEFAULT_SCALE));

        return config;
    }

    public static void saveSettings(Context context, Configuration config) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(ADDRESSPREF, config.getAddress());
        editor.putString(NAMEPREF, config.getName());

        putInt(editor, THRESHOLDPREF, config.getColourThreshold());
        putInt(editor, MINPRECENTPREF, config.getMinPercent());
        putInt(editor, MAXPERCENTPREF, config.getMaxPercent());
        putInt(editor, SCALEPREF, config.getScale());

        editor.commit();
    }

    private static void putInt(SharedPreferences.Editor editor, String name, int value) {
        editor.putString(name, Integer.toString(value));
    }

    private static int getInt(SharedPreferences prefs, String name, int defaultValue) {
        try {
            String strValue = prefs.getString(name, null);
            if (strValue != null && strValue.length() > 0) {
                    return Integer.parseInt(strValue);
            }
        } catch(Exception e) {
            Log.d(TAG, "Couldn't read preference value '" + name + "'");
        }
        return defaultValue;
    }

    public int getColourThreshold() {
        return mColourThreshold;
    }

    public void setColourThreshold(int colourThreshold) {
        mColourThreshold = colourThreshold;
    }

    public int getMinPercent() {
        return mMinPercent;
    }

    public void setMinPercent(int minPercent) {
        mMinPercent = minPercent;
    }

    public int getMaxPercent() {
        return mMaxPercent;
    }

    public void setMaxPercent(int maxPercent) {
        mMaxPercent = maxPercent;
    }

    public int getSequence() {
        return mSequence;
    }

    public void setSequence(int sequence) {
        mSequence = sequence;
    }

    public int getScale() {
        return mScale;
    }

    public void setScale(int scale) {
        mScale = scale;
    }
}
