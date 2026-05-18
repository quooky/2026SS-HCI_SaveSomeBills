package com.example.savesomebills;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.savesomebills.list_and_group.ListViewAdapter;

import java.util.ArrayList;
import java.util.List;

public class ListDeviceActivity extends AppCompatActivity {

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

        String groupId = getIntent().getStringExtra("group_id");
        if (groupId == null) groupId = "";

        List<Device> devices = new ArrayList<>();
        for (Device d : DeviceStorage.loadAll(this)) {
            if (d.groupId.equals(groupId)) devices.add(d);
        }

        RecyclerView recyclerView = findViewById(R.id.list_recycle_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ListViewAdapter adapter = new ListViewAdapter(devices);
        adapter.setOnEditClickListener(device -> {
            Intent intent = new Intent(this, AddDevice.class);
            intent.putExtra(AddDevice.EXTRA_EDIT_DEVICE_ID, device.objectId);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
    }
}
