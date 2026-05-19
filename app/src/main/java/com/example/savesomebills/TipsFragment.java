package com.example.savesomebills;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        "Halte Türen zu beheizten Räumen geschlossen, um Wärmeverlust zu vermeiden.",
        "Stelle deinen Kühlschrank auf 7°C und Gefrierfach auf -18°C ein.",
        "Öffne den Kühlschrank möglichst kurz – jedes Öffnen verbraucht Energie.",
        "Nutze Mehrfachsteckdosen mit Schalter zur einfachen Stromtrennung.",
        "Vermeide Standby-Modus – vollständig ausschalten spart jährlich viel Strom.",
        "Ein Ventilator verbraucht deutlich weniger Energie als eine Klimaanlage.",
        "Platziere den Kühlschrank nicht neben Herd oder Heizung.",
        "Reinige regelmäßig Filter von Geräten (z.B. Trockner), für bessere Effizienz.",
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
        "Moderne Geräte mit hoher Energieeffizienzklasse sparen langfristig Geld.",
        "Schalte deinen Fernseher komplett aus statt Standby.",
        "Nutze Eco-Modus bei Waschmaschine und Geschirrspüler.",
        "Lade Geräte nicht dauerhaft bis 100%, das spart Strom und Akku.",
        "Verwende Timer oder Smart-Steckdosen für automatische Abschaltung.",
        "Drucker nur einschalten, wenn du ihn wirklich brauchst.",
        "Vermeide dauerhaft angeschlossene externe Geräte am Laptop.",
        "Nutze Energiesparfunktionen bei Fernsehern und Monitoren.",
        "Lade dein Smartphone nicht über Nacht – das spart Strom und schont den Akku."
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tips, container, false);

        List<TipsPagerAdapter.Tip> generalTips = pickRandom(GENERAL_TIPS, "💡 Anwendungs-Tipp", 3);
        List<TipsPagerAdapter.Tip> deviceTips  = pickRandom(DEVICE_TIPS,  "🔌 Geräte-Tipp",    2);

        setupPager(view, R.id.pager_general, R.id.dots_general, generalTips);
        setupPager(view, R.id.pager_device,  R.id.dots_device,  deviceTips);

        return view;
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
        for (int i = 0; i < count; i++) {
            result.add(new TipsPagerAdapter.Tip(category, shuffled.get(i)));
        }
        return result;
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
