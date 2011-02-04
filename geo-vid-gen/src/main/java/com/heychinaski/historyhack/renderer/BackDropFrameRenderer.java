package com.heychinaski.historyhack.renderer;

import java.awt.Color;
import java.awt.Graphics2D;
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
        } catch (Exception e) {
            backdrop = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
            Graphics2D g2d = backdrop.createGraphics();
            g2d.setColor(Color.decode("0x3e133d"));
            g2d.drawRect(0, 0, width, height);
            g2d.dispose();
        }
        
    }

    public BufferedImage getCurrentFrame() {
        return backdrop;
    }

    public void renderNextFrame(Object data) {
        // pass
    }

}
