package com.heychinaski.historyhack.provider;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.heychinaski.historyhack.model.GeoEventPage;
import com.heychinaski.historyhack.renderer.CumulativeGeoFrameRenderer;

public class RandomGeoEventPageProvider implements GeoEventPageProvider {

    private int currentCycle = 0;

    private int maxFrames;

    private List<Point2D.Double> seedPoints = new ArrayList<Point2D.Double>();

    public RandomGeoEventPageProvider(int startFrame, int maxFrames) {
        super();
        currentCycle = startFrame;
        this.maxFrames = maxFrames;


        for(int i = 0; i < 10; i++) {
            addNewSeedPoint();
        }
    }

    private Point2D.Double addNewSeedPoint() {
        double longitude = (Math.random() * CumulativeGeoFrameRenderer.LONGITUDE_DEGREES) - (CumulativeGeoFrameRenderer.LONGITUDE_DEGREES / 2);
        double latitude = (Math.random() * CumulativeGeoFrameRenderer.LATITUDE_DEGREES) - (CumulativeGeoFrameRenderer.LATITUDE_DEGREES / 2);
        Point2D.Double seedPoint = new Point2D.Double(longitude, latitude);
        seedPoints.add(seedPoint);

        return seedPoint;
    }

    @Override
    public List<GeoEventPage> getNextFrame() {
        List<GeoEventPage> list = new ArrayList<GeoEventPage>();
        
        if(Math.random() <= 0.9) {
            currentCycle ++;
            return list;
        }

        for(int j = 0; j < 5; j++) {
            Point2D.Double newPoint;
            if(Math.random() <= 0.05) {
                newPoint = addNewSeedPoint(); 
            } else {
                Point2D.Double randomSeedPoint = seedPoints.get((int)(Math.random() * seedPoints.size()));
                newPoint = new Point.Double(randomSeedPoint.x,randomSeedPoint.y);

                double distance = (Math.random() * ((CumulativeGeoFrameRenderer.LONGITUDE_DEGREES / 10)));
                newPoint.x += (Math.random() * distance) - (distance/2);
                newPoint.y += (Math.random() * distance) - (distance/2);
            }

            GeoEventPage randomPage = new GeoEventPage(newPoint.x, newPoint.y, currentCycle, 0, 0, 0);
            list.add(randomPage);
        }

        currentCycle ++;
        return list;
    }

    @Override
    public boolean hasMoreEvents() {
        return currentCycle <= maxFrames;
    }

    public static void main(String[] args) throws IOException {
        try{
            // Create file 
            FileWriter fstream = new FileWriter("input.json");
            BufferedWriter out = new BufferedWriter(fstream);
            
            GeoEventPageProvider provider = new RandomGeoEventPageProvider(-1000, 2012);
            while(provider.hasMoreEvents()) {
                List<GeoEventPage> pages = provider.getNextFrame();
                if(pages.size() > 0) {
                    for(GeoEventPage page : pages) {
                        String json = new Gson().toJson(page);
                        out.write(json + "\n");
                    }
                }
            }
            
            //Close the output stream
            out.close();
        }catch (Exception e){//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
    }

    @Override
    public int getCurrentYear() {
        return currentCycle;
    }

}
