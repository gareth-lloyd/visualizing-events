package com.heychinaski.historyhack;

import java.awt.Point;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

/**
 * Hello world!
 * 
 */
public class App {
    private static final int WIDTH = 640;
    private static final int HEIGHT = 512;

    public static void main(String[] args) throws IOException {
        FrameRenderer renderer = new FrameRenderer(WIDTH, HEIGHT);

        for(int i = 0; i < 100; i++) {
            ArrayList<Point> points = new ArrayList<Point>();
            for(int j = 0; j < 10000; j++) {
                Point randomPoint = new Point((int)(Math.random() * WIDTH), (int)(Math.random() * HEIGHT));
                points.add(randomPoint);
            }
            renderer.renderNextFrame(points);
            
            RenderedImage image = renderer.getCurrentFrame();
            ImageIO.write(image, "png", new File("frames/frame" + i + ".png"));    
        }
    }
}
