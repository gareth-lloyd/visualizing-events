package com.heychinaski.historyhack.displayobjects;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import com.heychinaski.historyhack.model.GeoEventPage;

public class StatefulBlobFactoryImpl implements StatefulBlobFactory {

    List<Color> colors = new ArrayList<Color>();
    static final int EXPECTED_LOWEST_YEAR = -499;
    static final int EXPECTED_HIGHEST_YEAR = 2011;
    
    public StatefulBlobFactoryImpl() {
        colors.add(e);
    }
    
    public StatefulBlob createStatefulBlob(GeoEventPage page, Point point) {
        Color color = getColorFromYear(page.getYear());
        return new StatefulBlobImpl(point, color);
    }

    private Color getColorFromYear(int year) {
        // TODO Auto-generated method stub
        return null;
    }

}
