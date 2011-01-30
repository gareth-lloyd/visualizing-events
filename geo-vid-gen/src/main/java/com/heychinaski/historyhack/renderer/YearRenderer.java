package com.heychinaski.historyhack.renderer;

import java.awt.AlphaComposite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class YearRenderer implements FrameRenderer<Integer> {
    private BufferedImage currentFrame;
    
    public YearRenderer(int width, int height) {
        currentFrame = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
    }

    public BufferedImage getCurrentFrame() {
        return currentFrame;
    }

    public void renderNextFrame(Integer year) {
        Graphics2D g2 = currentFrame.createGraphics();
        
        // Following block renders zero alpha to whole image
        g2.setComposite(AlphaComposite.Clear);
        g2.fillRect(0, 0, currentFrame.getWidth(), currentFrame.getHeight());
        g2.dispose();
        
        g2 = currentFrame.createGraphics();
        
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 40));
        
        
        String yearString = "" + Math.abs(year);
        if(year < 0) {
            yearString += "BC";
        }
        g2.drawString(yearString, currentFrame.getWidth() - 180, currentFrame.getHeight() - 40);
        
        g2.dispose();
    }

}
