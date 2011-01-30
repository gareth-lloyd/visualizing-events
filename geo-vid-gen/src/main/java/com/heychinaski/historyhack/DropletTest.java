package com.heychinaski.historyhack;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import com.heychinaski.historyhack.displayobjects.StatefulBlob;
import com.heychinaski.historyhack.displayobjects.StatefulBlobFactoryImpl;
import com.heychinaski.historyhack.displayobjects.StatefulBlobImpl;
import com.heychinaski.historyhack.model.GeoEventPage;
import com.heychinaski.historyhack.renderer.BackDropFrameRenderer;
import com.heychinaski.historyhack.renderer.CompositeFrameRenderer;
import com.heychinaski.historyhack.renderer.GenerationalFrameRenderer;

/**
 * Quick class to allow me to test out various blob shapes
 * etc.
 * 
 * @author glloyd
 *
 */
public class DropletTest {
    public static final int WIDTH = 1600;
    public static final int HEIGHT = 800;
    
    
    public static void main(String[] args) {
        JFrame jframe = new JFrame();
        Compositer compositer = new Compositer();
        jframe.add(compositer);
        jframe.pack();
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jframe.setVisible(true);
        
        
        StatefulBlob sb = new StatefulBlobImpl(new Point(0, 0), Color.WHITE);
        for (int i = 0; i < 1000; i++) {
            // frame delay
            try {Thread.sleep(500);} catch (InterruptedException e) {}
            
            Graphics2D g2d = null;
            if (sb.willDrawFinalState()) {
                g2d = compositer.getBackdrop().createGraphics();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GraphicsUtils.scaleGraphics2D(g2d, WIDTH, HEIGHT);
                sb.draw(g2d);
                sb = new StatefulBlobImpl(new Point(random(), random()), Color.WHITE);
                g2d.dispose();
            }
            else {
                BufferedImage currentFrame = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_4BYTE_ABGR);
                g2d = currentFrame.createGraphics();
                compositer.setCurrentFrame(currentFrame);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GraphicsUtils.scaleGraphics2D(g2d, WIDTH, HEIGHT);
                sb.draw(g2d);
                g2d.dispose();
            }
            compositer.repaint();
        }
        System.exit(0);
    }
    
    private static int random() {
        Double d = Double.valueOf(Math.random() * 10000.0);
        return d.intValue();
    }

    static class Compositer extends Component {
        BufferedImage backdrop = new BackDropFrameRenderer(WIDTH, HEIGHT, 
                "/home/glloyd/projects/history_hackday/geo-vid-gen/target/backdrop.png").getCurrentFrame();
        private BufferedImage current;
        
        
        @Override
        public void paint(Graphics g) {
            g.drawImage(backdrop, 0, 0, null);
            g.drawImage(current, 0, 0, null);
        }
        
        @Override
        public Dimension getPreferredSize() {
            return new Dimension(backdrop.getWidth(), backdrop.getHeight());
        }
        
        public void setCurrentFrame(BufferedImage current) {
            this.current = current;
        }
        
        public BufferedImage getBackdrop() {
            return backdrop;
        }
    }
    
}
