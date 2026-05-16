package com.example.savesomebills.list_and_group;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.savesomebills.R;

import java.util.List;

public class GroupViewAdapter extends RecyclerView.Adapter<GroupViewAdapter.ViewHolder> {
    List<Integer> id_list;
    String json;

    public GroupViewAdapter(List<Integer> id_list, String json) {
        this.id_list = id_list;
        this.json = json;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_device_group,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int id = id_list.get(position);
        Group ob = Translator.translate_to_Group(id,json);
        holder.getName().setText(ob.getName());
        holder.getEnergy().setText(ob.getEnergy_amount());
        holder.getIcon().setText(ob.getIcon());
    }

    @Override
    public int getItemCount() {
        return id_list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView icon;
        TextView energy;
        TextView unit;
        Button button;
        public ViewHolder(@NonNull View view) {
            super(view);
            this.name = view.findViewById(R.id.device_group_name);
            this.icon = view.findViewById(R.id.device_group_icon);
            this.energy = view.findViewById(R.id.device_group_energy);
            this.unit = view.findViewById(R.id.device_group_unit);
            this.button = view.findViewById(R.id.device_group_button);

        }

        public TextView getName() {
            return name;
        }

        public TextView getIcon() {
            return icon;
        }

        public TextView getEnergy() {
            return energy;
        }

        public TextView getUnit() {
            return unit;
        }

        public Button getButton() {
            return button;
        }
    }

    


}


