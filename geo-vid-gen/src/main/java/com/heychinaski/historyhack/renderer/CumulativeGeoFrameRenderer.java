package com.heychinaski.historyhack.renderer;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;

import com.heychinaski.historyhack.model.GeoEventPage;

public class CumulativeGeoFrameRenderer implements FrameRenderer<List<GeoEventPage>> {
    private static final int RESOLUTION_FACTOR = 1000;

    public static final int LONGITUDE_DEGREES = 360;
    public static final int LATITUDE_DEGREES = 180;
    
    public static final int LONGITUDE_DEGREES_MULTIPLIED = LONGITUDE_DEGREES * RESOLUTION_FACTOR;
    public static final int LATITUDE_DEGREES_MULTIPLIED = LATITUDE_DEGREES * RESOLUTION_FACTOR;

    private BufferedImage currentFrame;
    
    private Graphics2D g2d;
    
    private final Color foregroundColor;
    
    private final boolean drawHalo;
    
    private final boolean drawDebug;
    
    /* (non-Javadoc)
     * @see com.heychinaski.historyhack.FrameRenderer#getCurrentFrame()
     */
    public BufferedImage getCurrentFrame() {
        return currentFrame;
    }

    public CumulativeGeoFrameRenderer(int width, int height, Color foregroundColor, Color backgroundColor, boolean drawHalo, boolean drawDebug) {
        currentFrame = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        g2d = currentFrame.createGraphics();
        
        this.foregroundColor = foregroundColor;
        
        this.drawHalo = drawHalo;
        this.drawDebug = drawDebug;

        g2d.setColor(backgroundColor);
        g2d.fillRect(0, 0, width, height);
        
        g2d.scale((float)width / (float)LONGITUDE_DEGREES_MULTIPLIED, (float)height / (float)LATITUDE_DEGREES_MULTIPLIED);
        g2d.translate((LONGITUDE_DEGREES_MULTIPLIED) / 2, (LATITUDE_DEGREES_MULTIPLIED) / 2);
    }
    
    /* (non-Javadoc)
     * @see com.heychinaski.historyhack.FrameRenderer#renderNextFrame(java.util.List)
     */
    public void renderNextFrame(List<GeoEventPage> pages) {
        g2d.setColor(foregroundColor);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        for(GeoEventPage page : pages) {
            // Multiply our point to get to the resolution we're working at
            Point multipliedPoint = new Point((int)Math.round(page.getLongitude() * RESOLUTION_FACTOR), 
                                                -(int)Math.round(page.getLatitude() * RESOLUTION_FACTOR));

            if(drawDebug) {
                g2d.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 2 * RESOLUTION_FACTOR));
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                g2d.drawString("" + page.getLatitude() + ", " + page.getLongitude(), multipliedPoint.x, multipliedPoint.y);
            } else {
                if(drawHalo) {
                    // draw some very faint circles at ever decreasing radii around our point.
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.005f));            
                    for(int radius = RESOLUTION_FACTOR * 5; radius >= RESOLUTION_FACTOR; radius -= RESOLUTION_FACTOR) {
                        g2d.fillOval(multipliedPoint.x - radius, multipliedPoint.y - radius, 2 * radius, 2 * radius);
                    }
                }
                
                // Draw the actual point, faintly but not as faint as above
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
                int radius = RESOLUTION_FACTOR / 2;
                g2d.fillOval(multipliedPoint.x - radius, multipliedPoint.y - radius, radius * 2, radius * 2);                
            }
                
        }
        
    }
}
