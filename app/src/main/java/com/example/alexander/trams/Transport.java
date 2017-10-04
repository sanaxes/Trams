package com.example.alexander.trams;

/**
 * Created by Alexander on 11.09.2017.
 */

public class Transport {
    private Integer id, time, distance;

    public Transport(Integer id, Integer time, Integer distance){
        this.setDistanceTransport(distance);
        this.setIdTransport(id);
        this.setTimeTransport(time);
    }

    public Integer getIdTransport() {
        return this.id;
    }

    protected void setIdTransport(Integer id) {
        this.id = id;
    }

    public Integer getTimeTransport() {
        return this.time;
    }

    protected void setTimeTransport(Integer time) {
        this.time = time;
    }

    public Integer getDistanceTransport() {
        return this.distance;
    }

    protected void setDistanceTransport(Integer distanceTransport) {
        this.distance = distanceTransport;
    }
}
