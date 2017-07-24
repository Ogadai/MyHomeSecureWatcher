package com.ogadai.ogadai_node.homewatcher;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

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

    private static final String DEFAULT_ADDRESS = "ws://10.0.2.2:45738";
    private static final String DEFAULT_NAME = "mytestnode";

    private static final int DEFAULT_THRESHOLD = 15;
    private static final int DEFAULT_MINPERCENT = 5;
    private static final int DEFAULT_MAXPERCENT = 40;
    private static final int DEFAULT_SEQUENCE = 2;

    public String getAddress() { return mAddress; }
    public void setAddress(String address) { mAddress = address; }

    public String getName() { return mName; }
    public void setName(String name) { mName = name; }

    public static Configuration readSettings(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        String address = prefs.getString(ADDRESSPREF, DEFAULT_ADDRESS);
        String name = prefs.getString(NAMEPREF, DEFAULT_NAME);

        Configuration config = new Configuration(address, name);

        config.setColourThreshold(prefs.getInt(THRESHOLDPREF, DEFAULT_THRESHOLD));
        config.setMinPercent(prefs.getInt(MINPRECENTPREF, DEFAULT_MINPERCENT));
        config.setMaxPercent(prefs.getInt(MAXPERCENTPREF, DEFAULT_MAXPERCENT));
        config.setSequence(prefs.getInt(SEQUENCEPREF, DEFAULT_SEQUENCE));

        return config;
    }

    public static void saveSettings(Context context, Configuration config) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(ADDRESSPREF, config.getAddress());
        editor.putString(NAMEPREF, config.getName());

        editor.putInt(THRESHOLDPREF, config.getColourThreshold());
        editor.putInt(MINPRECENTPREF, config.getMinPercent());
        editor.putInt(MAXPERCENTPREF, config.getMaxPercent());
        editor.putInt(SEQUENCEPREF, config.getSequence());

        editor.commit();
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
}
