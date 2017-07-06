package com.ogadai.ogadai_node.homewatcher.messages;

import com.google.gson.annotations.SerializedName;

/**
 * Created by alee on 04/07/2017.
 */

public class Sensor extends Message {
    @SerializedName("name")
    private String mName;

    @SerializedName("message")
    private String mMessage;

    public static final String TYPE = "sensor";

    public Sensor() {
        super(TYPE);
    }
    public Sensor(String name, String message)
    {
        super(TYPE);
        mName = name;
        mMessage = message;
    }

    public String getName() { return mName; }
    public final void setName(String name) { mName = name; }

    public String getMessage() { return mMessage; }
    public final void setMessage(String message) { mMessage = message; }
}
