package com.google.firebase.example.fireeats.model;

import com.google.android.gms.maps.model.Polyline;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsRoute;

import java.io.Serializable;

public class PolylineData {
    private Polyline polyline;
    private DirectionsLeg directionsLeg;
    private DirectionsRoute route;

    public PolylineData(Polyline polyline, DirectionsLeg directionsLeg) {
        this.polyline = polyline;
        this.directionsLeg = directionsLeg;
    }

    public Polyline getPolyline() {
        return polyline;
    }

    public void setPolyline(Polyline polyline) {
        this.polyline = polyline;
    }

    public DirectionsLeg getDirectionsLeg() {
        return directionsLeg;
    }

    public void setDirectionsLeg(DirectionsLeg directionsLeg) {
        this.directionsLeg = directionsLeg;
    }

    public DirectionsRoute getRoute() {
        return route;
    }

    public void setRoute(DirectionsRoute route) {
        this.route = route;
    }
}
