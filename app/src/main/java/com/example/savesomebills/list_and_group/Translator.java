package com.example.savesomebills.list_and_group;

import java.util.ArrayList;
import java.util.List;

public class Translator {


    public static List<Group> translate_to_Group(String json){

        List<Group> groups = new ArrayList<>();

        // IDs müssen später in Energy_Object vorkommen
        ArrayList<Integer> ids = new ArrayList<>();
        ids.add(101);
        ids.add(102);

        groups.add(new Group(ids, "🏠", "Home", "150", 1));

        return groups;

    }
    public static List<Energy_Object> translate_to_energy_object(List<Integer>ids, String json){

        List<Energy_Object> energyObjects = new ArrayList<>();

        // Erzeuge passende Testdaten für die übergebenen IDs
        for (int id : ids) {
            if (id == 101) {
                energyObjects.add(new Energy_Object("📱", "Telefon", "50", 101));
            } else if (id == 102) {
                energyObjects.add(new Energy_Object("💡", "Licht", "100", 102));
            } else {
                // fallback für unbekannte IDs
                energyObjects.add(new Energy_Object("❓", "Unbekannt", "0", id));
            }
        }

        return energyObjects;

    }
}
