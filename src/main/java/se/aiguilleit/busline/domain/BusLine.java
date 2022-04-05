package se.aiguilleit.busline.domain;

import java.util.Set;

public class BusLine {

    private int lineNumber;
    private Set<Stop> stops;


    public BusLine(int lineNumber, Set<Stop> stops) {
        this.lineNumber = lineNumber;
        this.stops = stops;
    }


    public int getLineNumber() {
        return lineNumber;
    }


    public Set<Stop> getStops() {
        return stops;
    }
}
