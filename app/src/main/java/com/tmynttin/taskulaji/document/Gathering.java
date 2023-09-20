package com.tmynttin.taskulaji.document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Gathering {
    private Geometry geometry;
    public String municipality;
    public String locality;
    public String localityDescription;
    private String dateBegin;
    private Unit[] units;

    public Gathering() {
        municipality = "";
        locality = "";
        localityDescription = "";
        dateBegin = "";
        units = new Unit[0];
    }

    public void setMunicipality(String municipality) {
        this.municipality = municipality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public void setLocalityDescription(String localityDescription) {
        this.localityDescription = localityDescription;
    }

    public void setDateBegin(String date) {
        dateBegin = date;
    }

    public void addGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public void setUnits(Unit[] units) {
        this.units = units;
    }

    public void addUnit(Unit unit) {
        List<Unit> unitsList = new ArrayList<Unit>();
        Collections.addAll(unitsList, units);
        unitsList.add(unit);
        units = unitsList.toArray(new Unit[unitsList.size()]);
    }
}
