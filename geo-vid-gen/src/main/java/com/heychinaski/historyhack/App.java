package com.heychinaski.historyhack;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import com.heychinaski.historyhack.displayobjects.StatefulBlobFactoryImpl;
import com.heychinaski.historyhack.model.GeoEventPage;
import com.heychinaski.historyhack.provider.ConstantJsonGeoEventPageProvider;
import com.heychinaski.historyhack.provider.GeoEventPageProvider;
import com.heychinaski.historyhack.provider.JsonInputGEPProvider;
import com.heychinaski.historyhack.renderer.BackDropFrameRenderer;
import com.heychinaski.historyhack.renderer.CompositeFrameRenderer;
import com.heychinaski.historyhack.renderer.FrameRenderer;
import com.heychinaski.historyhack.renderer.GenerationalFrameRenderer;
import com.heychinaski.historyhack.renderer.YearRenderer;

/**
 * 
 * 
 */
public class App {

    static StatefulBlobFactoryImpl factory;
    static FrameRenderer<Object> backDropRenderer;
    static FrameRenderer<List<GeoEventPage>> eventRenderer;
    static FrameRenderer<Integer> yearRenderer;
    static FrameRenderer<List<BufferedImage>> compositeRenderer;
    static GeoEventPageProvider provider;
    static String outputDirectory;
    static int width;
    static int height;
    
    public static void main(String[] args) throws IOException {
        Properties properties = new Properties();
        properties.load(Thread.currentThread ().getContextClassLoader ().getResourceAsStream("config.properties"));
        width = Integer.parseInt(properties.getProperty("width", "" + 1024));
        height = Integer.parseInt(properties.getProperty("height", "" + 1024));

        int frameSkip = Integer.parseInt(properties.getProperty("frameSkip", "" + 1));
        String backgroundHex = properties.getProperty("backgroundColor", "0x000000");
        String inputLocation = properties.getProperty("inputLocation", "./target/data.json");
        
        outputDirectory = properties.getProperty("outputDirectory", "./frames");
        long time = System.currentTimeMillis();

        backDropRenderer = new BackDropFrameRenderer(width, height, "/home/glloyd/projects/history_hackday/geo-vid-gen/target/backdrop.png");
        provider = new JsonInputGEPProvider(inputLocation);

        factory = new StatefulBlobFactoryImpl(width, height);
        eventRenderer = new GenerationalFrameRenderer(width, height, factory);
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
            
            renderToFile(pages, currentFrame);
            currentFrame++;
        }
        
        // pad ending
        for (int i = 0; i < 50; i++) {
            List<GeoEventPage> empty = Collections.emptyList();
            renderToFile(empty, currentFrame++);
        }
        
        long finishTime = System.currentTimeMillis();
        System.out.println("Done");
        System.out.println("Took " + (finishTime - time) + "ms");
    }

    /**
     * Process and return all the required overlays.
     * 
     * @param pages
     * @return
     */
    private static List<BufferedImage> getOverlays(List<GeoEventPage> pages) {
        eventRenderer.renderNextFrame(pages);
        
        int year = provider.getCurrentYear();
        factory.renderNextFrame(year);
        System.out.println("Current year " + year);
        yearRenderer.renderNextFrame(year);

        ArrayList<BufferedImage> overlays = new ArrayList<BufferedImage>();
        overlays.add(backDropRenderer.getCurrentFrame());
        overlays.add(eventRenderer.getCurrentFrame());
        overlays.add(factory.getCurrentFrame());
        overlays.add(yearRenderer.getCurrentFrame());
        return overlays;
    }


    /**
     * Output successive frames to png files. 
     * 
     * @param pages
     * @param currentFrame
     * @throws IOException
     */
    private static void renderToFile(List<GeoEventPage> pages, int currentFrame) throws IOException {
        compositeRenderer.renderNextFrame(getOverlays(pages));
        
        RenderedImage image = compositeRenderer.getCurrentFrame();

        String frameNumber = String.format("%05d", currentFrame);
        ImageIO.write(image, "png", new File(outputDirectory + "/frame" + frameNumber + ".png"));
    }
    
    
    /**
     * basically here for "how does it look" debugging,
     * this method draws repeatedly to an ImageComponent
     * in a JFrame. 
     */
    private static JFrame jframe = null;
    private static ImageComponent imageComponent = null;
    private static void renderToScreen(List<GeoEventPage> pages, int currentFrame) {
        if (jframe == null || imageComponent == null) {
            imageComponent = new ImageComponent(width, height);
            jframe = new JFrame();
            jframe.add(imageComponent);
            jframe.pack();
            jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            jframe.setVisible(true);
        }
        
        List<BufferedImage> overlays = getOverlays(pages);
        compositeRenderer.renderNextFrame(overlays);
        imageComponent.setCurrentFrame(compositeRenderer.getCurrentFrame());
        imageComponent.repaint();
    }
    
    /**
     * Stupid component to support rendering to screen.
     */
    private static class ImageComponent extends Component {
        private BufferedImage current;
        int width;
        int height;
        
        public ImageComponent(int width, int height) {
            this.width = width;
            this.height = height;
        }
        
        @Override
        public void paint(Graphics g) {
            g.drawImage(current, 0, 0, null);
        }
        
        @Override
        public Dimension getPreferredSize() {
            return new Dimension(width, height);
        }
        
        public void setCurrentFrame(BufferedImage current) {
            this.current = current;
        }
    }
}
