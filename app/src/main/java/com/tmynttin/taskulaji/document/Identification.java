package com.tmynttin.taskulaji.document;

import java.io.Serializable;

public class Identification implements Serializable {
    public String taxon;
    //public String taxonID;

    public Identification(String taxon) { //}, String taxonID) {
        this.taxon = taxon;
        //this.taxonID = taxonID;
    }
}
