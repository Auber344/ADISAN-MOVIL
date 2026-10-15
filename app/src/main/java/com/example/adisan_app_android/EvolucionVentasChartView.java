package com.example.adisan_app_android;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EvolucionVentasChartView extends View {

    public static class BarData {
        private final String label;
        private final double amount;

        public BarData(String label, double amount) {
            this.label = label;
            this.amount = amount;
        }

        public String getLabel() { return label; }
        public double getAmount() { return amount; }
    }

    private List<BarData> dataList = new ArrayList<>();

    private Paint paintAxis;
    private Paint paintGrid;
    private Paint paintBar;
    private Paint paintActiveBar;
    private Paint paintText;
    private Paint paintTooltipBg;
    private Paint paintTooltipText;
    private Paint paintGlowDot;

    private RectF barRect = new RectF();
    private RectF tooltipRect = new RectF();

    public EvolucionVentasChartView(Context context) {
        super(context);
        init();
    }

    public EvolucionVentasChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EvolucionVentasChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paintAxis = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintAxis.setColor(Color.parseColor("#64748B"));
        paintAxis.setTextSize(spToPx(10));

        paintGrid = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintGrid.setColor(Color.parseColor("#1E293B"));
        paintGrid.setStrokeWidth(dpToPx(1));

        paintBar = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintBar.setColor(Color.parseColor("#2563EB"));

        paintActiveBar = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintActiveBar.setColor(Color.parseColor("#38BDF8"));

        paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintText.setColor(Color.parseColor("#94A3B8"));
        paintText.setTextSize(spToPx(10));
        paintText.setTextAlign(Paint.Align.CENTER);

        paintTooltipBg = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintTooltipBg.setColor(Color.parseColor("#0F172A"));

        paintTooltipText = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintTooltipText.setColor(Color.WHITE);
        paintTooltipText.setTextSize(spToPx(10));
        paintTooltipText.setFakeBoldText(true);
        paintTooltipText.setTextAlign(Paint.Align.CENTER);

        paintGlowDot = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintGlowDot.setColor(Color.WHITE);
    }

    public void setData(List<BarData> data) {
        this.dataList = data != null ? data : new ArrayList<>();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        if (width <= 0 || height <= 0) return;

        if (dataList == null || dataList.isEmpty()) {
            paintText.setTextAlign(Paint.Align.CENTER);
            paintText.setTextSize(spToPx(12));
            canvas.drawText("Sin ventas registradas en este periodo", width / 2f, height / 2f, paintText);
            return;
        }

        float paddingLeft = dpToPx(36);
        float paddingRight = dpToPx(16);
        float paddingTop = dpToPx(32);
        float paddingBottom = dpToPx(28);

        float chartWidth = width - paddingLeft - paddingRight;
        float chartHeight = height - paddingTop - paddingBottom;

        // Calcular valor máximo
        double maxVal = 0;
        int peakIndex = -1;
        for (int i = 0; i < dataList.size(); i++) {
            double amt = dataList.get(i).getAmount();
            if (amt > maxVal) {
                maxVal = amt;
                peakIndex = i;
            }
        }

        if (maxVal <= 0) maxVal = 100;
        double niceMax = Math.ceil(maxVal / 100.0) * 100;
        if (niceMax < 100) niceMax = 100;

        // Dibujar líneas horizontales de cuadrícula y eje Y
        int steps = 3;
        for (int i = 0; i <= steps; i++) {
            float y = paddingTop + chartHeight - (i * chartHeight / steps);
            double val = (niceMax / steps) * i;

            canvas.drawLine(paddingLeft, y, width - paddingRight, y, paintGrid);

            String yLabel = String.format(Locale.US, "%.0f", val);
            paintAxis.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(yLabel, paddingLeft - dpToPx(6), y + dpToPx(3), paintAxis);
        }

        // Dibujar barras
        int n = dataList.size();
        float stepX = chartWidth / n;
        float barWidth = Math.min(stepX * 0.5f, dpToPx(14));

        for (int i = 0; i < n; i++) {
            BarData bd = dataList.get(i);
            float centerX = paddingLeft + (i * stepX) + (stepX / 2f);

            float valHeight = (float) ((bd.getAmount() / niceMax) * chartHeight);
            if (valHeight < dpToPx(2)) valHeight = dpToPx(2);

            float top = paddingTop + chartHeight - valHeight;
            float bottom = paddingTop + chartHeight;
            float left = centerX - (barWidth / 2f);
            float right = centerX + (barWidth / 2f);

            barRect.set(left, top, right, bottom);
            Paint curPaint = (i == peakIndex && bd.getAmount() > 0) ? paintActiveBar : paintBar;
            canvas.drawRoundRect(barRect, dpToPx(4), dpToPx(4), curPaint);

            // Dibujar etiqueta X (solo algunas o todas si caben)
            if (n <= 8 || i % (n / 4) == 0 || i == n - 1) {
                paintText.setTextAlign(Paint.Align.CENTER);
                canvas.drawText(bd.getLabel(), centerX, height - dpToPx(6), paintText);
            }

            // Si es la barra pico, dibujar punto brillante y tooltip
            if (i == peakIndex && bd.getAmount() > 0) {
                canvas.drawCircle(centerX, top, dpToPx(4), paintGlowDot);

                String tooltipStr = String.format(Locale.US, "S/ %.2f", bd.getAmount());
                float ttWidth = paintTooltipText.measureText(tooltipStr) + dpToPx(16);
                float ttHeight = dpToPx(22);
                float ttLeft = centerX - (ttWidth / 2f);
                if (ttLeft < paddingLeft) ttLeft = paddingLeft;
                if (ttLeft + ttWidth > width - paddingRight) ttLeft = width - paddingRight - ttWidth;
                float ttTop = top - ttHeight - dpToPx(8);
                if (ttTop < 0) ttTop = 0;

                tooltipRect.set(ttLeft, ttTop, ttLeft + ttWidth, ttTop + ttHeight);
                canvas.drawRoundRect(tooltipRect, dpToPx(11), dpToPx(11), paintTooltipBg);

                canvas.drawText(tooltipStr, tooltipRect.centerX(), tooltipRect.centerY() + dpToPx(3.5f), paintTooltipText);
            }
        }
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    private float spToPx(float sp) {
        return sp * getResources().getDisplayMetrics().scaledDensity;
    }
}
