package com.heychinaski.historyhack;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.imageio.ImageIO;

import com.heychinaski.historyhack.model.GeoEventPage;
import com.heychinaski.historyhack.provider.AllJsonEventsProvider;
import com.heychinaski.historyhack.provider.GeoEventPageProvider;
import com.heychinaski.historyhack.provider.JsonInputGEPProvider;
import com.heychinaski.historyhack.renderer.CompositeFrameRenderer;
import com.heychinaski.historyhack.renderer.CumulativeGeoFrameRenderer;
import com.heychinaski.historyhack.renderer.GenerationalFrameRenderer;
import com.heychinaski.historyhack.renderer.TwoLangFrameRenderer;
import com.heychinaski.historyhack.renderer.YearRenderer;

/**
 * 
 * 
 */
public class ComparisonFrameApp {

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

        AllJsonEventsProvider englishProvider = new AllJsonEventsProvider(inputLocation);
        AllJsonEventsProvider frenchProvider = new AllJsonEventsProvider("./fr-data.json");
        
        List<GeoEventPage> englishPages = englishProvider.getAllAsList();
        List<GeoEventPage> frenchPages = frenchProvider.getAllAsList();
        
        TwoLangFrameRenderer twoLangRenderer = new TwoLangFrameRenderer(englishPages, frenchPages, width, height, Color.RED, Color.WHITE);
        CompositeFrameRenderer compositeFrameRenderer = new CompositeFrameRenderer(width, height);
        
        twoLangRenderer.renderNextFrame(null);

        ArrayList<BufferedImage> overlays = new ArrayList<BufferedImage>();
        BufferedImage englishImage = twoLangRenderer.getCurrentFrame();
        overlays.add(englishImage);
        compositeFrameRenderer.renderNextFrame(overlays);

        RenderedImage image = compositeFrameRenderer.getCurrentFrame();

        ImageIO.write(image, "png", new File(outputDirectory + "/frame" + "engFrench" + ".png"));

        long finishTime = System.currentTimeMillis();
        System.out.println("Done");
        System.out.println("Took " + (finishTime - time) + "ms");
    }
}
