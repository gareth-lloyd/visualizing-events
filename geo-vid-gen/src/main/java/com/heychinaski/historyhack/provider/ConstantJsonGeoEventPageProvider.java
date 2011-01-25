package com.heychinaski.historyhack.provider;

import java.util.ArrayList;
import java.util.List;

import com.heychinaski.historyhack.model.GeoEventPage;

public class ConstantJsonGeoEventPageProvider extends JsonInputGEPProvider {
    
    private int numEvents;

    public ConstantJsonGeoEventPageProvider(String filename, int numEvents) {
        super(filename);
        this.numEvents = numEvents;
    }

    @Override
    public List<GeoEventPage> getNextFrame() {
        List<GeoEventPage> pages = new ArrayList<GeoEventPage>();
        
        GeoEventPage nextGeoEventPage = pageFromJson(nextLine);
        GeoEventPage initialGeoEventPage = nextGeoEventPage;
        
        if(initialGeoEventPage == null) {
            return pages;
        }
        
        // Stop when we run out or reach numEvents
        while(nextGeoEventPage != null && pages.size() < numEvents) {
            pages.add(nextGeoEventPage);
            
            readNextLine();
            if(nextLine != null) {
                nextGeoEventPage = pageFromJson(nextLine);
            } else {
                nextGeoEventPage = null;
            }
        }
        currentYear = pages.get(pages.size() - 1).getYear();
        return pages;
    }

}
