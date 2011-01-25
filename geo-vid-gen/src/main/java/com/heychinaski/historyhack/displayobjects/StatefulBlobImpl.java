package com.heychinaski.historyhack.displayobjects;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;


public class StatefulBlobImpl implements StatefulBlob {

    public static final BlobState[] DEFAULT_STATES = {
        new BlobState(0.7f, 400, Color.RED),
        new BlobState(0.7f, 500, Color.RED),
        new BlobState(0.7f, 600, Color.ORANGE),
        new BlobState(0.7f, 700, Color.ORANGE),
        new BlobState(0.5f, 800, Color.ORANGE),
        new BlobState(0.6f, 850, Color.YELLOW),
        new BlobState(0.4f, 900, Color.YELLOW),
        new BlobState(0.5f, 940, Color.WHITE),
        new BlobState(0.3f, 980, Color.WHITE),
        new BlobState(0.2f, 1000, Color.WHITE),
        new BlobState(0.1f, 1000, Color.WHITE),
        new BlobState(0.05f, 1000, Color.WHITE),
        new BlobState(0.05f, 1000, Color.WHITE)
    };
    
    private int currentState;
    private Point point;
    
    public StatefulBlobImpl(Point point) {
        this.point = point;
        this.currentState = 0;
    }
    
    public void draw(Graphics2D g2) {
        BlobState state = null;
        if (currentState >= DEFAULT_STATES.length) {
            state = DEFAULT_STATES[DEFAULT_STATES.length - 1];
        }
        else {
            state = DEFAULT_STATES[currentState];
        }
        g2.setColor(state.getColor());
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, state.getAlpha()));
        int radius = state.getRadius();
        g2.fillOval(point.x - radius, point.y - radius, radius * 2, radius * 2);
        
        currentState++;
    }
    
    public boolean willDrawFinalState() {
        return currentState == DEFAULT_STATES.length;
    }
    
}
