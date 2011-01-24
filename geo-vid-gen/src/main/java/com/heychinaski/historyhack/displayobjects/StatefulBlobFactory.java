package com.heychinaski.historyhack.displayobjects;

import java.awt.Point;

import com.heychinaski.historyhack.model.GeoEventPage;

/**
 * defines an interface for taking in {@link GeoEventPage} 
 * and returning StatefulBlobs. Implementations can 
 * encapsulate the logic that determines how a {@link GeoEventPage}
 * maps to a {@link StatefulBlob}.  
 * 
 * @author glloyd
 *
 */
public interface StatefulBlobFactory {

    /**
     * use the information in the {@link GeoEventPage}
     * to create a {@link StatefulBlob}.
     * 
     * @param page
     * @param point TODO
     * @return
     */
    public StatefulBlob createStatefulBlob(GeoEventPage page, Point point);
}
