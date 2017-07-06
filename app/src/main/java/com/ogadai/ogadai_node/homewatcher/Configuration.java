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

    public Configuration(String address, String name) {
        mAddress = address;
        mName = name;
    }

    public static final String ADDRESSPREF = "hub_address";
    public static final String NAMEPREF = "node_name";

    private static final String DEFAULT_ADDRESS = "ws://10.0.2.2:45738";
    private static final String DEFAULT_NAME = "mytestnode";

    public String getAddress() { return mAddress; }
    public void setAddress(String address) { mAddress = address; }

    public String getName() { return mName; }
    public void setName(String name) { mName = name; }

    public static Configuration readSettings(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        String address = prefs.getString(ADDRESSPREF, DEFAULT_ADDRESS);
        String name = prefs.getString(NAMEPREF, DEFAULT_NAME);

        return new Configuration(address, name);
    }

    public static void saveSettings(Context context, Configuration config) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(ADDRESSPREF, config.getAddress());
        editor.putString(NAMEPREF, config.getName());
        editor.commit();
    }
}
