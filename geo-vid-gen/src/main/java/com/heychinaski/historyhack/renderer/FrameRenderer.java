package com.heychinaski.historyhack.renderer;

import java.awt.image.BufferedImage;

public interface FrameRenderer<T> {

    public BufferedImage getCurrentFrame();

    public void renderNextFrame(T data);
}