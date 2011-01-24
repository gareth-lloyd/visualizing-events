package com.heychinaski.historyhack.provider;

import java.util.List;
import java.util.Map;

import com.heychinaski.historyhack.model.GeoEventPage;

public interface GeoEventPageProvider {
    public List<GeoEventPage> getNextFrame();
    
    public boolean hasMoreEvents();
    
    public int getCurrentYear();

    public Map<Integer, List<GeoEventPage>> allPages();
}
