package com.heychinaski.historyhack.renderer;

import java.awt.image.BufferedImage;

public interface FrameRenderer<T> {

    public abstract BufferedImage getCurrentFrame();

    public abstract void renderNextFrame(T data);

}