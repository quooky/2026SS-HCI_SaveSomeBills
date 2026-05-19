package com.example.savesomebills.list_and_group;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.savesomebills.AppSettings;
import com.example.savesomebills.Device;
import com.example.savesomebills.R;
import com.example.savesomebills.SettingsFragment;

import java.util.List;

public class ListViewAdapter extends RecyclerView.Adapter<ListViewAdapter.ViewHolder> {

    private List<Device> devices;
    private OnEditClickListener editListener;

    public interface OnEditClickListener {
        void onEditClick(Device device);
    }

    public void setOnEditClickListener(OnEditClickListener listener) {
        this.editListener = listener;
    }

    public ListViewAdapter(List<Device> devices) {
        this.devices = devices;
    }

    public void updateData(List<Device> newDevices) {
        this.devices = newDevices;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.layout_device_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Device d = devices.get(position);
        holder.name.setText(d.name);
        holder.icon.setText(d.icon != null ? d.icon : "⚡"); //d.wattOn + " W"
        holder.energy.setText(AppSettings.formatEnergy(holder.energy.getContext(), d.wattOn*0.001));
        holder.editButton.setOnClickListener(v -> {
            if (editListener != null) editListener.onEditClick(d);
        });
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView    name, icon, energy;
        ImageButton editButton;

        public ViewHolder(@NonNull View view) {
            super(view);
            name       = view.findViewById(R.id.device_list_name);
            icon       = view.findViewById(R.id.device_list_icon);
            energy     = view.findViewById(R.id.device_list_energy);
            editButton = view.findViewById(R.id.device_list_edit);
        }
    }
}
