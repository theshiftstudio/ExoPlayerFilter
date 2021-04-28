package com.daasuu.epf.filter;

public interface GlFilter {
    String DEFAULT_UNIFORM_SAMPLER = "sTexture";

    void setup();

    default void setFrameSize(int width, int height){ }

    void release();

    default void draw(int texName) {
        draw(texName, 0);
    }

    void draw(int texName, int frameBufferName);

    void onDraw();
}
