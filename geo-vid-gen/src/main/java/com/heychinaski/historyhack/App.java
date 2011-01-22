package com.heychinaski.historyhack;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.imageio.ImageIO;

import com.heychinaski.historyhack.model.GeoEventPage;
import com.heychinaski.historyhack.provider.GeoEventPageProvider;
import com.heychinaski.historyhack.provider.JsonInputGEPProvider;
import com.heychinaski.historyhack.renderer.CompositeFrameRenderer;
import com.heychinaski.historyhack.renderer.CumulativeGeoFrameRenderer;
import com.heychinaski.historyhack.renderer.YearRenderer;

/**
 * 
 * 
 */
public class App {

    public static void main(String[] args) throws IOException {
        Properties properties = new Properties();
        properties.load(Thread.currentThread ().getContextClassLoader ().getResourceAsStream("config.properties"));
        int width = Integer.parseInt(properties.getProperty("width", "" + 1024));
        int height = Integer.parseInt(properties.getProperty("height", "" + 1024));

        String foregroundHex = properties.getProperty("foregroundColor", "0xFFFFFF");
        String backgroundHex = properties.getProperty("backgroundColor", "0x000000");

        int frameSkip = Integer.parseInt(properties.getProperty("frameSkip", "" + 1));
        
        String inputLocation = properties.getProperty("inputLocation", "./input.json");
        String outputDirectory = properties.getProperty("outputDirectory", "./frames");
        
        boolean drawHalos = Boolean.parseBoolean(properties.getProperty("drawHalos", "false"));
        boolean drawDebug = Boolean.parseBoolean(properties.getProperty("drawDebug", "false"));


        long time = System.currentTimeMillis();

        GeoEventPageProvider provider = new JsonInputGEPProvider(inputLocation);

        CumulativeGeoFrameRenderer renderer = new CumulativeGeoFrameRenderer(width, height, Color.decode(foregroundHex), Color.decode(backgroundHex), drawHalos, drawDebug);
        YearRenderer yearRenderer = new YearRenderer(width, height);
        CompositeFrameRenderer compositeFrameRenderer = new CompositeFrameRenderer(width, height);

        int currentFrame = 0;
        while(provider.hasMoreEvents()) {
            List<GeoEventPage> pages = new ArrayList<GeoEventPage>();
            
            int frame = 0;
            while(provider.hasMoreEvents() && frame < frameSkip) {
                pages.addAll(provider.getNextFrame());
                frame++;
            }
            
            if(pages.size() > 0) {
                renderer.renderNextFrame(pages);
            }

            int year = provider.getCurrentYear();
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
            ImageIO.write(image, "png", new File(outputDirectory + "/frame" + frameNumber + ".png"));

            currentFrame++;
        }

        long finishTime = System.currentTimeMillis();
        System.out.println("Done");
        System.out.println("Took " + (finishTime - time) + "ms");
    }
}
