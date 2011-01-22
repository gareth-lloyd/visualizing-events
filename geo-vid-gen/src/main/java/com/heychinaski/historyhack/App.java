package com.heychinaski.historyhack;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import com.heychinaski.historyhack.model.GeoEventPage;
import com.heychinaski.historyhack.provider.GeoEventPageProvider;
import com.heychinaski.historyhack.provider.RandomGeoEventPageProvider;
import com.heychinaski.historyhack.renderer.CompositeFrameRenderer;
import com.heychinaski.historyhack.renderer.CumulativeGeoFrameRenderer;
import com.heychinaski.historyhack.renderer.YearRenderer;

/**
 * 
 * 
 */
public class App {
    private static final int WIDTH = 1280;
    private static final int HEIGHT = 1024;
    
    public static void main(String[] args) throws IOException {
        long time = System.currentTimeMillis();
        
        GeoEventPageProvider provider = new RandomGeoEventPageProvider(-1000, 2000);
        
        CumulativeGeoFrameRenderer renderer = new CumulativeGeoFrameRenderer(WIDTH, HEIGHT);
        YearRenderer yearRenderer = new YearRenderer(WIDTH, HEIGHT);
        CompositeFrameRenderer compositeFrameRenderer = new CompositeFrameRenderer(WIDTH, HEIGHT);

        int currentFrame = 0;
        while(provider.hasMoreEvents()) {
            List<GeoEventPage> pages = provider.getNextFrame();
            if(pages.size() > 0) {
                renderer.renderNextFrame(pages);
                
                    int year = pages.get(0).getYear();
                    System.out.println("Current year " + year);
                    yearRenderer.renderNextFrame(year);
                
                ArrayList<BufferedImage> overlays = new ArrayList<BufferedImage>();
                BufferedImage geoImage = renderer.getCurrentFrame();
                BufferedImage yearImage = yearRenderer.getCurrentFrame();
                overlays.add(geoImage);
                overlays.add(yearImage);
                compositeFrameRenderer.renderNextFrame(overlays);
                
                RenderedImage image = compositeFrameRenderer.getCurrentFrame();
                
                String frameNumber = String.format("%05d", currentFrame);
                ImageIO.write(image, "png", new File("/home/tomm/frames/frame" + frameNumber + ".png"));
                
                currentFrame++;
            }
        }
        
        long finishTime = System.currentTimeMillis();
        System.out.println("Done");
        System.out.println("Took " + (finishTime - time) + "ms");
    }
}
