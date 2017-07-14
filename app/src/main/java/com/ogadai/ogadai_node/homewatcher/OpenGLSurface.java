package com.ogadai.ogadai_node.homewatcher;

import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.view.Surface;

/**
 * Created by alee on 14/07/2017.
 */

public class OpenGLSurface {

    private SurfaceTexture mTexture;

    public OpenGLSurface(int width, int height) {
        setupTexture(width, height);
    }

    public Surface getSurface() {
        if (mTexture != null) {
            return new Surface(mTexture);
        }
        return null;
    }

    private void setupTexture(int width, int height) {
        int textures[] = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width,
                height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);

        mTexture = new SurfaceTexture(textures[0]);
        mTexture.setDefaultBufferSize(width, height);
    }
}
