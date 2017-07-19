package com.ogadai.ogadai_node.homewatcher.messages;

/**
 * Created by andyl on 19/07/2017.
 */

public class Ping extends Message {
    public static final String TYPE = "ping";

    public Ping() {
        super(TYPE);
    }
}
