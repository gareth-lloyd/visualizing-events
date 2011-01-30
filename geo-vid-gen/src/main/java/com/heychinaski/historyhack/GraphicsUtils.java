package com.heychinaski.historyhack;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

/**
 * Home for static methods which define the look and feel
 * or provide consistency across a few different classes. 
 * 
 * @author glloyd
 *
 */
public class GraphicsUtils {
    public static final int RESOLUTION_FACTOR = 1000;

    public static final int LONGITUDE_DEGREES = 360;
    public static final int LATITUDE_DEGREES = 180;
    
    public static final int LONGITUDE_DEGREES_MULTIPLIED = LONGITUDE_DEGREES * RESOLUTION_FACTOR;
    public static final int LATITUDE_DEGREES_MULTIPLIED = LATITUDE_DEGREES * RESOLUTION_FACTOR;
    
    /**
     * copy the supplied {@link BufferedImage} to a new 
     * one with the same colour model, alpha
     * @param bi
     * @return
     */
    public static BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        BufferedImage cloned = new BufferedImage(cm, raster, isAlphaPremultiplied, null);
        return cloned;
    }

    
    /**
     * Lat/long measurements in degrees are multiplied by 
     * 1000 to increase the precision with which we can 
     * place them.
     * 
     * A corresponding transformation must be applied to
     * the graphics object which draws points with reference
     * to the image's pixel dimensions. 
     * 
     * The translation means that negative measurements 
     * will be drawn with reference to the centre point of
     * the image.
     */
    public static void scaleGraphics2D(Graphics2D g2d, int width, int height) {
        g2d.scale((float)width / (float)LONGITUDE_DEGREES_MULTIPLIED, (float)height / (float)LATITUDE_DEGREES_MULTIPLIED);
        g2d.translate((LONGITUDE_DEGREES_MULTIPLIED) / 2, (LATITUDE_DEGREES_MULTIPLIED) / 2);
    }

    /**
     * scale latitude and longitude by teh resolution factor and 
     * return corresponding representation of a point in that space.
     * 
     * @param latitude
     * @param longitude
     * @return
     */
    public static Point pointFromLatLong(double latitude, double longitude) {
        return new Point((int)Math.round(longitude * RESOLUTION_FACTOR), 
                -(int)Math.round(latitude * RESOLUTION_FACTOR));
    }
    
}
