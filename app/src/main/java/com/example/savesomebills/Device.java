package com.example.savesomebills;

import java.util.UUID;

public class Device {
    public String objectId;
    public String groupId;
    public String name;
    public int onHours;
    public int standbyHours;
    public int wattOn;
    public int wattStandby;

    public Device(String name, String groupId, int onHours, int standbyHours, int wattOn, int wattStandby) {
        this.objectId = UUID.randomUUID().toString();
        this.groupId = groupId;
        this.name = name;
        this.onHours = onHours;
        this.standbyHours = standbyHours;
        this.wattOn = wattOn;
        this.wattStandby = wattStandby;
    }
}
