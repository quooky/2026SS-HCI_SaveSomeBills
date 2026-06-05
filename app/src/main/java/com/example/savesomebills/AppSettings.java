package com.example.savesomebills;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class AppSettings {

    private static final String PREFS               = "app_prefs";
    public  static final String KEY_SAVINGS_GOAL    = "savings_goal";
    public  static final String KEY_ENERGY_UNIT     = "energy_unit";
    private static final String KEY_CONFIRMED_TIPS  = "confirmed_tips";
    private static final String KEY_CONFIRMED_DATE  = "confirmed_tips_date";
    private static final String KEY_TOTAL_SAVED_KWH = "total_saved_kwh";

    public static final String UNIT_KWH   = "kwh";
    public static final String UNIT_PHONE = "phone";
    public static final String UNIT_EV    = "ev";

    private static final double WH_PER_PHONE = 15.0;
    private static final double KM_PER_KWH   = 6.0;

    public static float getSavingsGoal(Context context) {
        return prefs(context).getFloat(KEY_SAVINGS_GOAL, 50f);
    }

    public static void setSavingsGoal(Context context, float goal) {
        prefs(context).edit().putFloat(KEY_SAVINGS_GOAL, goal).apply();
    }

    public static String getEnergyUnit(Context context) {
        return prefs(context).getString(KEY_ENERGY_UNIT, UNIT_KWH);
    }

    public static void setEnergyUnit(Context context, String unit) {
        prefs(context).edit().putString(KEY_ENERGY_UNIT, unit).apply();
    }

    public static String formatEnergy(Context context, double kwh) {
        switch (getEnergyUnit(context)) {
            case UNIT_PHONE: {
                int n = (int) Math.round(kwh * 1000.0 / WH_PER_PHONE);
                return "~" + n + " 📱";
            }
            case UNIT_EV: {
                int km = (int) Math.round(kwh * KM_PER_KWH);
                return "~" + km + " km ⚡";
            }
            default:
                return String.format(Locale.getDefault(), "~%.1f kWh", kwh);
        }
    }

    public static Set<String> getConfirmedTips(Context context) {
        SharedPreferences p = prefs(context);
        if (!today().equals(p.getString(KEY_CONFIRMED_DATE, ""))) {
            p.edit()
                    .remove(KEY_CONFIRMED_TIPS)
                    .putString(KEY_CONFIRMED_DATE, today())
                    .apply();
            return new HashSet<>();
        }
        return new HashSet<>(p.getStringSet(KEY_CONFIRMED_TIPS, new HashSet<>()));
    }

    public static void confirmTip(Context context, String tipId) {
        Set<String> confirmed = getConfirmedTips(context);
        confirmed.add(tipId);
        prefs(context).edit()
                .putStringSet(KEY_CONFIRMED_TIPS, confirmed)
                .putString(KEY_CONFIRMED_DATE, today())
                .apply();
    }

    public static void addSavedKwh(Context context, double kwh) {
        float current = prefs(context).getFloat(KEY_TOTAL_SAVED_KWH, 0f);
        prefs(context).edit()
                .putFloat(KEY_TOTAL_SAVED_KWH, current + (float) kwh)
                .apply();
    }

    public static float getTotalSavedKwh(Context context) {
        return prefs(context).getFloat(KEY_TOTAL_SAVED_KWH, 0f);
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    private static String today() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }
}