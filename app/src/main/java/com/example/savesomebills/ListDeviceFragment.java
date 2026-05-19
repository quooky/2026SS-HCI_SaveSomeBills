package com.example.savesomebills;

import android.app.AlertDialog;
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

        String groupId = getArguments() != null
                ? getArguments().getString(ARG_GROUP_ID, "")
                : "";

        view.findViewById(R.id.btn_back).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        view.findViewById(R.id.fab_add_device).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddDevice.class);
            intent.putExtra(AddDevice.EXTRA_PRESELECT_ROOM, groupId);
            startActivity(intent);
        });

        View infoButton = view.findViewById(R.id.btn_info);
        infoButton.setOnClickListener(v ->
                new AlertDialog.Builder(requireContext())
                        .setTitle("Meine Geräte")
                        .setMessage(
                                "Hier siehst du alle Geräte in dieser Gruppe.\n\n" +

                                        "🔌 Verbrauch\n" +
                                        "• Watt (W) = Stromverbrauch im Betrieb\n" +
                                        "• Mehr Watt = höherer Verbrauch\n\n" +

                                        "📊 Einordnung\n" +
                                        "• Klein: 5–100 W\n" +
                                        "• Mittel: 50–200 W\n" +
                                        "• Groß: 300 W+\n\n" +

                                        "✏️ Bearbeiten\n" +
                                        "• Stift = Gerät anpassen\n" +
                                        "• Blaues + unten rechts = neues Gerät hinzufügen"
                        )
                        .setPositiveButton("OK", null)
                        .show()
        );

        RecyclerView recyclerView = view.findViewById(R.id.list_recycle_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        ListViewAdapter adapter = new ListViewAdapter(buildItems(groupId));
        adapter.setOnEditClickListener(device -> {
            Intent intent = new Intent(getActivity(), AddDevice.class);
            intent.putExtra(AddDevice.EXTRA_EDIT_DEVICE_ID, device.objectId);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        return view;
    }

    private List<Device> buildItems(String groupId) {
        List<Device> items = new ArrayList<>();
        for (Device d : DeviceStorage.loadAll(requireContext())) {
            if (d.groupId.equals(groupId)) items.add(d);
        }

        return items;
    }
}
