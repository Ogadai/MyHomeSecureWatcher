package com.ogadai.ogadai_node.homewatcher;

/**
 * Created by alee on 06/10/2017.
 */

public interface CameraPreview {
    void showImage(int width, int height, byte[] imageBytes);
}
