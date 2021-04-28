package com.daasuu.epf.render;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.daasuu.epf.fbo.EFrameBufferObject;
import com.daasuu.epf.fbo.FrameBufferObject;
import com.daasuu.epf.filter.GlBaseFilter;

import java.util.LinkedList;
import java.util.Queue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_FRAMEBUFFER;

/**
 * Created by sudamasayuki on 2017/05/16.
 */

abstract class EFrameBufferObjectRenderer implements GLSurfaceView.Renderer {

    private FrameBufferObject framebufferObject;
    private GlBaseFilter normalShader;

    private int width;
    private int height;

    private final Queue<Runnable> runOnDraw;


    EFrameBufferObjectRenderer() {
        runOnDraw = new LinkedList<>();
    }


    @Override
    public final void onSurfaceCreated(final GL10 gl, final EGLConfig config) {
        framebufferObject = new EFrameBufferObject();
        normalShader = new GlBaseFilter();
        normalShader.setup();
        onSurfaceCreated(config);
    }

    @Override
    public final void onSurfaceChanged(final GL10 gl, final int width, final int height) {
        this.width = width;
        this.height = height;
        framebufferObject.setup(width, height);
        normalShader.setFrameSize(width, height);
        onSurfaceChanged(width, height);
    }

    @Override
    public final void onDrawFrame(final GL10 gl) {
        synchronized (runOnDraw) {
            while (!runOnDraw.isEmpty()) {
                runOnDraw.poll().run();
            }
        }
        framebufferObject.enable();
        GLES20.glViewport(0, 0, width, height);

        onDrawFrame(framebufferObject, width, height);

        GLES20.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        GLES20.glViewport(0, 0, width, height);

        GLES20.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        normalShader.draw(framebufferObject.getTexName());

    }

    public abstract void onSurfaceCreated(EGLConfig config);

    public abstract void onSurfaceChanged(int width, int height);

    public abstract void onDrawFrame(FrameBufferObject fbo, int width, int height);
}
