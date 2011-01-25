package com.heychinaski.historyhack.provider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.heychinaski.historyhack.model.GeoEventPage;

public class JsonInputGEPProvider implements GeoEventPageProvider {
    
    protected String currentLine = "";
    protected String nextLine = null;
    protected BufferedReader reader;
    
    protected int currentYear = -1;

    public JsonInputGEPProvider(String filename) {
        try {
            File input = new File(filename);
            reader = new BufferedReader(new FileReader(input));
    
            readNextLine();
        } catch(Exception e) {
            throw new RuntimeException("Couldn't parse file", e);
        }

    }

    protected void readNextLine() {
        try {
            nextLine = reader.readLine();
        } catch(Exception e) {
            throw new RuntimeException("Couldn't read line from file", e);
        }
    }
    
    protected GeoEventPage pageFromJson(String line) {
        return new Gson().fromJson(nextLine, GeoEventPage.class); 
    }
    
    @Override
    public List<GeoEventPage> getNextFrame() {
        
        List<GeoEventPage> pages = new ArrayList<GeoEventPage>();
        
        GeoEventPage nextGeoEventPage = pageFromJson(nextLine);
        GeoEventPage initialGeoEventPage = nextGeoEventPage;
        
        int year;
        if(initialGeoEventPage != null) {
            year = initialGeoEventPage.getYear();
        } else {
            // empty list of pages
            return pages;
        }
        
        // The page that has come back has a year in advance of the
        // anticipated next year: there was a gap in the data. 
        if(year - currentYear > 1) {
            currentYear ++;
            return pages;
        } else {
            currentYear = year;
        }
        
        // Stop when we run out or change years
        while(nextGeoEventPage != null && initialGeoEventPage.getYear() == nextGeoEventPage.getYear()) {
            pages.add(nextGeoEventPage);
            
            readNextLine();
            if(nextLine != null) {
                nextGeoEventPage = pageFromJson(nextLine);
            } else {
                nextGeoEventPage = null;
            }
        }
        return pages;
    }

    @Override
    public boolean hasMoreEvents() {
        boolean b = nextLine != null;
        if(!b) {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return b;
    }

    @Override
    public int getCurrentYear() {
        return currentYear;
    }

    @Override
    public Map<Integer, List<GeoEventPage>> allPages() {
        Map<Integer, List<GeoEventPage>> allPages = new HashMap<Integer, List<GeoEventPage>>();
        while (this.hasMoreEvents()) {
            List<GeoEventPage> pages = getNextFrame();
            allPages.put(getCurrentYear(), pages);
        }
        System.out.println(allPages.size());
        System.out.println(allPages.get(1865).size());
        return allPages;
    }

}
