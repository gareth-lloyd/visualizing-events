package com.heychinaski.historyhack.displayobjects;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;


public class StatefulBlobImpl implements StatefulBlob {

    static final Color CENTRAL_CIRCLE_CLR = Color.decode("0xa0d9c3");
    static final int C_RADIUS = 800;
    static final float C_ALPHA = 0.2f; 
    
    public static final BlobState[] DEFAULT_STATES = {
        new BlobState(0.8f, 800, Color.decode("0x68a3ac")),
        new BlobState(0.8f, 1000, Color.decode("0x68a3ac")),
        new BlobState(0.8f, 1200, Color.decode("0x68a3ac")),
        new BlobState(0.75f, 1400, Color.decode("0x68a3ac")),
        new BlobState(0.7f, 1600, Color.decode("0x68a3ac")),
        new BlobState(0.65f, 1800, Color.decode("0x68a3ac")),
        new BlobState(0.6f, 2000, Color.decode("0x68a3ac")),
        new BlobState(0.5f, 2200, Color.decode("0x68a3ac")),
        new BlobState(0.45f, 2400, Color.decode("0x68a3ac")),
        new BlobState(0.4f, 2600, Color.decode("0x68a3ac")),
        new BlobState(0.35f, 2800, Color.decode("0x68a3ac")),
        new BlobState(0.25f, 3000, Color.decode("0x68a3ac")),
        new BlobState(0.15f, 3200, Color.decode("0x68a3ac")),
        new BlobState(0.1f, 3400, Color.decode("0x68a3ac")),
        new BlobState(0.08f, 3600, Color.decode("0x68a3ac")),
        new BlobState(0.05f, 3800, Color.decode("0x68a3ac")),
        new BlobState(0.02f, 4000, Color.decode("0x68a3ac"))
    };
    
    private int currentState;
    private Point point;
    private Color color;
    
    public StatefulBlobImpl(Point point, Color color) {
        this.point = point;
        this.currentState = 0;
        this.color = color;
    }
    
    public void draw(Graphics2D g2) {
        BlobState state = null;
        if (willDrawFinalState())
            state = DEFAULT_STATES[DEFAULT_STATES.length - 1];
        else 
            state = DEFAULT_STATES[currentState];
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // draw central part
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, C_ALPHA));
        g2.setColor(state.getColor());
        g2.fillOval(point.x - C_RADIUS, point.y - C_RADIUS, C_RADIUS * 2, C_RADIUS * 2);
        
        // draw ring
        if (!willDrawFinalState()) {
            int radius = state.getRadius();
//            int radius = state.getRadius() + 400;

            g2.setStroke(new BasicStroke(500));
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, state.getAlpha()));
            g2.drawOval(point.x - radius, point.y - radius, radius * 2, radius * 2);
            g2.setStroke(new BasicStroke(2000));
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, state.getAlpha() / 2));
            g2.drawOval(point.x - radius, point.y - radius, radius * 2, radius * 2);
            currentState++;
        }
    }
    
    public boolean willDrawFinalState() {
        return currentState == DEFAULT_STATES.length;
    }
    
}
