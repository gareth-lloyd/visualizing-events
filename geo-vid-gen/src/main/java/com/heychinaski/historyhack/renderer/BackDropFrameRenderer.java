package com.heychinaski.historyhack.renderer;

import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;


public class BackDropFrameRenderer implements FrameRenderer<Object> {

    BufferedImage backdrop;
    
    public BackDropFrameRenderer(int width, int height, String backDropName) {
        try {
            URL imageSrc = new File(backDropName).toURI().toURL();
            backdrop = ImageIO.read(imageSrc);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Bad image location: " + backDropName);
        } catch (IOException e) {
            throw new RuntimeException("Bad image location: " + backDropName);
        }
        
    }

    public BufferedImage getCurrentFrame() {
        return backdrop;
    }

    public void renderNextFrame(Object data) {
        // pass
    }

}
