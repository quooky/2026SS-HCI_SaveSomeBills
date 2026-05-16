package com.example.savesomebills.list_and_group;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.savesomebills.R;

import java.util.ArrayList;
import java.util.List;

public class ListViewAdapter extends RecyclerView.Adapter<ListViewAdapter.ViewHolder> {
    List<Integer> id_list;
    String json;


    public ListViewAdapter(List<Integer> id_list, String json) {
        this.id_list = id_list;
        this.json = json;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView name;
        TextView icon;
        TextView energy;
        TextView unit;
        Button button;
        public ViewHolder(@NonNull View view) {
            super(view);
            this.name = view.findViewById(R.id.device_list_name);
            this.icon = view.findViewById(R.id.device_list_icon);
            this.energy = view.findViewById(R.id.device_list_energy);
            this.unit = view.findViewById(R.id.device_list_unit);
            this.button = view.findViewById(R.id.device_list_button);

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


    @NonNull
    @Override
    public ListViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_device_list,parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewAdapter.ViewHolder holder, int position) {
          int id = id_list.get(position);
          Energy_Object ob = Translator.translate_to_energy_object(id,json);
          holder.getName().setText(ob.getName());
          holder.getEnergy().setText(ob.getEnergy_amount());
          holder.getIcon().setText(ob.getIcon());
          
    }

    @Override
    public int getItemCount() {
        return id_list.size();
    }
}
