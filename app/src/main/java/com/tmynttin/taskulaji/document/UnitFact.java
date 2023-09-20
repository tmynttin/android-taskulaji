package com.tmynttin.taskulaji.document;

import java.io.Serializable;

public class UnitFact implements Serializable {
    public String autocompleteSelectedTaxonID;

    public UnitFact(String taxonID) {
        autocompleteSelectedTaxonID = taxonID;
    }
}
