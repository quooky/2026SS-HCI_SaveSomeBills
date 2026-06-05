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
            "Stoßlüften für 5–10 Minuten spart mehr Heizenergie als dauerhaft gekippte Fenster.",
            "Nutze möglichst Tageslicht statt künstlicher Beleuchtung.",
            "Kochen mit Deckel reduziert den Energieverbrauch deutlich.",
            "Lass warme Speisen erst abkühlen, bevor sie in den Kühlschrank kommen.",
            "Heizkörper sollten nicht durch Möbel oder Vorhänge verdeckt werden.",
            "Eine um 1°C niedrigere Raumtemperatur kann bis zu 6% Heizenergie sparen.",
            "Trockne Wäsche möglichst an der Luft statt im Trockner.",
            "Nutze Restwärme von Herd und Backofen einige Minuten vor Ende der Garzeit.",
            "Öffne den Kühlschrank nur kurz und gezielt, damit weniger Kälte verloren geht.",
            "Nutze LED-Lampen – sie verbrauchen bis zu 80% weniger Strom als Glühbirnen.",
            "Schalte Licht konsequent aus, wenn du einen Raum verlässt.",
            "Stoßlüften (5–10 Minuten) ist effizienter als dauerhaft gekippte Fenster.",
            "Drehe die Heizung nachts um 1–2°C herunter – das spart spürbar Energie.",
            "Fülle Waschmaschine und Geschirrspüler immer komplett.",
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
            "Lade dein Smartphone nicht über Nacht – das spart Strom und schont den Akku.",
            "Ein Ventilator verbraucht deutlich weniger Energie als eine Klimaanlage.",
    };

    private ActionTipsPagerAdapter adapterAction;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tips, container, false);

        List<TipsPagerAdapter.Tip> generalTips = pickRandom(GENERAL_TIPS, "💡 Anwendungs-Tipp", 3);
        List<TipsPagerAdapter.Tip> deviceTips  = pickRandom(DEVICE_TIPS, "🔌 Geräte-Tipp", 3);

        setupPager(view, R.id.pager_general, R.id.dots_general, generalTips);
        setupPager(view, R.id.pager_device, R.id.dots_device, deviceTips);
        setupActionPager(view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapterAction != null) adapterAction.notifyDataSetChanged();
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
            @Override public void onPageSelected(int position) { updateDots(dots, position); }
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
                showBigConfirmationToast("✅ Tipp erledigt!\nDu sparst " + saving + " pro Monat.", R.color.green_40);
                Fragment home = requireActivity().getSupportFragmentManager().findFragmentByTag("home");
                if (home instanceof HomeFragment) {
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction().detach(home).attach(home).commit();
                }
            } else {
                showBigConfirmationToast("↩️ Tipp entfernt\n" + saving + " wurden wieder abgezogen.", R.color.red_40);
            }
        });

        adapterAction.setOnDismissListener(position -> {
            if (pager == null || adapterAction == null) return;
            pager.animate().alpha(0.25f).translationX(-40f).setDuration(130)
                    .withEndAction(() -> {
                        int next = (position + 1) >= adapterAction.getItemCount() ? 0 : position + 1;
                        pager.setTranslationX(40f);
                        pager.setCurrentItem(next, false);
                        pager.animate().alpha(1f).translationX(0f).setDuration(180).start();
                    }).start();
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
            @Override public void onPageSelected(int position) { updateDots(dots, position); }
        });
    }

    private List<ActionTipsPagerAdapter.ActionTip> buildActionTips() {
        List<ActionTipsPagerAdapter.ActionTip> tips = new ArrayList<>();

        for (Device d : DeviceStorage.loadAll(requireContext())) {
            if (d.wattStandby > 0) {
                double kwhPerMonth = (d.wattStandby * 24.0 * 30) / 1000.0;

            }
        }

        if (tips.size() < 4) {
            tips.add(new ActionTipsPagerAdapter.ActionTip(
                    "tip_standby", "🔌", "Standby-Geräte ausschalten",
                    "Fernseher, Konsole und Ladegeräte vollständig ausschalten.",
                    4.0,
                    "Geräte im Standby verbrauchen oft 1–5 W dauerhaft. Fernseher, Spielekonsolen " +
                            "und Ladegeräte summieren sich schnell auf 4–10 kWh pro Monat.\n\n" +
                            "Lösung: Steckerleiste mit Schalter verwenden – ein Klick trennt alle Geräte gleichzeitig.",
                    "https://www.ndr.de/ratgeber/verbraucher/Standby-Modus-Wie-viel-Strom-verbrauchen-Geraete-auf-Abruf,standby104.html"
            ));
            tips.add(new ActionTipsPagerAdapter.ActionTip(
                    "tip_fridge", "🧊", "Kühlschrank auf 7°C einstellen",
                    "7°C reichen aus und reduzieren den Stromverbrauch.",
                    2.5,
                    "Viele Kühlschränke sind unnötig kalt eingestellt. 7°C im Kühlschrank und -18°C im " +
                            "Gefrierfach sind ausreichend für sichere Lebensmittellagerung.\n\n" +
                            "Jedes Grad weniger erhöht den Stromverbrauch um ca. 6%. Zudem: Kühlschrank nie neben " +
                            "Herd oder in der Sonne aufstellen.",
                    "https://www.umweltberatung.at/jetzt-eiskalt-strom-sparen"
            ));
            tips.add(new ActionTipsPagerAdapter.ActionTip(
                    "tip_washing", "🧺", "Wäsche bei 30°C waschen",
                    "Für normale Verschmutzung reichen meist 30°C statt 60°C.",
                    4.0,
                    "Der Großteil des Stromverbrauchs einer Waschmaschine entfällt auf das Aufheizen des Wassers. " +
                            "Bei 30°C statt 60°C sinkt der Energieverbrauch um bis zu 60%.\n\n" +
                            "Moderne Waschmittel sind für niedrige Temperaturen optimiert – die Reinigungsleistung bleibt gleich.",
                    "https://www.vis.bayern.de/produkte_energie/energiesparen/verbrauchertipp_waschen.htm"
            ));
            tips.add(new ActionTipsPagerAdapter.ActionTip(
                    "tip_dryer", "🌬️", "Wäsche lufttrocknen",
                    "Verzichte auf den Trockner – spart oft mehr als 10 kWh/Monat.",
                    15.0,
                    "Ein Wäschetrockner gehört zu den stromhungrigsten Haushaltsgeräten: pro Trocknergang " +
                            "werden ca. 2–3 kWh verbraucht. Bei 5 Ladungen pro Woche sind das über 40 kWh/Monat.\n\n" +
                            "Alternative: Wäscheständer oder Wäscheleine – kostenlos und schonend für die Kleidung.",
                    "https://dry-smart.com/de-at/blogs/blog/energie-sparen-beim-waesche-trocknen"
            ));

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