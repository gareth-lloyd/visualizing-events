package com.heychinaski.historyhack.experimental;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

public class StatefulBlob {

    public static final BlobState[] STATES = {
        new BlobState(0.5f, 200, Color.YELLOW),
        new BlobState(0.4f, 500, Color.YELLOW),
        new BlobState(0.3f, 750, Color.YELLOW),
        new BlobState(0.2f, 950, Color.WHITE),
        new BlobState(0.1f, 1100, Color.WHITE),
        new BlobState(0.1f, 1200, Color.WHITE),
        new BlobState(0.1f, 1300, Color.WHITE),
        new BlobState(0.05f, 1400, Color.WHITE),
        new BlobState(0.05f, 1500, Color.WHITE),
        new BlobState(0.05f, 1600, Color.WHITE)
    };
    public static final int TOTAL_STATES = STATES.length;
    
    private int currentState;
    private Point point;
    
    public StatefulBlob(Point point) {
        this.point = point;
        this.currentState = 0;
    }
    
    public void drawAndIncrementState(Graphics2D g2) {
        BlobState state = null;
        if (currentState >= TOTAL_STATES) {
            state = STATES[TOTAL_STATES - 1];
        }
        else {
            state = STATES[currentState];
        }
        g2.setColor(state.color);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, state.alpha));
        int radius = state.radius;
        g2.fillOval(point.x - radius, point.y - radius, radius * 2, radius * 2);
        
        currentState++;
    }
    
    public boolean isRetired() {
        return currentState == TOTAL_STATES;
    }
    
    static class BlobState {
        float alpha;
        int radius;
        Color color;
        
        public BlobState(float alpha, int radius, Color color) {
            this.alpha = alpha;
            this.radius = radius;
            this.color = color;
        }
        
    }
}
