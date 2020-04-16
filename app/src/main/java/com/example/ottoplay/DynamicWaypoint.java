package com.example.ottoplay;

public class DynamicWaypoint extends Waypoint {
    private String lastUpdateTime;

    DynamicWaypoint(int ownerId) {
        this.ownerUserId = ownerId;
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


