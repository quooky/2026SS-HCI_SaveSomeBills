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

    private static final String TAG          = "HomeFragment";
    private static final int    MAX_VALUE    = 100;
    private static final int    MAX_ELEC_PRICE = 20;

    private View rootView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_home, container, false);

        rootView.findViewById(R.id.btn_settings).setOnClickListener(v ->
            requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new SettingsFragment())
                .addToBackStack(null)
                .commit()
        );

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        populateCharts();
    }

    private void populateCharts() {
        int savingsGoal = (int) AppSettings.getSavingsGoal(requireContext());

        TextView histTitle = rootView.findViewById(R.id.hist_title);
        if (histTitle != null) {
            histTitle.setText(getString(R.string.savings_target_format, savingsGoal));
        }

        LinearLayout histogramContainer   = rootView.findViewById(R.id.histogram_container);
        LinearLayout electricityContainer = rootView.findViewById(R.id.electricity_graph_container);
        PieChartView pieChartView         = rootView.findViewById(R.id.pie_chart_view);
        LinearLayout legendContainer      = rootView.findViewById(R.id.pie_legend_container);

        List<Integer> histData = loadData("savings_data.txt");
        List<Integer> elecData = loadData("electricity_prices.txt");

        rootView.post(() -> {
            if (histogramContainer != null) {
                populateHistogram(histogramContainer, histData, histogramContainer.getHeight(), savingsGoal);
                setupThresholdLine(rootView, histogramContainer.getHeight(), savingsGoal);
            }
            if (electricityContainer != null) {
                populateElectricityGraph(electricityContainer, elecData, electricityContainer.getHeight());
                displayCheapestHours(rootView, elecData);
            }
            if (pieChartView != null && legendContainer != null) {
                legendContainer.removeAllViews();
                loadAndPopulatePieChart(pieChartView, legendContainer);
            }
        });
    }

    // ── Pie chart ─────────────────────────────────────────────────────────────

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
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(requireContext().getAssets().open("pie_data.txt")))) {
            String line;
            while (count < 4 && (line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    String label = parts[0];
                    float value  = Float.parseFloat(parts[1]);
                    sumOfFirstFour += value;
                    int color = colors[count % colors.length];
                    entries.add(new PieChartView.PieEntry(label, value, color));
                    addLegendItem(legendContainer, label, color);
                    count++;
                }
            }
        } catch (IOException | NumberFormatException e) {
            Log.e(TAG, "Error loading pie data", e);
        }

        float othersValue = 100f - sumOfFirstFour;
        if (othersValue > 0) {
            String othersLabel = getString(R.string.others);
            int othersColor    = colors[count % colors.length];
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

        float density = getResources().getDisplayMetrics().density;

        View colorBox = new View(getContext());
        colorBox.setLayoutParams(new LinearLayout.LayoutParams((int) (12 * density), (int) (12 * density)));
        colorBox.setBackgroundColor(color);

        TextView textView = new TextView(getContext());
        textView.setText(label);
        textView.setTextSize(12);
        textView.setPadding((int) (8 * density), 0, 0, 0);
        textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.neutral_60));

        item.addView(colorBox);
        item.addView(textView);
        container.addView(item);
    }

    // ── Electricity graph ─────────────────────────────────────────────────────

    private void displayCheapestHours(View view, List<Integer> data) {
        TextView cheapestText = view.findViewById(R.id.cheapest_hours_text);
        if (cheapestText == null || data == null || data.size() < 3) return;

        int minSum = Integer.MAX_VALUE, startIndex = 0;
        for (int i = 0; i <= data.size() - 3; i++) {
            int sum = data.get(i) + data.get(i + 1) + data.get(i + 2);
            if (sum < minSum) { minSum = sum; startIndex = i; }
        }
        cheapestText.setText(getString(R.string.cheapest_hours_label,
            String.format(Locale.getDefault(), "%02d:00 - %02d:00", startIndex, startIndex + 3)));
    }

    // ── Data loading ──────────────────────────────────────────────────────────

    private List<Integer> loadData(String filename) {
        List<Integer> data = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(requireContext().getAssets().open(filename)))) {
            String line = reader.readLine();
            if (line != null)
                for (String val : line.split(",")) data.add(Integer.parseInt(val.trim()));
        } catch (IOException | NumberFormatException e) {
            Log.e(TAG, "Error loading " + filename, e);
            if (filename.equals("savings_data.txt")) { data.add(40); data.add(60); data.add(35); }
        }
        return data;
    }

    // ── Histogram ─────────────────────────────────────────────────────────────

    private void setupThresholdLine(View view, int containerHeightPx, int threshold) {
        View thresholdLine    = view.findViewById(R.id.threshold_line);
        TextView thresholdLbl = view.findViewById(R.id.threshold_label);
        if (thresholdLine == null || thresholdLbl == null) return;

        float density           = getResources().getDisplayMetrics().density;
        int labelHeightReserved = (int) (20 * density);
        int marginPx            = (int) ((threshold / (float) MAX_VALUE) * (containerHeightPx - labelHeightReserved));

        ViewGroup.MarginLayoutParams lineParams = (ViewGroup.MarginLayoutParams) thresholdLine.getLayoutParams();
        lineParams.bottomMargin = marginPx;
        thresholdLine.setLayoutParams(lineParams);

        ViewGroup.MarginLayoutParams labelParams = (ViewGroup.MarginLayoutParams) thresholdLbl.getLayoutParams();
        labelParams.bottomMargin = marginPx + (int) (2 * density);
        thresholdLbl.setLayoutParams(labelParams);
        thresholdLbl.setText(String.valueOf(threshold));
    }

    private void populateHistogram(LinearLayout container, List<Integer> data, int containerHeightPx, int threshold) {
        container.removeAllViews();
        float density = getResources().getDisplayMetrics().density;

        for (Integer value : data) {
            LinearLayout bin = new LinearLayout(getContext());
            bin.setOrientation(LinearLayout.VERTICAL);
            bin.setGravity(Gravity.BOTTOM);
            LinearLayout.LayoutParams binParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
            binParams.setMargins((int) (8 * density), 0, (int) (8 * density), 0);
            bin.setLayoutParams(binParams);

            TextView lbl = new TextView(getContext());
            lbl.setText(String.valueOf(value));
            lbl.setTextSize(11);
            lbl.setGravity(Gravity.CENTER_HORIZONTAL);
            lbl.setTextColor(ContextCompat.getColor(requireContext(), R.color.neutral_60));
            bin.addView(lbl);

            View bar = new View(getContext());
            int labelReserved = (int) (20 * density);
            int barH = (int) ((value / (float) MAX_VALUE) * (containerHeightPx - labelReserved));
            bar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, barH));
            bar.setBackgroundColor(ContextCompat.getColor(requireContext(),
                value >= threshold ? R.color.green_40 : R.color.red_40));
            bin.addView(bar);
            container.addView(bin);
        }
    }

    private void populateElectricityGraph(LinearLayout container, List<Integer> data, int containerHeightPx) {
        container.removeAllViews();
        float density = getResources().getDisplayMetrics().density;

        for (Integer value : data) {
            View bar = new View(getContext());
            int barH = (int) ((value / (float) MAX_ELEC_PRICE) * containerHeightPx);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, barH, 1.0f);
            p.setMargins((int) (1 * density), 0, (int) (1 * density), 0);
            bar.setLayoutParams(p);

            int color;
            if (value < 10)      color = ContextCompat.getColor(requireContext(), R.color.green_40);
            else if (value < 15) color = ContextCompat.getColor(requireContext(), R.color.teal_40);
            else                 color = ContextCompat.getColor(requireContext(), R.color.amber_40);
            bar.setBackgroundColor(color);
            container.addView(bar);
        }
    }
}
