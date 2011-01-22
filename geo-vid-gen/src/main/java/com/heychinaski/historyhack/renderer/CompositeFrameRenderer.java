package com.heychinaski.historyhack.renderer;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

public class CompositeFrameRenderer implements FrameRenderer<List<BufferedImage>> {
    
    private BufferedImage currentFrame;
    
    public CompositeFrameRenderer(int width, int height) {
        currentFrame = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
    }

    @Override
    public BufferedImage getCurrentFrame() {
        return currentFrame;
    }

    @Override
    public void renderNextFrame(List<BufferedImage> overlays) {
        Graphics2D g2 = currentFrame.createGraphics();

        
        // Clear the image
        g2.setComposite(AlphaComposite.Clear);
        g2.fillRect(0, 0, currentFrame.getWidth(), currentFrame.getHeight());
        g2.dispose();
        
        g2 = currentFrame.createGraphics();
        for(BufferedImage image : overlays) {
            g2.drawImage(image, 0, 0, null);
        }
        
        g2.dispose();
    }

}
