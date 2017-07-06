package com.ogadai.ogadai_node.homewatcher.messages;

import com.google.gson.annotations.SerializedName;

/**
 * Created by alee on 30/06/2017.
 */

public class Initialise extends Message {
    @SerializedName("name")
    private String mName;

    public static final String TYPE = "initialise";

    public Initialise() {
        super(TYPE);
    }
    public Initialise(String name) {
        super(TYPE);
        mName = name;
    }

    public String getName() { return mName; }
    public final void setName(String name) { mName = name; }
}
