package com.example.savesomebills.list_and_group;

import java.util.ArrayList;

public class Translator {


    public static Group translate_to_Group(int id, String json){
        ArrayList<Integer>ids = new ArrayList<>();
        ids.add(101);
        return new Group(ids,"🏠","home","50");
    }
    public static Energy_Object translate_to_energy_object(int id, String json){
        return new Energy_Object("\uFE0F","telephone","50");
    }
}
