package com.example.savesomebills;

import java.util.UUID;

public class Device {
    public String objectId;
    public String groupId;
    public String name;
    public String icon;
    public int onHours;
    public int standbyHours;
    public int wattOn;
    public int wattStandby;
    public String usage;
    public String cost;

    public Device(String name, String groupId, int onHours, int standbyHours, int wattOn, int wattStandby, String icon,String usage,String cost) {
        this.objectId = UUID.randomUUID().toString();
        this.groupId = groupId;
        this.name = name;
        this.icon = icon;
        this.onHours = onHours;
        this.standbyHours = standbyHours;
        this.wattOn = wattOn;
        this.wattStandby = wattStandby;
        this.usage = usage;
        this.cost = cost;
    }
}
