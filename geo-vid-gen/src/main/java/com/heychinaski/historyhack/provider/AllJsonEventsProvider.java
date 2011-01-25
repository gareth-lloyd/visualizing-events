package com.heychinaski.historyhack.provider;

import java.util.ArrayList;
import java.util.List;

import com.heychinaski.historyhack.model.GeoEventPage;

public class AllJsonEventsProvider extends JsonInputGEPProvider {
    
    public AllJsonEventsProvider(String filename) {
        super(filename);
    }
    
    public List<GeoEventPage> getAllAsList() {
        List<GeoEventPage> allPages = new ArrayList<GeoEventPage>();
        while (this.hasMoreEvents()) {
            allPages.addAll(getNextFrame());
        }
        return allPages;
    }

}
