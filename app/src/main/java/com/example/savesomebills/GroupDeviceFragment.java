package com.example.savesomebills;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.savesomebills.list_and_group.Group;
import com.example.savesomebills.list_and_group.GroupViewAdapter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GroupDeviceFragment extends Fragment {

    private static final String PREFS_GROUP_ICONS = "group_icons";
    private GroupViewAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_device_group, container, false);

        SharedPreferences prefs = requireActivity()
                .getSharedPreferences(PREFS_GROUP_ICONS, Context.MODE_PRIVATE);

        List<Group> groups = buildGroups(prefs);

        adapter = new GroupViewAdapter(groups);

        adapter.setOnGroupClickListener(groupId ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, ListDeviceFragment.newInstance(groupId))
                        .addToBackStack(null)
                        .commit()
        );

        adapter.setOnGroupLongClickListener((position, groupId) -> {
            String[] options = {"Icon ändern", "Raum umbenennen"};
            new AlertDialog.Builder(requireContext())
                    .setTitle(groupId)
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            EmojiPickerDialog.show(requireContext(), EmojiPickerDialog.ROOM_EMOJIS, emoji -> {
                                prefs.edit().putString("icon_" + groupId, emoji).apply();
                                adapter.updateData(buildGroups(prefs));
                            });
                        } else {
                            EditText input = new EditText(requireContext());
                            input.setInputType(InputType.TYPE_CLASS_TEXT);
                            input.setText(groupId);
                            input.selectAll();
                            new AlertDialog.Builder(requireContext())
                                    .setTitle("Raum umbenennen")
                                    .setView(input)
                                    .setPositiveButton("Speichern", (d, w) -> {
                                        String newName = input.getText().toString().trim();
                                        if (newName.isEmpty() || newName.equals(groupId)) return;
                                        DeviceStorage.renameGroup(requireContext(), groupId, newName);
                                        String oldIcon = prefs.getString("icon_" + groupId, "🏠");
                                        prefs.edit()
                                                .remove("icon_" + groupId)
                                                .putString("icon_" + newName, oldIcon)
                                                .apply();
                                        adapter.updateData(buildGroups(prefs));
                                    })
                                    .setNegativeButton("Abbrechen", null)
                                    .show();
                        }
                    })
                    .show();
        });

        RecyclerView recyclerView = view.findViewById(R.id.group_recycle_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.floatingActionButton2).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), AddDevice.class))
        );

        view.findViewById(R.id.imageButton).setOnClickListener(v ->
                new AlertDialog.Builder(requireContext())
                        .setTitle("Meine Gruppen")
                        .setMessage("Hier siehst du deine Räume oder Gruppen. Der Wert zeigt den gesamten Verbrauch aller Geräte in dieser Gruppe. Tippe auf eine Gruppe, um die einzelnen Geräte zu sehen.")
                        .setPositiveButton("OK", null)
                        .show()
        );

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            SharedPreferences prefs = requireActivity()
                    .getSharedPreferences(PREFS_GROUP_ICONS, Context.MODE_PRIVATE);
            adapter.updateData(buildGroups(prefs));
        }
    }

    private List<Group> buildGroups(SharedPreferences prefs) {
        List<Device> devices = DeviceStorage.loadAll(requireContext());

        LinkedHashMap<String, List<Device>> map = new LinkedHashMap<>();
        for (Device d : devices) {
            map.computeIfAbsent(d.groupId, k -> new ArrayList<>()).add(d);
        }

        List<Group> groups = new ArrayList<>();
        for (Map.Entry<String, List<Device>> entry : map.entrySet()) {
            int totalWatt = 0;
            for (Device d : entry.getValue()) totalWatt += d.wattOn;

            String icon = prefs.getString("icon_" + entry.getKey(), "🏠");
            groups.add(new Group(new ArrayList<>(), icon, entry.getKey(), totalWatt, 0, ""));
        }
        return groups;
    }
}