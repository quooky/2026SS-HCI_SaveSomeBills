package com.example.savesomebills;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.savesomebills.list_and_group.GroupViewAdapter;
import com.example.savesomebills.list_and_group.Translator;

import java.util.ArrayList;

public class GroupDeviceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.list_device_group);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        String json = "";


        RecyclerView recyclerView = findViewById(R.id.group_recycle_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new GroupViewAdapter(Translator.translate_to_Group(json)));
    }


    public void group_Button_click(View view){
        Intent intent = new Intent(this, ListDeviceActivity.class);
        intent.putExtra("object_list", (int[]) view.getTag());
        startActivity(intent);
    }
}