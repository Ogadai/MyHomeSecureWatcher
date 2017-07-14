package com.ogadai.ogadai_node.homewatcher;

/**
 * Created by alee on 05/07/2017.
 */
public interface CameraControls {
    void start();

    void stop();

    void takePicture(Camera2.TakePictureCallback callback);
}
