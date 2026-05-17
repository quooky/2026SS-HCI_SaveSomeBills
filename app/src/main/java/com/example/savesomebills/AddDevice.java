package com.example.savesomebills;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AddDevice extends AppCompatActivity {

    private static final int    REQUEST_CAMERA  = 1001;
    private static final double PRICE_PER_KWH   = 0.30;
    private static final double MAX_COST_DAY    = 3.0;
    private static final String PREFS_NAME       = "app_prefs";
    private static final String KEY_ROOMS        = "rooms";
    private static final String PLACEHOLDER      = "Raum wählen...";
    private static final String ADD_ROOM         = "+ Hinzufügen";

    private Spinner           spinnerRoom;
    private TextInputEditText inputName, inputOnHours, inputStandbyHours;
    private TextView          tvWattResult, tvCostPerHour;
    private View              gradientBarContainer, gradientBarIndicator;
    private ArrayAdapter<String> roomAdapter;
    private List<String>      rooms = new ArrayList<>();

    private int wattOn      = 0;
    private int wattStandby = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_device);

        spinnerRoom       = findViewById(R.id.spinner_room);
        inputName         = findViewById(R.id.input_name);
        inputOnHours      = findViewById(R.id.input_on_hours);
        inputStandbyHours = findViewById(R.id.input_standby_hours);
        tvWattResult          = findViewById(R.id.tv_watt_result);
        tvCostPerHour         = findViewById(R.id.tv_cost_per_hour);
        gradientBarContainer  = findViewById(R.id.gradient_bar_container);
        gradientBarIndicator  = findViewById(R.id.gradient_bar_indicator);

        loadRooms();
        setupSpinner();

        TextWatcher watcher = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            public void afterTextChanged(Editable s) { updateForecast(); }
        };
        inputOnHours.addTextChangedListener(watcher);
        inputStandbyHours.addTextChangedListener(watcher);

        findViewById(R.id.imageButton3).setOnClickListener(v -> finish());
        findViewById(R.id.btn_cancel).setOnClickListener(v -> finish());

        findViewById(R.id.btn_easy_scan).setOnClickListener(v -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, REQUEST_CAMERA);
        });

        findViewById(R.id.btn_confirm).setOnClickListener(v -> {
            String name     = inputName.getText() != null ? inputName.getText().toString().trim() : "";
            String onStr    = inputOnHours.getText() != null ? inputOnHours.getText().toString().trim() : "";
            String sbStr    = inputStandbyHours.getText() != null ? inputStandbyHours.getText().toString().trim() : "";
            int    pos      = spinnerRoom.getSelectedItemPosition();
            String selected = pos > 0 ? (String) spinnerRoom.getSelectedItem() : "";

            if (name.isEmpty() || onStr.isEmpty() || sbStr.isEmpty()) {
                Toast.makeText(this, "Bitte alle Felder ausfüllen", Toast.LENGTH_SHORT).show();
                return;
            }
            if (pos == 0 || selected.equals(ADD_ROOM)) {
                Toast.makeText(this, "Bitte einen Raum auswählen", Toast.LENGTH_SHORT).show();
                return;
            }
            if (wattOn == 0) {
                Toast.makeText(this, "Bitte erst EasyScan verwenden", Toast.LENGTH_SHORT).show();
                return;
            }

            int onHours      = Integer.parseInt(onStr);
            int standbyHours = Integer.parseInt(sbStr);

            if (onHours + standbyHours > 24) {
                Toast.makeText(this, "ON + Standby darf nicht mehr als 24h sein", Toast.LENGTH_SHORT).show();
                return;
            }

            DeviceStorage.save(this, new Device(name, selected, onHours, standbyHours, wattOn, wattStandby));
            Toast.makeText(this, "Gerät gespeichert", Toast.LENGTH_SHORT).show();
            finish();
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // ── Spinner ───────────────────────────────────────────────────────────────

    private void setupSpinner() {
        List<String> items = buildSpinnerItems();
        roomAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, items);
        roomAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerRoom.setAdapter(roomAdapter);
        spinnerRoom.setSelection(0);

        spinnerRoom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == roomAdapter.getCount() - 1) {
                    spinnerRoom.setSelection(0);
                    showAddRoomDialog();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private List<String> buildSpinnerItems() {
        List<String> items = new ArrayList<>();
        items.add(PLACEHOLDER);
        items.addAll(rooms);
        items.add(ADD_ROOM);
        return items;
    }

    private void showAddRoomDialog() {
        EditText input = new EditText(this);
        input.setHint("z.B. Wohnzimmer");
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        input.setPadding(48, 32, 48, 32);

        new AlertDialog.Builder(this)
            .setTitle("Neuen Raum hinzufügen")
            .setView(input)
            .setPositiveButton("Hinzufügen", (dialog, which) -> {
                String room = input.getText().toString().trim();
                if (!room.isEmpty() && !rooms.contains(room)) {
                    rooms.add(room);
                    saveRooms();
                    setupSpinner();
                    spinnerRoom.setSelection(rooms.size()); // select new room
                }
            })
            .setNegativeButton("Abbrechen", null)
            .show();
    }

    // ── Room persistence ──────────────────────────────────────────────────────

    private void loadRooms() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String json = prefs.getString(KEY_ROOMS, "[]");
        rooms = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) rooms.add(array.getString(i));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveRooms() {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .edit()
            .putString(KEY_ROOMS, new JSONArray(rooms).toString())
            .apply();
    }

    // ── Camera & forecast ─────────────────────────────────────────────────────

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK) {
            Random random = new Random();
            wattOn      = 10 + random.nextInt(491);
            wattStandby = 1  + random.nextInt(20);
            tvWattResult.setText("⚡ " + wattOn + "W ON  |  " + wattStandby + "W Standby");
            tvWattResult.setTextColor(getColor(R.color.teal_40));
            updateForecast();
        }
    }

    private void updateForecast() {
        if (wattOn == 0) return;
        try {
            int onH  = inputOnHours.getText().toString().isEmpty() ? 0 : Integer.parseInt(inputOnHours.getText().toString());
            int sbH  = inputStandbyHours.getText().toString().isEmpty() ? 0 : Integer.parseInt(inputStandbyHours.getText().toString());
            double costPerHour = (wattOn / 1000.0) * PRICE_PER_KWH;
            double costPerDay  = ((onH * wattOn + sbH * wattStandby) / 1000.0) * PRICE_PER_KWH;
            tvCostPerHour.setText(String.format("%.2f €/h  |  %.2f €/Tag", costPerHour, costPerDay));

            float fraction = (float) Math.min(costPerDay / MAX_COST_DAY, 1.0);
            gradientBarIndicator.setVisibility(View.VISIBLE);
            gradientBarContainer.post(() -> {
                float max = gradientBarContainer.getWidth() - gradientBarIndicator.getWidth();
                gradientBarIndicator.setTranslationX(max * fraction);
            });
        } catch (NumberFormatException ignored) {}
    }
}
