package com.example.savesomebills;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TipsPagerAdapter extends RecyclerView.Adapter<TipsPagerAdapter.TipViewHolder> {

    private final List<Tip> tips;

    public TipsPagerAdapter(List<Tip> tips) {
        this.tips = tips;
    }

    @NonNull
    @Override
    public TipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_tip_card, parent, false);
        return new TipViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TipViewHolder holder, int position) {
        holder.text.setText(tips.get(position).text);
    }

    @Override
    public int getItemCount() {
        return tips.size();
    }

    static class TipViewHolder extends RecyclerView.ViewHolder {
        TextView text;

        TipViewHolder(@NonNull View view) {
            super(view);
            text = view.findViewById(R.id.tv_tip_text);
        }
    }

    static class Tip {
        final String category;
        final String text;

        Tip(String category, String text) {
            this.category = category;
            this.text = text;
        }
    }
}
