package com.heychinaski.historyhack.provider;

import java.util.List;

import com.heychinaski.historyhack.model.GeoEventPage;

public interface GeoEventPageProvider {
    public List<GeoEventPage> getNextFrame();
    
    public boolean hasMoreEvents();
    
    public int getCurrentYear();
}
