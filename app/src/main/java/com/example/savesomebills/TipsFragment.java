package com.example.savesomebills;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Random;

import androidx.fragment.app.Fragment;

public class TipsFragment extends Fragment {

    private final String[] generalTips = {
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
            "Vermeide unnötiges Aufladen von Geräten (z.B. Handy über Nacht).",
            "Halte Türen zu beheizten Räumen geschlossen, um Wärmeverlust zu vermeiden.",
            "Reinige regelmäßig Filter von Geräten (z.B. Trockner), für bessere Effizienz.",
            "Nutze Energiesparprogramme bei Haushaltsgeräten.",
            "Vermeide Standby-Modus – vollständig ausschalten spart jährlich viel Strom.",
            "Stelle deinen Kühlschrank auf 7°C und Gefrierfach auf -18°C ein.",
            "Öffne den Kühlschrank möglichst kurz – jedes Öffnen verbraucht Energie.",
            "Platziere den Kühlschrank nicht neben Herd oder Heizung.",
            "Nutze Mehrfachsteckdosen mit Schalter zur einfachen Stromtrennung."
    };

    private final String[] deviceTips = {
            "Geräte im Standby können bis zu 10% deines Stromverbrauchs ausmachen.",
            "Ziehe Ladegeräte aus der Steckdose – sie verbrauchen auch ohne Gerät Strom.",
            "Nutze Steckdosenleisten mit Schalter für TV, Konsole & Co.",
            "Ein Laptop verbraucht deutlich weniger Strom als ein Desktop-PC.",
            "Reduziere die Bildschirmhelligkeit bei Handy und Laptop.",
            "Aktiviere den Energiesparmodus auf Smartphone und Laptop.",
            "Schalte deinen Router nachts aus, wenn du ihn nicht brauchst.",
            "Moderne Geräte mit hoher Energieeffizienzklasse sparen langfristig Geld.",
            "Ein alter Kühlschrank kann doppelt so viel Strom verbrauchen wie ein neuer.",
            "Vermeide Dauerbetrieb von Spielkonsolen im Ruhemodus.",
            "Schalte deinen Fernseher komplett aus statt Standby.",
            "Nutze Eco-Modus bei Waschmaschine und Geschirrspüler.",
            "Vermeide unnötige Nutzung von Klimaanlagen.",
            "Ein Ventilator verbraucht deutlich weniger Energie als eine Klimaanlage.",
            "Lade Geräte nicht dauerhaft bis 100%, das spart Strom und Akku.",
            "Verwende Timer oder Smart-Steckdosen für automatische Abschaltung.",
            "Halte Software aktuell – effizientere Prozesse sparen Energie.",
            "Drucker nur einschalten, wenn du ihn wirklich brauchst.",
            "Vermeide dauerhaft angeschlossene externe Geräte am Laptop.",
            "Nutze Energiesparfunktionen bei Fernsehern und Monitoren."
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_tips, container, false);

        TextView tip1 = view.findViewById(R.id.tip1);
        TextView tip2 = view.findViewById(R.id.tip2);
        TextView tip3 = view.findViewById(R.id.tip3);

        Random random = new Random();

        int index1 = random.nextInt(generalTips.length);
        int index2 = random.nextInt(deviceTips.length);
        int index3;

        do {
            index3 = random.nextInt(generalTips.length);
        } while (index3 == index1);

        tip1.setText("💡 " + generalTips[index1]);
        tip2.setText("🔌 " + deviceTips[index2]);
        tip3.setText("💡 " + generalTips[index3]);

        return view;
    }
}