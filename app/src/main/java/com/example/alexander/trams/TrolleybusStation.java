package com.example.alexander.trams;

public class TrolleybusStation extends Station{

    TrolleybusStation(Integer id, String name) {
        super(id, name);
        SetIdStation(id);
        SetNameStation(name);
        SetTypeStation("Trolleybus");
    }
}
