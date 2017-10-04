package com.example.alexander.trams;

/**
 * Created by Alexander on 09.09.2017.
 */

public class Tram extends Transport{
    Tram(Integer id, Integer time, Integer distance) {
        super(id, time, distance);
        setTimeTransport(time);
        setIdTransport(id);
        setDistanceTransport(distance);
    }
}
