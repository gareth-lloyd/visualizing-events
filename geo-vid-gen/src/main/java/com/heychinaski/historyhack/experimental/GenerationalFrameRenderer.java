package com.heychinaski.historyhack.experimental;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.heychinaski.historyhack.model.GeoEventPage;
import com.heychinaski.historyhack.renderer.FrameRenderer;

public class GenerationalFrameRenderer implements FrameRenderer<Integer> {
    private static final int RESOLUTION_FACTOR = 1000;

    public static final int LONGITUDE_DEGREES = 360;
    public static final int LATITUDE_DEGREES = 180;
    
    public static final int LONGITUDE_DEGREES_MULTIPLIED = LONGITUDE_DEGREES * RESOLUTION_FACTOR;
    public static final int LATITUDE_DEGREES_MULTIPLIED = LATITUDE_DEGREES * RESOLUTION_FACTOR;
    private final Color foregroundColor;
    
    BufferedImage imageWithRetireesOnly;
    Graphics2D retireesImageG2d;
    
    private int currentYear;
    private Map<Integer, List<GeoEventPage>> yearPages;


    private BufferedImage currentFrame;

    private LinkedList<List<StatefulBlob>> relevantGenerations;

    private int width;

    private int height;
    
    /**
     * 
     * @param yearEvents ALL year events
     */
    public GenerationalFrameRenderer(Map<Integer, List<GeoEventPage>> yearEvents, int width, int height, Color foregroundColor, Color backgroundColor) {
        this.width = width;
        this.height = height;
        this.foregroundColor = foregroundColor;
        
        this.yearPages = yearEvents;
        
        Integer minYear = Integer.MAX_VALUE;
        for (Integer year : yearEvents.keySet()) {
            if (year < minYear) {
                minYear = year;
            }
        }
        currentYear = minYear;
        
        this.relevantGenerations = new LinkedList<List<StatefulBlob>>();
        
        // initialize images
        imageWithRetireesOnly = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        retireesImageG2d = imageWithRetireesOnly.createGraphics();
        retireesImageG2d.setColor(backgroundColor);
        retireesImageG2d.fillRect(0, 0, width, height);
        
        retireesImageG2d.scale((float)width / (float)LONGITUDE_DEGREES_MULTIPLIED, (float)height / (float)LATITUDE_DEGREES_MULTIPLIED);
        retireesImageG2d.translate((LONGITUDE_DEGREES_MULTIPLIED) / 2, (LATITUDE_DEGREES_MULTIPLIED) / 2);
    }
    
    @Override
    public BufferedImage getCurrentFrame() {
        return currentFrame;
    }

    @Override
    public void renderNextFrame(Integer year) {
        // draw only the retirees onto the stored image
        if (relevantGenerations.size() == StatefulBlob.TOTAL_STATES) {
            // after this drawing, we won't need them any more
            System.out.println();
            List<StatefulBlob> retirees = relevantGenerations.removeLast();
            drawGeneration(retirees, retireesImageG2d);
        }
        
        // copy this before drawing others
        currentFrame = deepCopy(imageWithRetireesOnly);
        Graphics2D currentg2d = currentFrame.createGraphics();
        currentg2d.scale((float)width / (float)LONGITUDE_DEGREES_MULTIPLIED, (float)height / (float)LATITUDE_DEGREES_MULTIPLIED);
        currentg2d.translate((LONGITUDE_DEGREES_MULTIPLIED) / 2, (LATITUDE_DEGREES_MULTIPLIED) / 2);
        
        // create StatefulBlobs for new Generation
        if (!yearPages.containsKey(year)) {
            return;
        }
        List<StatefulBlob> newGeneration = new ArrayList<StatefulBlob>();
        for (GeoEventPage page : yearPages.get(year)) {
            Point point = new Point((int)Math.round(page.getLongitude() * RESOLUTION_FACTOR), 
                    -(int)Math.round(page.getLatitude() * RESOLUTION_FACTOR));
            newGeneration.add(new StatefulBlob(point));
        }
        relevantGenerations.addFirst(newGeneration);
        
        // draw all non-retired generations
        for (List<StatefulBlob> generation : relevantGenerations) {
            drawGeneration(generation, currentg2d);
        }
        currentg2d.dispose();
    }

    private void drawGeneration(List<StatefulBlob> generation, Graphics2D g2d) {
        for (StatefulBlob blob : generation) {
            blob.drawAndIncrementState(g2d);
        }
    }

    private BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        BufferedImage cloned = new BufferedImage(cm, raster, isAlphaPremultiplied, null);
        return cloned;
    }

}
