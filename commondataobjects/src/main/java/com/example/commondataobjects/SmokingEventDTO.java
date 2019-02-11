package com.example.commondataobjects;


import java.io.Serializable;

public class SmokingEventDTO implements Serializable {
    private int id;
    private String test;
    private String startDate;
    private String startTime;
    private String stopDate;
    private String stopTime;
    private boolean eventConfirmed;
    private boolean isSyncLabel;
    private boolean removed;

    public SmokingEventDTO (int id, String test, String startDate, String startTime,
                            String stopDate, String stopTime, boolean eventConfirmed,
                            boolean isSyncLabel, boolean removed){
        this.id = id;
        this.test = test;
        this.startDate = startDate;
        this.startTime = startTime;
        this.stopDate = stopDate;
        this.stopTime = stopTime;
        this.eventConfirmed = eventConfirmed;
        this.isSyncLabel = isSyncLabel;
        this.removed = removed;
    }

    public String getTest() {
        return test;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getStopDate() {
        return stopDate;
    }

    public String getStopTime() {
        return stopTime;
    }

    public boolean isEventConfirmed() {
        return eventConfirmed;
    }

    public boolean isSyncLabel() {
        return isSyncLabel;
    }

    public boolean isRemoved() {
        return removed;
    }
}
