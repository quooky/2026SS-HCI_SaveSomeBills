package com.example.savesomebills;

import android.content.res.AssetManager;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import androidx.appcompat.app.AlertDialog;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private static final int MAX_VALUE = 100;
    private static final int MAX_ELEC_PRICE = 20;

    private Integer firstCheapestHour;

    private View currentTimeLine;
    private FrameLayout graphContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        LinearLayout histogramContainer = view.findViewById(R.id.histogram_container);
        LinearLayout electricityContainer = view.findViewById(R.id.electricity_graph_container);
        ViewPager2 piePager = view.findViewById(R.id.pie_chart_pager);
        LinearLayout pieDots = view.findViewById(R.id.dots_pie_chart);

        view.findViewById(R.id.btn_settings).setOnClickListener(v ->
            requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new SettingsFragment())
                .addToBackStack(null)
                .commit()
        );

        view.findViewById(R.id.btn_info).setOnClickListener(v ->
                new AlertDialog.Builder(requireContext())
                        .setTitle("ℹ️ So funktioniert der Homescreen")
                        .setMessage(
                                "Hier siehst du eine Übersicht über dein Sparziel und deinen Stromverbrauch.\n\n" +

                                        "📊 Monatliches Sparziel\n" +
                                        "Die Balken zeigen deine geschätzten Einsparungen der letzten Monate. " +
                                        "Die gestrichelte Linie zeigt dein eingestelltes monatliches Sparziel.\n\n" +

                                        "⚡ Happy Hours\n" +
                                        "Hier werden günstige Stromzeiten angezeigt. " +
                                        "In diesen Stunden lohnt es sich besonders, Geräte wie Waschmaschine oder Geschirrspüler zu nutzen.\n\n" +

                                        "🏠 Geräteverbrauch\n" +
                                        "Dieser Bereich zeigt die Verteilung deines Verbrauchs nach Geräten oder Gruppen, sobald Geräte hinzugefügt wurden."
                        )
                        .setPositiveButton("Verstanden", null)
                        .show()
        );

        new Thread(() -> {
            final List<Integer> histData =
                    applyActionTipSavings(loadData("savings_data.txt", false));

            List<Integer> elecData = loadData("electricity_prices.txt", true);

            if (getActivity() == null) return;

            getActivity().runOnUiThread(() -> {
                if (getView() == null) return;

                int threshold = (int) AppSettings.getSavingsGoal(requireContext());
                TextView histTitle = view.findViewById(R.id.hist_title);
                if (histTitle != null) {
                    histTitle.setText(getString(R.string.savings_target, threshold));
                }
                if (histogramContainer != null) {
                    histogramContainer.post(() -> {
                        populateHistogram(
                                histogramContainer,
                                histData,
                                histogramContainer.getHeight(),
                                threshold
                        );

                        setupThresholdLine(
                                view,
                                histogramContainer.getHeight(),
                                threshold
                        );
                    });
                }

                if (electricityContainer != null) {
                    populateElectricityGraph(
                            electricityContainer,
                            elecData,
                            electricityContainer.getHeight()
                    );

                    displayCheapestHours(view, elecData);
                }

                if (piePager != null && pieDots != null) {
                    setupPiePager(piePager, pieDots);
                }
            });
        }).start();

        graphContainer = (FrameLayout) view.findViewById(R.id.electricity_graph_container).getParent();
        currentTimeLine = view.findViewById(R.id.current_time_line);

        return view;
    }

    private void updateCurrentTimeLine() {
        if (currentTimeLine == null || graphContainer == null) {
            return;
        }

        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);

        float currentTimeInHours = currentHour + (currentMinute / 60.0f);

        float minHour = 0.0f;
        float maxHour = 24.0f;

        if (currentTimeInHours < minHour || currentTimeInHours > maxHour) {
            currentTimeLine.setVisibility(View.GONE);
            return;
        }

        float position = (currentTimeInHours - minHour) / (maxHour - minHour);

        graphContainer.post(() -> {
            int containerWidth = graphContainer.getWidth();

            if (containerWidth > 0) {
                int linePosition = (int) (position * containerWidth);

                FrameLayout.LayoutParams params =
                        (FrameLayout.LayoutParams) currentTimeLine.getLayoutParams();

                if (params == null) {
                    params = new FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.WRAP_CONTENT,
                            FrameLayout.LayoutParams.MATCH_PARENT
                    );
                }

                params.leftMargin = linePosition;
                currentTimeLine.setLayoutParams(params);
                currentTimeLine.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setupPiePager(ViewPager2 pager, LinearLayout dotsContainer) {
        List<Device> devices = DeviceStorage.loadAll(requireContext());
        if (devices.isEmpty()) return;

        List<PieChartsPagerAdapter.ChartData> chartDataList = new ArrayList<>();

        chartDataList.add(new PieChartsPagerAdapter.ChartData(
                getString(R.string.energy_distribution_devices),
                getIndividualConsumptionEntries(devices)
        ));

        chartDataList.add(new PieChartsPagerAdapter.ChartData(
                getString(R.string.energy_distribution_groups),
                getGroupedConsumptionEntries(devices)
        ));

        pager.setAdapter(new PieChartsPagerAdapter(chartDataList));
        setupDots(dotsContainer, chartDataList.size());
        updateDots(dotsContainer, 0);

        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateDots(dotsContainer, position);
            }
        });
    }

    private List<PieChartView.PieEntry> getIndividualConsumptionEntries(List<Device> devices) {
        List<PieChartView.PieEntry> entries = new ArrayList<>();
        int[] colors = getChartColors();

        class DeviceConsumption {
            String name;
            float total;

            DeviceConsumption(String name, float total) {
                this.name = name;
                this.total = total;
            }
        }

        List<DeviceConsumption> consumptions = new ArrayList<>();
        float totalEnergy = 0;

        for (Device d : devices) {
            float consumption = (d.onHours * d.wattOn) + (d.standbyHours * d.wattStandby);
            consumptions.add(new DeviceConsumption(d.name, consumption));
            totalEnergy += consumption;
        }

        consumptions.sort((a, b) -> Float.compare(b.total, a.total));

        int displayCount = Math.min(consumptions.size(), 4);
        float sumOfDisplayed = 0;

        for (int i = 0; i < displayCount; i++) {
            DeviceConsumption dc = consumptions.get(i);
            entries.add(new PieChartView.PieEntry(dc.name, dc.total, colors[i % colors.length]));
            sumOfDisplayed += dc.total;
        }

        if (consumptions.size() > 4) {
            float othersValue = totalEnergy - sumOfDisplayed;

            if (othersValue > 0) {
                entries.add(new PieChartView.PieEntry(
                        getString(R.string.others),
                        othersValue,
                        colors[4 % colors.length]
                ));
            }
        }

        return entries;
    }

    private List<PieChartView.PieEntry> getGroupedConsumptionEntries(List<Device> devices) {
        Map<String, Float> groupMap = new HashMap<>();

        for (Device d : devices) {
            float consumption = (d.onHours * d.wattOn) + (d.standbyHours * d.wattStandby);
            groupMap.put(d.groupId, groupMap.getOrDefault(d.groupId, 0f) + consumption);
        }

        List<PieChartView.PieEntry> entries = new ArrayList<>();
        int[] colors = getChartColors();
        int i = 0;

        for (Map.Entry<String, Float> entry : groupMap.entrySet()) {
            entries.add(new PieChartView.PieEntry(entry.getKey(), entry.getValue(), colors[i % colors.length]));
            i++;
        }

        return entries;
    }

    private int[] getChartColors() {
        return new int[] {
                ContextCompat.getColor(requireContext(), R.color.green_40),
                ContextCompat.getColor(requireContext(), R.color.teal_40),
                ContextCompat.getColor(requireContext(), R.color.amber_40),
                ContextCompat.getColor(requireContext(), R.color.red_40),
                ContextCompat.getColor(requireContext(), R.color.neutral_40)
        };
    }

    private void setupDots(LinearLayout container, int count) {
        container.removeAllViews();

        float dp = getResources().getDisplayMetrics().density;
        int size = (int) (8 * dp);
        int margin = (int) (4 * dp);

        for (int i = 0; i < count; i++) {
            View dot = new View(getContext());

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
            lp.setMargins(margin, 0, margin, 0);
            dot.setLayoutParams(lp);

            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.OVAL);
            dot.setBackground(shape);

            container.addView(dot);
        }
    }

    private void updateDots(LinearLayout container, int activeIndex) {
        TypedValue accent = new TypedValue();
        TypedValue inactive = new TypedValue();

        requireContext().getTheme().resolveAttribute(R.attr.appColorAccent, accent, true);
        requireContext().getTheme().resolveAttribute(R.attr.appColorOnSurfaceVariant, inactive, true);

        for (int i = 0; i < container.getChildCount(); i++) {
            GradientDrawable shape = (GradientDrawable) container.getChildAt(i).getBackground();
            shape.setColor(i == activeIndex ? accent.data : inactive.data);
        }
    }

    private void displayCheapestHours(View view, List<Integer> data) {
        TextView cheapestText = view.findViewById(R.id.cheapest_hours_text);

        if (cheapestText == null || data == null || data.size() < 3) return;

        if (firstCheapestHour == null) {
            setFirstCheapestHour(data);
        }

        String timeRange = String.format(
                Locale.getDefault(),
                "%02d:00 - %02d:00",
                firstCheapestHour,
                firstCheapestHour + 3
        );

        cheapestText.setText(getString(R.string.cheapest_hours_label, timeRange));
    }

    private void setFirstCheapestHour(List<Integer> data) {
        int minSum = Integer.MAX_VALUE;
        int startIndex = 0;

        for (int i = 0; i <= data.size() - 3; i++) {
            int currentSum = data.get(i) + data.get(i + 1) + data.get(i + 2);

            if (currentSum <= minSum) {
                minSum = currentSum;
                startIndex = i;
            }
        }

        firstCheapestHour = startIndex;
    }

    private List<Integer> loadData(String filename) {
        List<Integer> data = new ArrayList<>();

        File file = new File(requireContext().getFilesDir(), filename);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            String savedDate = reader.readLine();
            String dataLine = reader.readLine();

            if (dataLine != null) {
                for (String val : dataLine.split(",")) {
                    data.add(Integer.parseInt(val.trim()));
                }
            }
        } catch (IOException | NumberFormatException e) {
            Log.e(TAG, "Error reading internal storage data", e);
        }

        return data;
    }

    private List<Integer> loadData(String filename, boolean checkDate) {
        List<Integer> data = new ArrayList<>();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        if (checkDate) {
            File file = new File(requireContext().getFilesDir(), filename);

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
                String savedDate = reader.readLine();

                if (today.equals(savedDate)) {
                    String dataLine = reader.readLine();

                    if (dataLine != null) {
                        for (String val : dataLine.split(",")) {
                            data.add(Integer.parseInt(val.trim()));
                        }

                        return data;
                    }
                }
            } catch (IOException | NumberFormatException e) {
                Log.e(TAG, "Error reading internal storage data", e);
            }

            return updateElectricityData(filename, today);
        }

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
        }

        return data;
    }

    private Pair<List<Integer>, Boolean> getElectricityPrices(String date) {
        List<Integer> hourlyAverages = new ArrayList<>();
        HttpURLConnection connection = null;

        try {
            URL url = new URL("https://api.energy-charts.info/price?bzn=AT&start=" + date);

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();

                JSONObject json = new JSONObject(response.toString());
                JSONArray prices = json.getJSONArray("price");

                for (int hour = 0; hour < 24; hour++) {
                    float sum = 0;
                    int count = 0;

                    for (int i = 0; i < 4; i++) {
                        int index = hour * 4 + i;

                        if (index < prices.length()) {
                            sum += prices.getInt(index) / 10.0;
                            count++;
                        }
                    }

                    if (count > 0) {
                        hourlyAverages.add(Math.round(sum / count));
                    } else {
                        hourlyAverages.add(0);
                    }
                }

                return new Pair<>(hourlyAverages, true);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching electricity prices from API", e);
        }

        if (hourlyAverages.size() < 24) {
            hourlyAverages = loadData("electricity_prices.txt");
        }

        return new Pair<>(hourlyAverages, false);
    }

    private List<Integer> updateElectricityData(String filename, String today) {
        Pair<List<Integer>, Boolean> updates = getElectricityPrices(today);
        List<Integer> newData = updates.first;
        Boolean upToDate = updates.second;

        if (upToDate) {
            File file = new File(requireContext().getFilesDir(), filename);

            try (PrintWriter writer = new PrintWriter(new FileOutputStream(file))) {
                writer.println(today);
                writer.println(
                        newData.stream()
                                .map(String::valueOf)
                                .collect(Collectors.joining(","))
                );
            } catch (IOException e) {
                Log.e(TAG, "Error updating electricity data file", e);
            }
        }

        return newData;
    }

    private void setupThresholdLine(View view, int containerHeightPx, int threshold) {
        View thresholdLine = view.findViewById(R.id.threshold_line);
        TextView thresholdLabel = view.findViewById(R.id.threshold_label);

        if (thresholdLine != null && thresholdLabel != null) {
            float density = getResources().getDisplayMetrics().density;
            int labelHeightReserved = (int) (20 * density);

            int thresholdMarginPx =
                    (int) ((threshold / (float) MAX_VALUE) * (containerHeightPx - labelHeightReserved));

            ViewGroup.MarginLayoutParams lineParams =
                    (ViewGroup.MarginLayoutParams) thresholdLine.getLayoutParams();

            lineParams.bottomMargin = thresholdMarginPx;
            thresholdLine.setLayoutParams(lineParams);

            ViewGroup.MarginLayoutParams labelParams =
                    (ViewGroup.MarginLayoutParams) thresholdLabel.getLayoutParams();

            labelParams.bottomMargin = thresholdMarginPx + (int) (2 * density);
            thresholdLabel.setLayoutParams(labelParams);
            thresholdLabel.setText(String.valueOf(threshold));
        }
    }

    private void populateHistogram(LinearLayout container, List<Integer> data, int containerHeightPx, int threshold) {
        container.removeAllViews();

        float density = getResources().getDisplayMetrics().density;

        for (Integer value : data) {
            LinearLayout binContainer = new LinearLayout(getContext());
            binContainer.setOrientation(LinearLayout.VERTICAL);
            binContainer.setGravity(Gravity.BOTTOM);

            LinearLayout.LayoutParams binParams =
                    new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);

            binParams.setMargins((int) (24 * density), 0, (int) (24 * density), 0);
            binContainer.setLayoutParams(binParams);

            TextView valueLabel = new TextView(getContext());
            valueLabel.setText(String.valueOf(value));
            valueLabel.setTextSize(11);
            valueLabel.setGravity(Gravity.CENTER_HORIZONTAL);
            valueLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.neutral_60));

            binContainer.addView(valueLabel);

            View bar = new View(getContext());

            int labelHeightReserved = (int) (20 * density);
            int barHeightPx =
                    (int) ((value / (float) MAX_VALUE) * (containerHeightPx - labelHeightReserved));

            LinearLayout.LayoutParams barParams =
                    new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, barHeightPx);

            bar.setLayoutParams(barParams);

            int colorRes = (value >= threshold) ? R.color.green_40 : R.color.red_40;
            bar.setBackgroundColor(ContextCompat.getColor(requireContext(), colorRes));

            binContainer.addView(bar);
            container.addView(binContainer);
        }
    }

    private void populateElectricityGraph(LinearLayout container, List<Integer> data, int containerHeightPx) {
        container.removeAllViews();

        float density = getResources().getDisplayMetrics().density;

        for (int i = 0; i < data.size(); i++) {
            View bar = new View(getContext());

            int barHeightPx =
                    (int) ((data.get(i) / (float) MAX_ELEC_PRICE) * containerHeightPx);

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(0, barHeightPx, 1.0f);

            params.setMargins((int) (1 * density), 0, (int) (1 * density), 0);
            bar.setLayoutParams(params);

            if (firstCheapestHour == null) {
                setFirstCheapestHour(data);
            }

            int color;

            if (i >= firstCheapestHour && i < firstCheapestHour + 3) {
                color = ContextCompat.getColor(requireContext(), R.color.green_40);
            } else {
                color = ContextCompat.getColor(requireContext(), R.color.teal_40);
            }

            bar.setBackgroundColor(color);
            container.addView(bar);
        }

        updateCurrentTimeLine();
    }

    private List<Integer> applyActionTipSavings(List<Integer> histData) {
        List<Integer> updatedData = new ArrayList<>(histData);

        if (updatedData.isEmpty()) {
            return updatedData;
        }

        float savedKwh = AppSettings.getTotalSavedKwh(requireContext());

        int savedAmount = Math.round(savedKwh * 0.50f);

        int rightBarIndex = updatedData.size() - 1;
        updatedData.set(rightBarIndex, updatedData.get(rightBarIndex) + savedAmount);

        return updatedData;
    }
}