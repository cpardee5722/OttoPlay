package com.example.ottoplay.ClassDiagrams;

public class DynamicWaypoint extends Waypoint {
    private String lastUpdateTime;

    public DynamicWaypoint(int wpId, int ownerId, String wpName) {
        this.globalId = wpId;
        this.ownerUserId = ownerId;
        this.waypointName = wpName;
        super.editSetting = EditingSetting.SOLO;
        super.visSetting = VisibilitySetting.PRIVATE;
    }

    public String getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void updateLocation() {
        //TODO
        //change lastUpdateTime
    }
}


