package se.aiguilleit.busline.domain;

import java.util.Objects;

public class Stop {

    private int stopNumber;
    private int stopAreaNumber;
    private String name;


    public Stop(int stopNumber, int stopAreaNumber, String name) {
        this.stopNumber = stopNumber;
        this.stopAreaNumber = stopAreaNumber;
        this.name = name;
    }


    public int getStopNumber() {
        return stopNumber;
    }


    public int getStopAreaNumber() {
        return stopAreaNumber;
    }


    public String getName() {
        return name;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stop stop = (Stop) o;
        return stopNumber == stop.stopNumber;
    }


    @Override
    public int hashCode() {
        return Objects.hash(stopNumber);
    }
}
