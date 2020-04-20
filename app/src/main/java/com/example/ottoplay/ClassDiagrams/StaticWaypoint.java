package com.example.ottoplay.ClassDiagrams;

import com.example.ottoplay.ClassDiagrams.Waypoint;

import java.util.Date;
import java.util.HashSet;

public class StaticWaypoint extends Waypoint {
    private Date creationDate;

    //set of user Ids that can edit this waypoint
    private HashSet<Integer> editSet;

    public StaticWaypoint(int wpId, int ownerId, String wpName, EditingSetting es, VisibilitySetting vs, String location) {
        this.globalId = wpId;
        this.ownerUserId = ownerId;
        this.waypointName = wpName;
        this.editSetting = es;
        this.visSetting = vs;
        this.location = location;

        editSet = new HashSet<>();
        creationDate = new Date();
    }

    public StaticWaypoint() {
        editSetting = EditingSetting.SOLO;
        visSetting = VisibilitySetting.HIDDEN;
        //editSet.add(ownerUserId);
        editSet = new HashSet<>();
        creationDate = new Date();
    }

    public void setEditSetting(EditingSetting es) {
        this.editSetting = es;
    }

    public EditingSetting getEditSetting() {
        return this.editSetting;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date date) {
        this.creationDate = date;
    }

    public boolean canEdit(int userId){
        return editSet.contains(userId);
    }

    public void addEditor(int userId) {
        editSet.add(userId);
    }

    public void removeEditor(int userId) {
        editSet.remove(userId);
    }

}
