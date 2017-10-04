package com.example.alexander.trams;

public class TramStation extends Station{

    TramStation(Integer id, String name) {
        super(id, name);
        SetIdStation(id);
        SetNameStation(name);
        SetTypeStation("Tram");
    }
}
