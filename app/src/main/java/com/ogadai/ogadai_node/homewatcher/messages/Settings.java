package com.ogadai.ogadai_node.homewatcher.messages;

import com.google.gson.annotations.SerializedName;

/**
 * Created by alee on 30/06/2017.
 */

public class Settings extends Message {
    @SerializedName("settings")
    private SettingsData mSettings;

    public static final String TYPE = "settings";

    public Settings() {
        super(TYPE);
    }

    public SettingsData getSettings() { return mSettings; }
    public void setSettings(SettingsData settings) { mSettings = settings; }

    public class SettingsData {
        @SerializedName("addr")
        private String mAddr;

        @SerializedName("identification")
        private Identification mIdentification;

        public String getAddr() { return mAddr; }
        public final void setAddr(String addr) { mAddr = addr; }

        public Identification getIdentification() { return mIdentification; }
        public final void setIdentification(Identification identification) { mIdentification = identification; }
    }

    public class Identification {
        @SerializedName("name")
        private String mName;

        @SerializedName("token")
        private String mToken;

        public String getName() { return mName; }
        public final void setName(String name) { mName = name; }

        public String getToken() { return mToken; }
        public final void setToken(String token) { mToken = token; }
    }
}
