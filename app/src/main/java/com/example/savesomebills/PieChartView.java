package com.example.savesomebills;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class PieChartView extends View {

    public static class PieEntry {
        public String label;
        public float value;
        public int color;

        public PieEntry(String label, float value, int color) {
            this.label = label;
            this.value = value;
            this.color = color;
        }
    }

    private List<PieEntry> entries = new ArrayList<>();
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private RectF rectF = new RectF();

    public PieChartView(Context context) {
        super(context);
    }

    public PieChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setEntries(List<PieEntry> entries) {
        this.entries = entries;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (entries == null || entries.isEmpty()) return;

        float total = 0;
        for (PieEntry entry : entries) total += entry.value;

        float startAngle = 0;
        float width = getWidth();
        float height = getHeight();
        float radius = Math.min(width, height) / 2 * 0.8f;
        rectF.set(width / 2 - radius, height / 2 - radius, width / 2 + radius, height / 2 + radius);

        for (PieEntry entry : entries) {
            float sweepAngle = (entry.value / total) * 360f;
            paint.setColor(entry.color);
            canvas.drawArc(rectF, startAngle, sweepAngle, true, paint);
            startAngle += sweepAngle;
        }
    }
}
