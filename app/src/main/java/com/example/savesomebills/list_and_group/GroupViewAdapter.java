package com.example.savesomebills.list_and_group;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.savesomebills.R;

import java.util.List;

public class GroupViewAdapter extends RecyclerView.Adapter<GroupViewAdapter.ViewHolder> {
    List<Group> group_list;
    private OnGroupClickListener listener;
    private OnIconClickListener iconListener;

    public interface OnGroupClickListener {
        void onGroupClick(String groupId);
    }

    public interface OnIconClickListener {
        void onIconClick(int position, String groupId);
    }

    public void setOnGroupClickListener(OnGroupClickListener listener) {
        this.listener = listener;
    }

    public void setOnIconClickListener(OnIconClickListener listener) {
        this.iconListener = listener;
    }

    public GroupViewAdapter(List<Group> id_list) {
        this.group_list = id_list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_device_group,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Group ob = group_list.get(position);
        holder.getName().setText(ob.getName());
        holder.getEnergy().setText(ob.getEnergy_amount());
        holder.getIcon().setText(ob.getIcon());
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onGroupClick(ob.getName());
        });
        holder.getIcon().setOnClickListener(v -> {
            if (iconListener != null) iconListener.onIconClick(holder.getAdapterPosition(), ob.getName());
        });
    }

    @Override
    public int getItemCount() {
        return group_list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView icon;
        TextView energy;
        TextView unit;

        public ViewHolder(@NonNull View view) {
            super(view);
            this.name = view.findViewById(R.id.device_group_name);
            this.icon = view.findViewById(R.id.device_group_icon);
            this.energy = view.findViewById(R.id.device_group_energy);
            this.unit = view.findViewById(R.id.device_group_unit);
        }

        public TextView getName() { return name; }
        public TextView getIcon() { return icon; }
        public TextView getEnergy() { return energy; }
        public TextView getUnit() { return unit; }
    }

    


}


