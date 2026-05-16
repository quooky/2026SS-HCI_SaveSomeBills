package com.example.savesomebills.list_and_group;

import java.util.ArrayList;
import java.util.List;

public class Group {
    List<Integer> id;
    String icon;
    String name;
    String energy_amount;
    int groupid;

    public Group(List<Integer> id, String icon, String name, String energy_amount,int groupid) {
        this.id = id;

        this.icon = icon;
        this.name = name;
        this.energy_amount = energy_amount;
        this.groupid =  groupid;
    }

    public int getGroupid() {
        return groupid;
    }

    public List<Integer> getId() {
        return id;
    }

    public String getIcon() {
        return icon;
    }

    public String getName() {
        return name;
    }

    public String getEnergy_amount() {
        return energy_amount;
    }
}
