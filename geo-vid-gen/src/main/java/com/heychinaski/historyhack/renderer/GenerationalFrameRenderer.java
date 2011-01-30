package com.heychinaski.historyhack.renderer;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.heychinaski.historyhack.GraphicsUtils;
import com.heychinaski.historyhack.displayobjects.StatefulBlob;
import com.heychinaski.historyhack.displayobjects.StatefulBlobFactory;
import com.heychinaski.historyhack.model.GeoEventPage;

public class GenerationalFrameRenderer implements FrameRenderer<List<GeoEventPage>> {
    BufferedImage imageWithRetireesOnly;
    Graphics2D retireesImageG2d;
    private List<StatefulBlob> blobsToRetire;
    private List<StatefulBlob> blogsToDraw;
    
    private BufferedImage currentFrame;

    private int width;

    private int height;

    private StatefulBlobFactory blobFactory;
    
    /**
     * 
     * @param yearEvents ALL year events
     */
    public GenerationalFrameRenderer(int width, int height, StatefulBlobFactory blobFactory) {
        this.width = width;
        this.height = height;
        this.blobFactory = blobFactory;
        blogsToDraw = new LinkedList<StatefulBlob>();
        blobsToRetire = Collections.emptyList();
        
        // initialize images
        imageWithRetireesOnly = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        retireesImageG2d = imageWithRetireesOnly.createGraphics();
        
        GraphicsUtils.scaleGraphics2D(retireesImageG2d, width, height);
        currentFrame = GraphicsUtils.deepCopy(imageWithRetireesOnly);
    }
    
    public BufferedImage getCurrentFrame() {
        return currentFrame;
    }

    public void renderNextFrame(List<GeoEventPage> incomingEvents) {
        // draw only the retirees onto the stored image
        for (StatefulBlob blob : blobsToRetire) {
            blob.draw(retireesImageG2d);
        }
        blobsToRetire = Collections.emptyList();
        
        // copy this before drawing others
        currentFrame = GraphicsUtils.deepCopy(imageWithRetireesOnly);
        Graphics2D currentg2d = currentFrame.createGraphics();
        GraphicsUtils.scaleGraphics2D(currentg2d, width, height);
        
        for (GeoEventPage page : incomingEvents) {
            Point point = GraphicsUtils.pointFromLatLong(page.getLatitude(), page.getLongitude());
            blogsToDraw.add(blobFactory.createStatefulBlob(page, point));
        }
        
        // draw the rest on top of the retired blobs. Any blobs that will
        // draw their last state next time are ready to retire.
        Iterator<StatefulBlob> it = blogsToDraw.iterator();
        List<StatefulBlob> willRetireNextFrame = new ArrayList<StatefulBlob>();
        while (it.hasNext()) {
            StatefulBlob blob = it.next();
            blob.draw(currentg2d);
            if(blob.willDrawFinalState()) {
                it.remove();
                willRetireNextFrame.add(blob);
            }
        }
        blobsToRetire = willRetireNextFrame;
        currentg2d.dispose();
    }
}
