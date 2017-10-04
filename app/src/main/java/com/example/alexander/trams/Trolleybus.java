package com.example.alexander.trams;

public class Trolleybus  extends Transport {

        Trolleybus(Integer id, Integer time, Integer distance) {
            super(id, time, distance);
            setTimeTransport(time);
            setIdTransport(id);
            setDistanceTransport(distance);
        }
}
