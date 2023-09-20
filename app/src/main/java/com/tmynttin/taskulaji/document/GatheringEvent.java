package com.tmynttin.taskulaji.document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GatheringEvent {
    private String dateBegin;
    private String[] leg;
    private boolean legPublic;
    private String[] legUserID;
    //private String timeStart;

    public GatheringEvent() {
        this.dateBegin = "";
        this.leg = new String[0];
        this.legPublic = true;
        this.legUserID = new String[0];
        //this.timeStart = "";
    }

    public void setTime(String date, String time) {
        this.dateBegin = date + "T" + time;
        //this.timeStart = time;
    }

    public void setLegPublic(boolean isPublic) {
        this.legPublic = isPublic;
    }

    public void addLeg(String userID) {
        List<String> legList = new ArrayList<String>();
        Collections.addAll(legList, leg);
        legList.add(userID);
        leg = legList.toArray(new String[legList.size()]);

        List<String> legUserIDList = new ArrayList<String>();
        Collections.addAll(legUserIDList, legUserID);
        legUserIDList.add(userID);
        legUserID = legUserIDList.toArray(new String[legUserIDList.size()]);
    }
}