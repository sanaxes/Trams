package com.example.alexander.trams;

public class Station {
    private Integer idStation;
    private String nameStation;
    private String typeStation;

    Station(Integer id, String name) {
        this.SetIdStation(id);
        this.SetNameStation(name);
    }

    protected void SetTypeStation(String type) { this.typeStation = type;}

    public String getTypeStation() {return this.typeStation;}

    protected void SetIdStation(Integer id) {
        this.idStation = id;
    }

    public Integer getIdStation() {
        return this.idStation;
    }

    protected void SetNameStation(String name) {
        this.nameStation = name;
    }

    public String getNameStation() {
        return this.nameStation;
    }
}

