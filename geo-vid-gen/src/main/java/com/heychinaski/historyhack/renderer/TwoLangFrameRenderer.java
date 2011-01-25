package com.heychinaski.historyhack.renderer;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.List;

import com.heychinaski.historyhack.model.GeoEventPage;

public class TwoLangFrameRenderer implements FrameRenderer<Object> {
    private static final int RESOLUTION_FACTOR = 1000;

    public static final int LONGITUDE_DEGREES = 360;
    public static final int LATITUDE_DEGREES = 180;
    
    public static final int LONGITUDE_DEGREES_MULTIPLIED = LONGITUDE_DEGREES * RESOLUTION_FACTOR;
    public static final int LATITUDE_DEGREES_MULTIPLIED = LATITUDE_DEGREES * RESOLUTION_FACTOR;
    private final Color langAColor;
    
    BufferedImage currentFrame;
    Graphics2D currentG2d;
    
    private List<GeoEventPage> langAEvents;

    private int width;
    private int height;

    private List<GeoEventPage> langBEvents;

    private Color langBColor;
    
    /**
     * 
     * @param langAEvents ALL year events
     * @param langBEvents 
     */
    public TwoLangFrameRenderer(List<GeoEventPage> langAEvents, List<GeoEventPage> langBEvents, int width, int height, Color langAColor, Color langBColor) {
        this.width = width;
        this.height = height;
        this.langAColor = langAColor;
        this.langBColor = langBColor;
        this.langAEvents = langAEvents;
        this.langBEvents = langBEvents;
        
        // initialize images
        currentFrame = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        currentG2d = currentFrame.createGraphics();
        currentG2d.setColor(Color.BLACK);
        currentG2d.fillRect(0, 0, currentFrame.getWidth(), currentFrame.getHeight());
        
        currentG2d.scale((float)width / (float)LONGITUDE_DEGREES_MULTIPLIED, (float)height / (float)LATITUDE_DEGREES_MULTIPLIED);
        currentG2d.translate((LONGITUDE_DEGREES_MULTIPLIED) / 2, (LATITUDE_DEGREES_MULTIPLIED) / 2);
    }
    
    public BufferedImage getCurrentFrame() {
        return currentFrame;
    }

    public void renderNextFrame(Object ignore) {
        boolean atLeastOneHasEvents = true;
        int index = 0;
        
        while (atLeastOneHasEvents) {
            atLeastOneHasEvents = false;
            if (langAEvents.size() > index) {
                drawPoint(langAEvents.get(index), langAColor);
                atLeastOneHasEvents = true;
            } 
            if (langBEvents.size() > index) {
                drawPoint(langBEvents.get(index), langBColor);
                atLeastOneHasEvents = true;
            }
            index++;
        }
        
        currentG2d.dispose();
    }

    private void drawPoint(GeoEventPage page, Color color) {
        Point point = new Point((int)Math.round(page.getLongitude() * RESOLUTION_FACTOR), 
                -(int)Math.round(page.getLatitude() * RESOLUTION_FACTOR));
        // do draw
        currentG2d.setColor(color);
        currentG2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
        int radius = 1000;
        currentG2d.fillOval(point.x - radius, point.y - radius, radius * 2, radius * 2);

    }

}
