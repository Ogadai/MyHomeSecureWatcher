package com.ogadai.ogadai_node.homewatcher.messages;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Type;

/**
 * Created by alee on 30/06/2017.
 */

public class Message {
    @SerializedName("method")
    private String mMethod;

    public Message() {}
    public Message(String method) {
        mMethod = method;
    }

    public String getMethod() { return mMethod; }
    public final void setMethod(String method) { mMethod = method; }

    public String toJSON() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static Message fromJSON(String json) {
        Gson gson = new Gson();
        Message deviceMessage = gson.fromJson(json, Message.class);

        Type messageType = getMessageType(deviceMessage.getMethod());
        if (messageType != null) {
            return gson.fromJson(json, messageType);
        }
        return null;
    }

    private static Type getMessageType(String name) {
        if (name.equalsIgnoreCase(Initialise.TYPE)) {
            return Initialise.class;
        } else if (name.equalsIgnoreCase(Settings.TYPE)) {
            return Settings.class;
        } else if (name.equalsIgnoreCase(SetState.TYPE)) {
            return SetState.class;
        }
        return null;
    }
}
