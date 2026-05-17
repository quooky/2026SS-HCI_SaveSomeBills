package com.example.savesomebills;

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
import com.example.savesomebills.list_and_group.Translator;

import java.util.ArrayList;

public class ListDeviceFragment extends Fragment {

    private static final String ARG_IDS = "object_list";

    public static ListDeviceFragment newInstance(int[] ids) {
        ListDeviceFragment fragment = new ListDeviceFragment();
        Bundle args = new Bundle();
        args.putIntArray(ARG_IDS, ids);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_device, container, false);

        int[] ids = getArguments() != null ? getArguments().getIntArray(ARG_IDS) : new int[0];
        ArrayList<Integer> idsList = new ArrayList<>();
        for (int id : ids) idsList.add(id);

        ArrayList<Energy_Object> objects = new ArrayList<>(Translator.translate_to_energy_object(idsList, ""));

        RecyclerView recyclerView = view.findViewById(R.id.list_recycle_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new ListViewAdapter(objects));

        return view;
    }
}
