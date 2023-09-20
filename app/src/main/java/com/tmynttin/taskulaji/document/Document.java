package com.tmynttin.taskulaji.document;

import android.content.Context;

import com.tmynttin.taskulaji.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Document {
    public String creator;
    public String[] editors;
    private String formID;
    private String secureLevel;
    private String publicityRestrictions;
    public GatheringEvent gatheringEvent;
    public Gathering[] gatherings;

    public Document() {
        creator = "";
        editors = new String[0];
        formID = "JX.519";
        setSecureLevel("none");
        setPublicityRestrictions("Public");
        gatherings = new Gathering[0];
    }

    public void setSecureLevel(String level) {
        switch(level) {
            case "none":
                secureLevel = "MX.secureLevelNone";
                break;
            case "KM10":
                secureLevel = "MX.secureLevelKM10";
                break;
            default:
                secureLevel = "MX.secureLevelKM10";
        }
    }

    public void setPublicityRestrictions(String level) {
        switch(level) {
            case "Public":
                publicityRestrictions = "MZ.publicityRestrictionsPublic";
                break;
            case "Protected":
                publicityRestrictions = "MZ.publicityRestrictionsProtected";
                break;
            case "Private":
                publicityRestrictions = "MZ.publicityRestrictionsPrivate";
                break;
            default:
                publicityRestrictions = "MZ.publicityRestrictionsPrivate";
        }
    }

    public void setCreator(String creator) {
        this.creator = creator;
        addEditor(creator);

    }

    public void addEditor(String editor) {
        List<String> editorsList = new ArrayList<String>();//Arrays.asList(editors);
        Collections.addAll(editorsList, editors);
        editorsList.add(editor);
        editors = editorsList.toArray(new String[editorsList.size()]);
    }

    public void setGatheringEvent(GatheringEvent gatheringEvent) {
        this.gatheringEvent = gatheringEvent;
    }

    public void addGathering(Gathering gathering) {
        List<Gathering> gatheringList = new ArrayList<Gathering>();//Arrays.asList(gatherings);
        Collections.addAll(gatheringList, gatherings);
        gatheringList.add(gathering);
        gatherings = gatheringList.toArray(new Gathering[gatheringList.size()]);
    }

}
