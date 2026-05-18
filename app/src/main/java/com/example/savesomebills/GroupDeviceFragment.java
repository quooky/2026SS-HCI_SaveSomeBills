package com.example.savesomebills;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_device_group, container, false);

        SharedPreferences prefs = requireActivity()
                .getSharedPreferences(PREFS_GROUP_ICONS, Context.MODE_PRIVATE);

        List<Group> groups = buildGroups(prefs);

        GroupViewAdapter adapter = new GroupViewAdapter(groups);

        adapter.setOnGroupClickListener(groupId ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, ListDeviceFragment.newInstance(groupId))
                        .addToBackStack(null)
                        .commit()
        );

        adapter.setOnIconClickListener((position, groupId) ->
                EmojiPickerDialog.show(requireContext(), EmojiPickerDialog.ROOM_EMOJIS, emoji -> {
                    prefs.edit().putString("icon_" + groupId, emoji).apply();
                    groups.get(position).setIcon(emoji);
                    adapter.notifyItemChanged(position);
                })
        );

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
            groups.add(new Group(new ArrayList<>(), icon, entry.getKey(), totalWatt + " W", 0));
        }
        return groups;
    }
}