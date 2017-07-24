package com.ogadai.ogadai_node.homewatcher.messages;

import com.google.gson.annotations.SerializedName;

/**
 * Created by alee on 30/06/2017.
 */

public class SetState extends Message {
    @SerializedName("name")
    private String mName;

    @SerializedName("state")
    private String mState;

    public static final String TYPE = "setState";

    public SetState() {
        super(TYPE);
    }
    public SetState(String name, String state) {
        super(TYPE);
        mName = name;
        mState  = state;
    }

    public String getName() { return mName; }
    public final void setName(String name) { mName = name; }

    public String getState() { return mState; }
    public final void setState(String state) { mState = state; }
}
