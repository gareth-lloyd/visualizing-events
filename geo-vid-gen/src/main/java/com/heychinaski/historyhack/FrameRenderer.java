package com.heychinaski.historyhack;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.util.List;

public class FrameRenderer {
    private static final Color BG_COLOR = Color.DARK_GRAY;

    private BufferedImage currentFrame;
    
    private Graphics2D g2d;
    
    public RenderedImage getCurrentFrame() {
        return currentFrame;
    }

    public FrameRenderer(int width, int height) {
        currentFrame = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        g2d = currentFrame.createGraphics();

        g2d.setColor(BG_COLOR);
        g2d.fillRect(0, 0, width, height);
    }
    
    public void renderNextFrame(List<Point> points ) {
        
        g2d.setColor(Color.WHITE);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
        for(Point point : points) {
            g2d.drawLine(point.x, point.y, point.x, point.y);
        }
        
    }
}
