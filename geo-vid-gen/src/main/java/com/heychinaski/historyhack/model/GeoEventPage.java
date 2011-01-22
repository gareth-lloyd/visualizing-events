package com.heychinaski.historyhack.model;

public class GeoEventPage {
    double longitude;
    double latitude;
    
    int year;
    int month;
    int day;
    
    int articleLength;

    public GeoEventPage(double longitude, double latitude, int year, int month, int day, int articleLength) {
        super();
        this.longitude = longitude;
        this.latitude = latitude;
        this.year = year;
        this.month = month;
        this.day = day;
        this.articleLength = articleLength;
    }
    
    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }

    public int getArticleLength() {
        return articleLength;
    }
}
