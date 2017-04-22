package com.example.grim.tutmap;

import java.util.List;

/**
 * Created by Grim on 07.02.2016.
 */
public class RouteResponse {
    public List <Route> routes;

    public String getPoints() {
        return this.routes.get(0).overview_polyline.points;
    }
    class Route {
        OverviewPolyline overview_polyline;
    }
    class OverviewPolyline {
        String points;
    }
}
