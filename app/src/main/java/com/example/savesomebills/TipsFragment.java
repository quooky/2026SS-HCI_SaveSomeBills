package com.example.savesomebills;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class TipsFragment extends Fragment {

    private static final String[] GENERAL_TIPS = {
            "Wasche deine Kleidung bei 30°C statt 60°C – das spart bis zu 60% Energie.",
            "Nutze LED-Lampen – sie verbrauchen bis zu 80% weniger Strom als Glühbirnen.",
            "Schalte Licht konsequent aus, wenn du einen Raum verlässt.",
            "Nutze möglichst Tageslicht statt künstlicher Beleuchtung.",
            "Stoßlüften (5–10 Minuten) ist effizienter als dauerhaft gekippte Fenster.",
            "Drehe die Heizung nachts um 1–2°C herunter – das spart spürbar Energie.",
            "Trockne Wäsche an der Luft statt im Trockner – spart viel Strom.",
            "Kochen mit Deckel spart bis zu 30% Energie.",
            "Vermeide Vorheizen des Backofens, wenn es nicht zwingend nötig ist.",
            "Nutze einen Wasserkocher statt Herd zum Wasser erhitzen.",
            "Fülle Waschmaschine und Geschirrspüler immer komplett.",
            "Stelle deinen Kühlschrank auf 7°C und Gefrierfach auf -18°C ein.",
            "Nutze Mehrfachsteckdosen mit Schalter zur einfachen Stromtrennung.",
            "Ein Ventilator verbraucht deutlich weniger Energie als eine Klimaanlage.",
            "Nutze Energiesparprogramme bei Haushaltsgeräten."
    };

    private static final String[] DEVICE_TIPS = {
            "Geräte im Standby können bis zu 10% deines Stromverbrauchs ausmachen.",
            "Ziehe Ladegeräte aus der Steckdose – sie verbrauchen auch ohne Gerät Strom.",
            "Nutze Steckdosenleisten mit Schalter für TV, Konsole & Co.",
            "Ein Laptop verbraucht deutlich weniger Strom als ein Desktop-PC.",
            "Reduziere die Bildschirmhelligkeit bei Handy und Laptop.",
            "Aktiviere den Energiesparmodus auf Smartphone und Laptop.",
            "Schalte deinen Router nachts aus, wenn du ihn nicht brauchst.",
            "Schalte deinen Fernseher komplett aus statt Standby.",
            "Nutze Eco-Modus bei Waschmaschine und Geschirrspüler.",
            "Lade Geräte nicht dauerhaft bis 100%, das spart Strom und Akku.",
            "Drucker nur einschalten, wenn du ihn wirklich brauchst.",
            "Lade dein Smartphone nicht über Nacht – das spart Strom und schont den Akku."
    };

    private ActionTipsPagerAdapter adapterAction;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_tips, container, false);

        List<TipsPagerAdapter.Tip> generalTips =
                pickRandom(GENERAL_TIPS, "💡 Anwendungs-Tipp", 3);

        List<TipsPagerAdapter.Tip> deviceTips =
                pickRandom(DEVICE_TIPS, "🔌 Geräte-Tipp", 3);

        setupPager(view, R.id.pager_general, R.id.dots_general, generalTips);
        setupPager(view, R.id.pager_device, R.id.dots_device, deviceTips);
        setupActionPager(view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapterAction != null) {
            adapterAction.notifyDataSetChanged();
        }
    }

    private void setupPager(View root, int pagerId, int dotsId, List<TipsPagerAdapter.Tip> tips) {
        ViewPager2 pager = root.findViewById(pagerId);
        pager.setAdapter(new TipsPagerAdapter(tips));
        pager.setOffscreenPageLimit(1);

        pager.setPageTransformer((page, position) -> {
            float scale = 1f - 0.08f * Math.abs(position);
            page.setScaleY(scale);
            page.setAlpha(0.5f + (1f - Math.abs(position)) * 0.5f);
        });

        LinearLayout dots = root.findViewById(dotsId);
        setupDots(dots, tips.size());
        updateDots(dots, 0);

        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateDots(dots, position);
            }
        });
    }

    private List<TipsPagerAdapter.Tip> pickRandom(String[] source, String category, int count) {
        List<String> shuffled = new ArrayList<>();
        Collections.addAll(shuffled, source);
        Collections.shuffle(shuffled);

        List<TipsPagerAdapter.Tip> result = new ArrayList<>();

        for (int i = 0; i < Math.min(count, shuffled.size()); i++) {
            result.add(new TipsPagerAdapter.Tip(category, shuffled.get(i)));
        }

        return result;
    }

    private void setupActionPager(View root) {
        List<ActionTipsPagerAdapter.ActionTip> tips = buildActionTips();
        if (tips.isEmpty()) return;

        Set<String> confirmed = AppSettings.getConfirmedTips(requireContext());

        adapterAction = new ActionTipsPagerAdapter(tips, confirmed);

        ViewPager2 pager = root.findViewById(R.id.pager_action);
        LinearLayout dots = root.findViewById(R.id.dots_action);

        adapterAction.setOnConfirmListener((tip, isConfirmed) -> {
            String saving = AppSettings.formatEnergy(requireContext(), tip.savingKwhPerMonth);

            if (isConfirmed) {
                showBigConfirmationToast(
                        "✅ Tipp erledigt!\nDu sparst " + saving + " pro Monat.",
                        R.color.green_40
                );
            } else {
                showBigConfirmationToast(
                        "↩️ Tipp entfernt\n" + saving + " wurden wieder abgezogen.",
                        R.color.red_40
                );
            }
        });

        adapterAction.setOnDismissListener(position -> {
            if (pager == null || adapterAction == null) return;

            pager.animate()
                    .alpha(0.25f)
                    .translationX(-40f)
                    .setDuration(130)
                    .withEndAction(() -> {
                        int next = position + 1;

                        if (next >= adapterAction.getItemCount()) {
                            next = 0;
                        }

                        pager.setTranslationX(40f);
                        pager.setCurrentItem(next, false);

                        pager.animate()
                                .alpha(1f)
                                .translationX(0f)
                                .setDuration(180)
                                .start();
                    })
                    .start();
        });

        pager.setAdapter(adapterAction);
        pager.setOffscreenPageLimit(1);

        pager.setPageTransformer((page, position) -> {
            float scale = 1f - 0.08f * Math.abs(position);
            page.setScaleY(scale);
            page.setAlpha(0.5f + (1f - Math.abs(position)) * 0.5f);
        });

        setupDots(dots, tips.size());
        updateDots(dots, 0);

        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateDots(dots, position);
            }
        });
    }

    private List<ActionTipsPagerAdapter.ActionTip> buildActionTips() {
        List<ActionTipsPagerAdapter.ActionTip> tips = new ArrayList<>();

        for (Device d : DeviceStorage.loadAll(requireContext())) {
            if (d.wattStandby > 0) {
                double kwhPerMonth = (d.wattStandby * 24.0 * 30) / 1000.0;

                tips.add(new ActionTipsPagerAdapter.ActionTip(
                        "standby_" + d.objectId,
                        d.icon != null ? d.icon : "⚡",
                        d.name + " vom Strom trennen",
                        d.name + " verbraucht im Standby " + d.wattStandby + " W – einfach Stecker ziehen.",
                        kwhPerMonth
                ));
            }
        }

        if (tips.isEmpty()) {
            tips.add(new ActionTipsPagerAdapter.ActionTip(
                    "tip_standby", "🔌", "Standby-Geräte ausschalten",
                    "Fernseher, Konsole und Ladegeräte vollständig ausschalten statt Standby nutzen.",
                    4.0));

            tips.add(new ActionTipsPagerAdapter.ActionTip(
                    "tip_dryer", "🌬️", "Wäsche lufttrocknen",
                    "Verzichte möglichst auf den Trockner. Das spart oft mehr als 10 kWh pro Monat.",
                    15.0));

            tips.add(new ActionTipsPagerAdapter.ActionTip(
                    "tip_washing", "🧺", "Bei 30°C waschen",
                    "Für normale Verschmutzung reichen meist 30°C statt 60°C.",
                    4.0));

            tips.add(new ActionTipsPagerAdapter.ActionTip(
                    "tip_fridge", "🧊", "Kühlschrank auf 7°C einstellen",
                    "7°C reichen für Lebensmittel aus und reduzieren den Stromverbrauch.",
                    2.5));
        }

        return tips.subList(0, Math.min(tips.size(), 4));
    }

        private void showBigConfirmationToast(String message, int colorRes) {
            TextView text = new TextView(requireContext());
            text.setText(message);
            text.setTextColor(Color.WHITE);
            text.setTextSize(18);
            text.setGravity(Gravity.CENTER);
            text.setPadding(42, 28, 42, 28);

            GradientDrawable bg = new GradientDrawable();
            bg.setColor(requireContext().getColor(colorRes));
            bg.setCornerRadius(32);
            text.setBackground(bg);

            Toast toast = new Toast(requireContext());
            toast.setView(text);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setGravity(Gravity.BOTTOM, 0, 180);
            toast.show();
        }

    private void setupDots(LinearLayout container, int count) {
        container.removeAllViews();

        float dp = getResources().getDisplayMetrics().density;
        int size = (int) (8 * dp);
        int margin = (int) (5 * dp);

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
}