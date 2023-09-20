package com.tmynttin.taskulaji.document;

import android.util.Log;

import com.tmynttin.taskulaji.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Unit implements Serializable {
    private static final String TAG = "Unit";
    public String recordBasis;
    public Identification[] identifications;
    public String count;
    public String taxonConfidence;
    public String notes;
    public UnitFact unitFact;
    public String[] images;


    public Unit() {
        recordBasis = "MY.recordBasisHumanObservation";
        count = "1";
        taxonConfidence = "MY.taxonConfidenceSure";
        notes = "";
        images = new String[0];
    }

    public void setIdentification(String taxon, String taxonID) {
        Identification identification = new Identification(taxon); //, taxonID);
        identifications = new Identification[]{identification};
        unitFact = new UnitFact(taxonID);
    }

    public void addImage(String imageId) {
        List<String> imagesList = new ArrayList<String>();
        Collections.addAll(imagesList, images);
        imagesList.add(imageId);
        images = imagesList.toArray(new String[imagesList.size()]);
    }

    public void setRecordBasis(int index) {
        recordBasis = Helpers.getProperty(R.raw.record_basis, index);
        Log.d(TAG, "setRecordBasis: " + recordBasis);
    }

    public void setTaxonConfidence(int index) {
        taxonConfidence = Helpers.getProperty(R.raw.taxon_confidence, index);
        Log.d(TAG, "setTaxonConfidence: " + taxonConfidence);
    }

}
