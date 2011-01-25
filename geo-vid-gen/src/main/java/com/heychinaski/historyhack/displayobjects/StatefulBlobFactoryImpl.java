package com.heychinaski.historyhack.displayobjects;

import java.awt.Point;

import com.heychinaski.historyhack.model.GeoEventPage;

public class StatefulBlobFactoryImpl implements StatefulBlobFactory {

    public StatefulBlob createStatefulBlob(GeoEventPage page, Point point) {
        // TODO: add any necessary logic to analyze page contents
        // and decide what to render in response.
        return new StatefulBlobImpl(point);
    }

}
