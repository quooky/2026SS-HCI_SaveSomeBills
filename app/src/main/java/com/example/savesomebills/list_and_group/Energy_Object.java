package com.example.savesomebills.list_and_group;

import android.widget.Button;

import java.util.List;

public class Energy_Object {

    String icon;
    String name;
    String energy_amount;
    int id;
    String usage;

    public Energy_Object(String icon, String name, String energy_amount,int id, String usage) {
        this.icon = icon;
        this.name = name;
        this.energy_amount = energy_amount;
        this.id = id;
        this.usage = usage;
    }

    public String getUsage() {
        return usage;
    }

    public int getId() {
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
