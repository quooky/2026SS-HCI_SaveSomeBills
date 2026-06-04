package com.example.savesomebills;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Locale;

public class SettingsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);


        // Savings goal
        TextInputEditText inputGoal = view.findViewById(R.id.input_savings_goal);
        float current = AppSettings.getSavingsGoal(requireContext());
        inputGoal.setText(String.format(Locale.getDefault(), "%.0f", current));

        // Energy unit toggle
        MaterialButtonToggleGroup toggleUnit = view.findViewById(R.id.toggle_unit);
        String unit = AppSettings.getEnergyUnit(requireContext());
        switch (unit) {
            case AppSettings.UNIT_PHONE: toggleUnit.check(R.id.btn_unit_phone); break;
            case AppSettings.UNIT_EV:    toggleUnit.check(R.id.btn_unit_ev);    break;
            default:                     toggleUnit.check(R.id.btn_unit_kwh);
        }

        TextView tvUnitInfo = view.findViewById(R.id.tv_unit_info);
        updateUnitInfo(tvUnitInfo, unit);
        toggleUnit.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            String u = unitFromButtonId(checkedId);
            updateUnitInfo(tvUnitInfo, u);
        });

        // Save
        view.findViewById(R.id.btn_save_settings).setOnClickListener(v -> {
            String goalStr = inputGoal.getText() != null ? inputGoal.getText().toString().trim() : "";
            if (!goalStr.isEmpty()) {
                try {
                    AppSettings.setSavingsGoal(requireContext(), Float.parseFloat(goalStr));
                } catch (NumberFormatException ignored) {}
            }
            AppSettings.setEnergyUnit(requireContext(), unitFromButtonId(toggleUnit.getCheckedButtonId()));
            Toast.makeText(requireContext(), "Einstellungen gespeichert", Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        return view;
    }

    private String unitFromButtonId(int id) {
        if (id == R.id.btn_unit_phone) return AppSettings.UNIT_PHONE;
        if (id == R.id.btn_unit_ev)    return AppSettings.UNIT_EV;
        return AppSettings.UNIT_KWH;
    }

    private void updateUnitInfo(TextView tv, String unit) {
        switch (unit) {
            case AppSettings.UNIT_PHONE:
                tv.setText("1 kWh ≈ 67 Handyladungen (à 15 Wh)");
                break;
            case AppSettings.UNIT_EV:
                tv.setText("1 kWh ≈ 6 km E-Auto Reichweite");
                break;
            default:
                tv.setText("Standard-Einheit für Energie");
        }
    }
}
