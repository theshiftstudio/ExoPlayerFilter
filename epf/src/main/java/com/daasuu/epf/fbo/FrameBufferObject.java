package com.daasuu.epf.fbo;

public interface FrameBufferObject {

    int getTexName();

    int getFrameBufferName();

    void setup(int width, int height);

    void release();

    void enable();
}
