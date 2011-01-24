package com.heychinaski.historyhack;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.imageio.ImageIO;

import com.heychinaski.historyhack.displayobjects.StatefulBlob;
import com.heychinaski.historyhack.displayobjects.StatefulBlobFactory;
import com.heychinaski.historyhack.displayobjects.StatefulBlobFactoryImpl;
import com.heychinaski.historyhack.model.GeoEventPage;
import com.heychinaski.historyhack.provider.ConstantJsonGeoEventPageProvider;
import com.heychinaski.historyhack.provider.GeoEventPageProvider;
import com.heychinaski.historyhack.provider.JsonInputGEPProvider;
import com.heychinaski.historyhack.renderer.CompositeFrameRenderer;
import com.heychinaski.historyhack.renderer.CumulativeGeoFrameRenderer;
import com.heychinaski.historyhack.renderer.FrameRenderer;
import com.heychinaski.historyhack.renderer.GenerationalFrameRenderer;
import com.heychinaski.historyhack.renderer.YearRenderer;

/**
 * 
 * 
 */
public class App {

    static FrameRenderer<List<GeoEventPage>> eventRenderer;
    static FrameRenderer<Integer> yearRenderer;
    static FrameRenderer<List<BufferedImage>> compositeRenderer;
    static GeoEventPageProvider provider;
    static String outputDirectory;
    
    
    public static void main(String[] args) throws IOException {
        Properties properties = new Properties();
        properties.load(Thread.currentThread ().getContextClassLoader ().getResourceAsStream("config.properties"));
        int width = Integer.parseInt(properties.getProperty("width", "" + 1024));
        int height = Integer.parseInt(properties.getProperty("height", "" + 1024));

        int frameSkip = Integer.parseInt(properties.getProperty("frameSkip", "" + 1));
        String backgroundHex = properties.getProperty("backgroundColor", "0x000000");
        String inputLocation = properties.getProperty("inputLocation", "./input.json");
        outputDirectory = properties.getProperty("outputDirectory", "./frames");
        long time = System.currentTimeMillis();

        provider = new ConstantJsonGeoEventPageProvider(inputLocation, 10);

        eventRenderer = new GenerationalFrameRenderer(width, height, 
                Color.decode(backgroundHex), new StatefulBlobFactoryImpl());
        yearRenderer = new YearRenderer(width, height);
        compositeRenderer = new CompositeFrameRenderer(width, height);

        int currentFrame = 0;
        while(provider.hasMoreEvents()) {
            List<GeoEventPage> pages = new ArrayList<GeoEventPage>();
            
            int frame = 0;
            while(provider.hasMoreEvents() && frame < frameSkip) {
                pages.addAll(provider.getNextFrame());
                frame++;
            }
            
            render(pages, currentFrame);
            currentFrame++;
        }
        
        // pad ending
        for (int i = 0; i < 100; i++) {
            List<GeoEventPage> empty = Collections.emptyList();
            render(empty, currentFrame++);
        }
        
        long finishTime = System.currentTimeMillis();
        System.out.println("Done");
        System.out.println("Took " + (finishTime - time) + "ms");
    }

    private static void render(List<GeoEventPage> pages, int currentFrame) throws IOException {
        if(pages.size() > 0) {
            eventRenderer.renderNextFrame(pages);
        }

        int year = provider.getCurrentYear();
        System.out.println("Current year " + year);
        yearRenderer.renderNextFrame(year);

        ArrayList<BufferedImage> overlays = new ArrayList<BufferedImage>();
        BufferedImage geoImage = eventRenderer.getCurrentFrame();
        BufferedImage yearImage = yearRenderer.getCurrentFrame();
        overlays.add(geoImage);
        overlays.add(yearImage);
        compositeRenderer.renderNextFrame(overlays);

        RenderedImage image = compositeRenderer.getCurrentFrame();

        String frameNumber = String.format("%05d", currentFrame);
        ImageIO.write(image, "png", new File(outputDirectory + "/frame" + frameNumber + ".png"));
    }
}
