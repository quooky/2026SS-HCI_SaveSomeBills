package com.example.savesomebills.list_and_group;

import android.widget.Button;

import java.util.List;

public class Energy_Object {

    String icon;
    String name;
    String energy_amount;

    public Energy_Object(String icon, String name, String energy_amount) {
        this.icon = icon;
        this.name = name;
        this.energy_amount = energy_amount;
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
