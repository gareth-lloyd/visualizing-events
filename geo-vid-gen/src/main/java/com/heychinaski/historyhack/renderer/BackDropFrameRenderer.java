package com.heychinaski.historyhack.renderer;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;


public class BackDropFrameRenderer implements FrameRenderer<Object> {

    BufferedImage backDrop;
    
    public BackDropFrameRenderer(int width, int height, String backDropName) {
        backDrop = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g2d = backDrop.createGraphics();
        Image image = Toolkit.getDefaultToolkit().getImage(backDropName);
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
    }

    public BufferedImage getCurrentFrame() {
        return backDrop;
    }

    public void renderNextFrame(Object data) {
        // pass
    }

}
