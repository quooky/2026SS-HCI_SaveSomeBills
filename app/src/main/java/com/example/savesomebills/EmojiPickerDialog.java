package com.example.savesomebills;

import android.content.Context;
import android.view.Gravity;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import java.util.function.Consumer;

public class EmojiPickerDialog {

    public static final String[] DEVICE_EMOJIS = {
        "⚡", "💡", "📺", "🖥️", "🎮", "🔌", "❄️", "🫖", "🧺", "🔊",
        "☕", "🖨️", "📱", "🌡️", "🏋️", "🕹️", "🖱️", "🔋", "💻", "📡"
    };

    public static final String[] ROOM_EMOJIS = {
        "🏠", "🛋️", "🛏️", "🚿", "🍳", "🧹", "🪴", "📚", "🏢", "🚗",
        "🌿", "⛺", "🏗️", "🌊", "🔑", "🛁", "🎨", "🏋️", "🎵", "🖼️"
    };

    public static void show(Context context, String[] emojis, Consumer<String> onPick) {
        float dp = context.getResources().getDisplayMetrics().density;
        int pad  = (int) (12 * dp);
        int size = (int) (56 * dp);

        GridLayout grid = new GridLayout(context);
        grid.setColumnCount(5);
        grid.setPadding(pad, pad, pad, pad);

        AlertDialog dialog = new AlertDialog.Builder(context).setView(grid).create();

        for (String emoji : emojis) {
            TextView tv = new TextView(context);
            tv.setText(emoji);
            tv.setTextSize(28);
            tv.setGravity(Gravity.CENTER);
            tv.setClickable(true);
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = size;
            lp.height = size;
            tv.setLayoutParams(lp);
            tv.setOnClickListener(v -> {
                onPick.accept(emoji);
                dialog.dismiss();
            });
            grid.addView(tv);
        }

        dialog.show();
    }
}
