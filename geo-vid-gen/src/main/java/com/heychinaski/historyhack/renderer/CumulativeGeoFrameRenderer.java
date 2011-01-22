package com.heychinaski.historyhack.renderer;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;

import com.heychinaski.historyhack.model.GeoEventPage;

public class CumulativeGeoFrameRenderer implements FrameRenderer<List<GeoEventPage>> {
    private static final int RESOLUTION_FACTOR = 1000;

    private static final Color BG_COLOR = Color.BLACK;

    public static final int LONGITUDE_DEGREES = 360;
    public static final int LATITUDE_DEGREES = 180;
    
    public static final int LONGITUDE_DEGREES_MULTIPLIED = LONGITUDE_DEGREES * RESOLUTION_FACTOR;
    public static final int LATITUDE_DEGREES_MULTIPLIED = LATITUDE_DEGREES * RESOLUTION_FACTOR;

    private BufferedImage currentFrame;
    
    private Graphics2D g2d;
    
    /* (non-Javadoc)
     * @see com.heychinaski.historyhack.FrameRenderer#getCurrentFrame()
     */
    public BufferedImage getCurrentFrame() {
        return currentFrame;
    }

    public CumulativeGeoFrameRenderer(int width, int height) {
        currentFrame = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        g2d = currentFrame.createGraphics();

        g2d.setColor(BG_COLOR);
        g2d.fillRect(0, 0, width, height);
        
        g2d.scale((float)width / (float)LONGITUDE_DEGREES_MULTIPLIED, (float)height / (float)LATITUDE_DEGREES_MULTIPLIED);
        g2d.translate((LONGITUDE_DEGREES_MULTIPLIED) / 2, (LATITUDE_DEGREES_MULTIPLIED) / 2);
    }
    
    /* (non-Javadoc)
     * @see com.heychinaski.historyhack.FrameRenderer#renderNextFrame(java.util.List)
     */
    public void renderNextFrame(List<GeoEventPage> pages) {
        g2d.setColor(Color.WHITE);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        for(GeoEventPage page : pages) {

            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.005f));            
            for(int i = RESOLUTION_FACTOR * 5; i >= RESOLUTION_FACTOR; i -= RESOLUTION_FACTOR) {
                g2d.fillOval((int)(page.getLongitude() * RESOLUTION_FACTOR) - i, (int)(page.getLatitude() * RESOLUTION_FACTOR) - i, 2 * i, 2 * i);
            }
            
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
            g2d.fillOval((int)(page.getLongitude() * RESOLUTION_FACTOR) - (RESOLUTION_FACTOR / 2), (int)(page.getLatitude() * RESOLUTION_FACTOR) - (RESOLUTION_FACTOR / 2), (RESOLUTION_FACTOR), (RESOLUTION_FACTOR / 2));
                
        }
        
    }
}
