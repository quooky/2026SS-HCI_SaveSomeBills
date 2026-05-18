package com.example.savesomebills;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.savesomebills.list_and_group.Energy_Object;
import com.example.savesomebills.list_and_group.ListViewAdapter;

import java.util.ArrayList;
import java.util.List;

public class ListDeviceFragment extends Fragment {

    private static final String ARG_GROUP_ID = "group_id";

    public static ListDeviceFragment newInstance(String groupId) {
        ListDeviceFragment fragment = new ListDeviceFragment();
        Bundle args = new Bundle();
        args.putString(ARG_GROUP_ID, groupId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_device, container, false);

        String groupId = getArguments() != null ? getArguments().getString(ARG_GROUP_ID, "") : "";

        view.findViewById(R.id.btn_back).setOnClickListener(v ->
            requireActivity().getSupportFragmentManager().popBackStack()
        );

        view.findViewById(R.id.fab_add_device).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddDevice.class);
            intent.putExtra(AddDevice.EXTRA_PRESELECT_ROOM, groupId);
            startActivity(intent);
        });

        RecyclerView recyclerView = view.findViewById(R.id.list_recycle_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new ListViewAdapter(buildItems(groupId)));

        return view;
    }

    private List<Energy_Object> buildItems(String groupId) {
        List<Energy_Object> items = new ArrayList<>();
        for (Device d : DeviceStorage.loadAll(requireContext())) {
            if (d.groupId.equals(groupId)) {
                items.add(new Energy_Object(d.icon, d.name, d.wattOn + " W", 0));
            }
        }
        return items;
    }
}
