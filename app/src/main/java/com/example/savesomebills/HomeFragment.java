package com.example.savesomebills;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private static final int THRESHOLD = 50;
    private static final int MAX_VALUE = 100;
    private static final int MAX_ELEC_PRICE = 20;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        LinearLayout histogramContainer = view.findViewById(R.id.histogram_container);
        if (histogramContainer != null) {
            List<Integer> data = loadData("savings_data.txt");
            populateHistogram(histogramContainer, data);
            setupThresholdLine(view);
        }

        LinearLayout electricityContainer = view.findViewById(R.id.electricity_graph_container);
        if (electricityContainer != null) {
            List<Integer> data = loadData("electricity_prices.txt");
            populateElectricityGraph(electricityContainer, data);
            displayCheapestHours(view, data);
        }

        PieChartView pieChartView = view.findViewById(R.id.pie_chart_view);
        LinearLayout legendContainer = view.findViewById(R.id.pie_legend_container);
        if (pieChartView != null && legendContainer != null) {
            loadAndPopulatePieChart(pieChartView, legendContainer);
        }

        return view;
    }

    private void loadAndPopulatePieChart(PieChartView pieChartView, LinearLayout legendContainer) {
        List<PieChartView.PieEntry> entries = new ArrayList<>();
        int[] colors = {
            ContextCompat.getColor(requireContext(), R.color.green_40),
            ContextCompat.getColor(requireContext(), R.color.teal_40),
            ContextCompat.getColor(requireContext(), R.color.amber_40),
            ContextCompat.getColor(requireContext(), R.color.red_40),
            ContextCompat.getColor(requireContext(), R.color.neutral_40)
        };

        float sumOfFirstFour = 0;
        int count = 0;
        AssetManager assetManager = requireContext().getAssets();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(assetManager.open("pie_data.txt")))) {
            String line;
            while (count < 4 && (line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    String label = parts[0];
                    float value = Float.parseFloat(parts[1]);
                    sumOfFirstFour += value;
                    int color = colors[count % colors.length];
                    entries.add(new PieChartView.PieEntry(label, value, color));
                    
                    // Add to legend
                    addLegendItem(legendContainer, label, color);
                    
                    count++;
                }
            }
        } catch (IOException | NumberFormatException e) {
            Log.e(TAG, "Error loading pie data", e);
        }

        // Add "Others" category as the 5th entry
        float othersValue = 100f - sumOfFirstFour;
        if (othersValue > 0) {
            String othersLabel = getString(R.string.others);
            int othersColor = colors[count % colors.length];
            entries.add(new PieChartView.PieEntry(othersLabel, othersValue, othersColor));
            addLegendItem(legendContainer, othersLabel, othersColor);
        }

        pieChartView.setEntries(entries);
    }

    private void addLegendItem(LinearLayout container, String label, int color) {
        LinearLayout item = new LinearLayout(getContext());
        item.setOrientation(LinearLayout.HORIZONTAL);
        item.setGravity(Gravity.CENTER_VERTICAL);
        item.setPadding(0, 4, 0, 4);

        View colorBox = new View(getContext());
        float density = getResources().getDisplayMetrics().density;
        LinearLayout.LayoutParams boxParams = new LinearLayout.LayoutParams((int)(12 * density), (int)(12 * density));
        colorBox.setLayoutParams(boxParams);
        colorBox.setBackgroundColor(color);

        TextView textView = new TextView(getContext());
        textView.setText(label);
        textView.setTextSize(12);
        textView.setPadding((int)(8 * density), 0, 0, 0);
        textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.neutral_40));

        item.addView(colorBox);
        item.addView(textView);
        container.addView(item);
    }

    private void displayCheapestHours(View view, List<Integer> data) {
        TextView cheapestText = view.findViewById(R.id.cheapest_hours_text);
        if (cheapestText == null || data == null || data.size() < 3) return;

        int minSum = Integer.MAX_VALUE;
        int startIndex = 0;

        // Find the window of 3 consecutive hours with the lowest sum
        for (int i = 0; i <= data.size() - 3; i++) {
            int currentSum = data.get(i) + data.get(i + 1) + data.get(i + 2);
            if (currentSum < minSum) {
                minSum = currentSum;
                startIndex = i;
            }
        }

        String timeRange = String.format(Locale.getDefault(), "%02d:00 - %02d:00", startIndex, startIndex + 3);
        cheapestText.setText(getString(R.string.cheapest_hours_label, timeRange));
    }

    private List<Integer> loadData(String filename) {
        List<Integer> data = new ArrayList<>();
        AssetManager assetManager = requireContext().getAssets();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(assetManager.open(filename)))) {
            String line = reader.readLine();
            if (line != null) {
                String[] values = line.split(",");
                for (String val : values) {
                    data.add(Integer.parseInt(val.trim()));
                }
            }
        } catch (IOException | NumberFormatException e) {
            Log.e(TAG, "Error loading data from " + filename, e);
            // Default values if file missing
            if (filename.equals("savings_data.txt")) {
                data.add(40); data.add(60); data.add(35);
            }
        }
        return data;
    }

    private void setupThresholdLine(View view) {
        View thresholdLine = view.findViewById(R.id.threshold_line);
        TextView thresholdLabel = view.findViewById(R.id.threshold_label);

        if (thresholdLine != null && thresholdLabel != null) {
            float density = getResources().getDisplayMetrics().density;
            int containerHeightPx = (int) (150 * density);
            int labelHeightReserved = (int) (20 * density);
            
            int thresholdMarginPx = (int) ((THRESHOLD / (float) MAX_VALUE) * (containerHeightPx - labelHeightReserved));

            ViewGroup.MarginLayoutParams lineParams = (ViewGroup.MarginLayoutParams) thresholdLine.getLayoutParams();
            lineParams.bottomMargin = thresholdMarginPx;
            thresholdLine.setLayoutParams(lineParams);

            ViewGroup.MarginLayoutParams labelParams = (ViewGroup.MarginLayoutParams) thresholdLabel.getLayoutParams();
            labelParams.bottomMargin = thresholdMarginPx + (int) (2 * density);
            thresholdLabel.setLayoutParams(labelParams);
            thresholdLabel.setText(String.valueOf(THRESHOLD));
        }
    }

    private void populateHistogram(LinearLayout container, List<Integer> data) {
        container.removeAllViews();
        float density = getResources().getDisplayMetrics().density;
        int containerHeightPx = (int) (150 * density);

        for (Integer value : data) {
            LinearLayout binContainer = new LinearLayout(getContext());
            binContainer.setOrientation(LinearLayout.VERTICAL);
            binContainer.setGravity(Gravity.BOTTOM);
            LinearLayout.LayoutParams binParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
            binParams.setMargins((int) (8 * density), 0, (int) (8 * density), 0);
            binContainer.setLayoutParams(binParams);

            TextView valueLabel = new TextView(getContext());
            valueLabel.setText(String.valueOf(value));
            valueLabel.setTextSize(11);
            valueLabel.setGravity(Gravity.CENTER_HORIZONTAL);
            valueLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.neutral_40));
            binContainer.addView(valueLabel);

            View bar = new View(getContext());
            int labelHeightReserved = (int) (20 * density);
            int barHeightPx = (int) ((value / (float) MAX_VALUE) * (containerHeightPx - labelHeightReserved));
            
            LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, barHeightPx);
            bar.setLayoutParams(barParams);

            int colorRes = (value >= THRESHOLD) ? R.color.green_40 : R.color.red_40;
            bar.setBackgroundColor(ContextCompat.getColor(requireContext(), colorRes));
            
            binContainer.addView(bar);
            container.addView(binContainer);
        }
    }

    private void populateElectricityGraph(LinearLayout container, List<Integer> data) {
        container.removeAllViews();
        float density = getResources().getDisplayMetrics().density;
        int containerHeightPx = (int) (120 * density);

        for (Integer value : data) {
            View bar = new View(getContext());
            int barHeightPx = (int) ((value / (float) MAX_ELEC_PRICE) * containerHeightPx);
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, barHeightPx, 1.0f);
            params.setMargins((int) (1 * density), 0, (int) (1 * density), 0);
            bar.setLayoutParams(params);
            
            int color;
            if (value < 10) {
                color = ContextCompat.getColor(requireContext(), R.color.green_40);
            } else if (value < 15) {
                color = ContextCompat.getColor(requireContext(), R.color.teal_40);
            } else {
                color = ContextCompat.getColor(requireContext(), R.color.amber_40);
            }
            bar.setBackgroundColor(color);
            
            container.addView(bar);
        }
    }
}
