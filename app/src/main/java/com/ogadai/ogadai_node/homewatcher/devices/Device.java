package com.ogadai.ogadai_node.homewatcher.devices;

/**
 * Created by alee on 03/07/2017.
 */

public interface Device {
    void setState(String state);
    void setHandler(Emitter emitter);

    interface Emitter {
        void changed(String state);
        void post(String relativePath, byte[] data);
    }
}
