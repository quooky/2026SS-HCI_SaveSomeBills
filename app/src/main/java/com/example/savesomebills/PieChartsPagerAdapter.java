package com.example.savesomebills;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class PieChartsPagerAdapter extends RecyclerView.Adapter<PieChartsPagerAdapter.ViewHolder> {

    public static class ChartData {
        String title;
        List<PieChartView.PieEntry> entries;

        public ChartData(String title, List<PieChartView.PieEntry> entries) {
            this.title = title;
            this.entries = entries;
        }
    }

    private final List<ChartData> dataList;

    public PieChartsPagerAdapter(List<ChartData> dataList) {
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pie_chart_page, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChartData data = dataList.get(position);
        holder.titleView.setText(data.title);
        holder.pieChartView.setEntries(data.entries);
        
        holder.legendContainer.removeAllViews();
        for (PieChartView.PieEntry entry : data.entries) {
            addLegendItem(holder.legendContainer, entry.label, entry.color);
        }
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    private void addLegendItem(LinearLayout container, String label, int color) {
        Context context = container.getContext();
        LinearLayout item = new LinearLayout(context);
        item.setOrientation(LinearLayout.HORIZONTAL);
        item.setGravity(Gravity.CENTER_VERTICAL);
        item.setPadding(0, 4, 0, 4);

        View colorBox = new View(context);
        float density = context.getResources().getDisplayMetrics().density;
        LinearLayout.LayoutParams boxParams = new LinearLayout.LayoutParams((int)(10 * density), (int)(10 * density));
        colorBox.setLayoutParams(boxParams);
        colorBox.setBackgroundColor(color);

        TextView textView = new TextView(context);
        textView.setText(label);
        textView.setTextSize(11);
        textView.setPadding((int)(8 * density), 0, 0, 0);
        textView.setTextColor(ContextCompat.getColor(context, R.color.neutral_60));

        item.addView(colorBox);
        item.addView(textView);
        container.addView(item);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleView;
        PieChartView pieChartView;
        LinearLayout legendContainer;

        ViewHolder(View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.page_title);
            pieChartView = itemView.findViewById(R.id.pie_chart_view);
            legendContainer = itemView.findViewById(R.id.pie_legend_container);
        }
    }
}
