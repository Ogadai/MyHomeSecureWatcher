package com.ogadai.ogadai_node.homewatcher.devices;

/**
 * Created by alee on 03/07/2017.
 */

public abstract class DeviceBase implements Device {
    private Emitter mEmitter;

    public void setHandler(Emitter emitter) {
        mEmitter = emitter;
    }

    protected void emit(String state) {
        if (mEmitter != null) {
            mEmitter.changed(state);
        }
    }

    protected void post(String relativePath, byte[] data) {
        if (mEmitter != null) {
            mEmitter.post(relativePath, data);
        }
    }
}
