package com.heychinaski.historyhack.displayobjects;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.heychinaski.historyhack.GraphicsUtils;
import static com.heychinaski.historyhack.GraphicsUtils.RESOLUTION_FACTOR;
import com.heychinaski.historyhack.model.GeoEventPage;
import com.heychinaski.historyhack.renderer.FrameRenderer;

public class StatefulBlobFactoryImpl implements StatefulBlobFactory, FrameRenderer<Integer> {
    private static final int NUM = 25;
    private static final int RADIAL_WIDTH = 40000;
    private int width;
    private int height;
    private BufferedImage currentFrame;
    private Random random = new Random();
    
    private static List<Color> colors = new ArrayList<Color>();
    static {
        colors.add(Color.decode("0xffffff"));
        colors.add(Color.decode("0xdcdb6e"));
        colors.add(Color.decode("0xccefe2"));
        colors.add(Color.decode("0xa0d9c3"));
        colors.add(Color.decode("0x7ec1b1"));
        colors.add(Color.decode("0x68a3ac"));
        colors.add(Color.decode("0x415587"));   
    }
    private List<GeoEventPage> thisEventCohort = new ArrayList<GeoEventPage>();
    
    MovingWeightedAverage xPos = new MovingWeightedAverage(50);
    MovingWeightedAverage yPos = new MovingWeightedAverage(50);
    MovingWeightedAverage xSize = new MovingWeightedAverage(400);
    
    /**
     * Constructor. Set width and height for rendering purposes.
     * @param width
     * @param height
     */
    public StatefulBlobFactoryImpl(int width, int height) {
        this.width = width;
        this.height = height;
        currentFrame = newFrame();
    }

    @Override
    public StatefulBlob createStatefulBlob(GeoEventPage page, Point point) {
        thisEventCohort.add(page);
        return new StatefulBlobImpl(point, colors.get(random.nextInt(colors.size())));
    }

    @Override
    public BufferedImage getCurrentFrame() {
        return currentFrame;
    }

    @Override
    public void renderNextFrame(Integer year) {
        currentFrame = newFrame();
        Graphics2D g2d = currentFrame.createGraphics();
        GraphicsUtils.scaleGraphics2D(g2d, width, height);
        
        // add new EventCohortStats at beginning of list
        EventCohortStats eventCohortStats = new EventCohortStats(thisEventCohort);
        
        xPos.addMeasure(eventCohortStats.averageLongitude);
        yPos.addMeasure(eventCohortStats.averageLatitude);
        xSize.addMeasure(eventCohortStats.horizSize);
        
        int width = xSize.asInt() * RESOLUTION_FACTOR;
        // hack to make it look right
        if (width < 23000 || year < -487)  
            width = 23000;
        // radial gradient should be outside circle
        width += RADIAL_WIDTH * 2;

        int xPosInt = ((xPos.asInt()) * RESOLUTION_FACTOR);
        int yPosInt = -1 * (yPos.asInt() * RESOLUTION_FACTOR);
        
        float[] fractions = getFractions(width);
        Color[] radialColors = new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 100)};
        RadialGradientPaint rgp = new RadialGradientPaint(xPosInt, yPosInt, width, fractions, radialColors);
        
        g2d.setPaint(rgp);
        int ScaledWidth = currentFrame.getWidth() * RESOLUTION_FACTOR;
        int ScaledHeight = currentFrame.getHeight() * RESOLUTION_FACTOR;
        g2d.fillRect(-ScaledWidth / 2, -ScaledHeight / 2, ScaledWidth, ScaledHeight);
        
        g2d.dispose();
    }

    // find the fractions to apply to the gradient taking into account 
    // the width in relation to the desired width of gradient
    private float[] getFractions(int width) {
        int radius = width / 2;
        float proportion = radius / (float) (RADIAL_WIDTH + radius);
        return new float[] {proportion, 1f};
    }

    private BufferedImage newFrame() {
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g2 = bi.createGraphics();
        
        // Following block renders zero alpha to whole image
        g2.setComposite(AlphaComposite.Clear);
        g2.fillRect(0, 0, bi.getWidth(), bi.getHeight());
        g2.dispose();
        return bi;
    }
    
    /**
     * Calculates a weighted average, with the highest
     * weight given to the most recently added measure.
     */
    private class MovingWeightedAverage {
        private LinkedList<Double> measures = new LinkedList<Double>();
        final int maxMeasures;
        private double avg = 0.0;
        
        public MovingWeightedAverage(int maxMeasures) {
            this.maxMeasures = maxMeasures;
        }
        
        public double addMeasure(double measure) {
            measures.addFirst(measure);
            int lim = Math.min(measures.size(), maxMeasures);
            double numerator = 0.0;
            double denominator = 0.0;
            for (int i = 0; i < lim; i++) {
                numerator += (lim - i) * measures.get(i);
                denominator += (lim - i);
            }
            return avg = numerator / denominator;
        }
        
        public double get() {
            return avg;
        }
        
        public int asInt() {
            return (int) Math.round(avg);
        }
    }
    
    /**
     * calculates statistics for one cohort of 
     * {@link GeoEventPage}s,  
     */
    private class EventCohortStats {
        final double averageLatitude;
        final double averageLongitude;
        final double horizSize;
        final double vertSize; 
        
        public EventCohortStats(List<GeoEventPage> cohort) {
            int numEvents = cohort.size();
            if (numEvents == 0) {
                averageLatitude = averageLongitude = horizSize = vertSize = 0.0;
                return;
            }
            double totalLat = 0.0, totalLong = 0.0;
            
            List<Double> lats = new ArrayList<Double>();
            List<Double> longs = new ArrayList<Double>();
            for (GeoEventPage page : cohort) {
                totalLat += page.getLatitude();
                totalLong += page.getLongitude();
                lats.add(page.getLatitude());
                longs.add(page.getLongitude());
            }
            averageLatitude = totalLat / numEvents;
            averageLongitude = totalLong / numEvents;
            
            Collections.sort(longs);
            Collections.sort(lats);
            horizSize = percentile(longs, 80) - percentile(longs, 10);
            vertSize = percentile(lats, 90) - percentile(lats, 10);
        }

        /**
         * return the pth percentile of the inputs. Inputs
         * must be sorted beforehand.
         * @param longs
         * @param p
         * @return
         */
        private double percentile(List<Double> inputs, double p) {
            int numElements = inputs.size();
            if (p > 100.0 || p < 0) {
                throw new IndexOutOfBoundsException();
            }
            if (numElements == 0) {
                return 0.0;
            }
            int i = (int) ((numElements / 100.0) * p);
            if (i == numElements) {
                i -= 1;
            }
            return inputs.get(i);
        }
    }
}
