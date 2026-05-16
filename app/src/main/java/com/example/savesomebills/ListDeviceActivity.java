package com.example.savesomebills;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.savesomebills.list_and_group.GroupViewAdapter;

import java.util.ArrayList;

public class ListDeviceActivity extends AppCompatActivity {
    static final String json = "{             \"gruppen\": [    {        \"gruppenid\": 1,            \"icon\": \"group_home\",            \"name\": \"Wohnzimmer\",            \"idliste\": [101, 102],        \"strom\": 230    },    {        \"gruppenid\": 2,            \"icon\": \"group_work\",            \"name\": \"Werkstatt\",            \"idliste\": [103],        \"strom\": 400    }  ],          \"stromobjekte\": [    {        \"id\": 101,            \"gruppenid\": 1,            \"icon\": \"lamp\",            \"strom\": 60,            \"name\": \"Stehlampe\"    },    {        \"id\": 102,            \"gruppenid\": 1,            \"icon\": \"tv\",            \"strom\": 120,            \"name\": \"Fernseher\"    },    {        \"id\": 103,            \"gruppenid\": 2,            \"icon\": \"drill\",            \"strom\": 800,            \"name\": \"Bohrmaschine\"    }  ]}";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.list_device);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        RecyclerView recyclerView = findViewById(R.id.list_recycle_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new GroupViewAdapter(new ArrayList<>(5),""));
    }

}