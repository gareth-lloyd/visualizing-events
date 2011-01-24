package com.heychinaski.historyhack.displayobjects;
import java.awt.Color;

public class BlobState {
    private final float alpha;

    private final int radius;

    private final Color color;

    public BlobState(float alpha, int radius, Color color) {
        this.alpha = alpha;
        this.radius = radius;
        this.color = color;
    }

    public float getAlpha() {
        return alpha;
    }

    public int getRadius() {
        return radius;
    }

    public Color getColor() {
        return color;
    }
}