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

public class DistribucionIngresosChartView extends View {

    public static class CategoryData {
        private final String categoryName;
        private final double amount;
        private final int color;

        public CategoryData(String categoryName, double amount, int color) {
            this.categoryName = categoryName;
            this.amount = amount;
            this.color = color;
        }

        public String getCategoryName() { return categoryName; }
        public double getAmount() { return amount; }
        public int getColor() { return color; }
    }

    private List<CategoryData> categoryList = new ArrayList<>();
    private double totalSales = 0.0;

    private Paint paintArc;
    private Paint paintInnerCircle;
    private Paint paintTextTotal;
    private Paint paintTextLabel;
    private RectF arcBounds = new RectF();

    public DistribucionIngresosChartView(Context context) {
        super(context);
        init();
    }

    public DistribucionIngresosChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DistribucionIngresosChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paintArc = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintArc.setStyle(Paint.Style.STROKE);
        paintArc.setStrokeWidth(dpToPx(28));

        paintInnerCircle = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintInnerCircle.setColor(Color.parseColor("#101725"));

        paintTextTotal = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintTextTotal.setColor(Color.WHITE);
        paintTextTotal.setTextSize(spToPx(16));
        paintTextTotal.setFakeBoldText(true);
        paintTextTotal.setTextAlign(Paint.Align.CENTER);

        paintTextLabel = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintTextLabel.setColor(Color.parseColor("#94A3B8"));
        paintTextLabel.setTextSize(spToPx(11));
        paintTextLabel.setTextAlign(Paint.Align.CENTER);
    }

    public void setData(List<CategoryData> categories, double totalSales) {
        this.categoryList = categories != null ? categories : new ArrayList<>();
        this.totalSales = totalSales;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        if (width <= 0 || height <= 0) return;

        float centerX = width / 2f;
        float centerY = height / 2f;
        float radius = Math.min(width, height) / 2f - dpToPx(20);

        if (categoryList == null || categoryList.isEmpty() || totalSales <= 0) {
            paintArc.setColor(Color.parseColor("#1E293B"));
            arcBounds.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
            canvas.drawArc(arcBounds, 0, 360, false, paintArc);

            canvas.drawText("S/ 0.00", centerX, centerY, paintTextTotal);
            canvas.drawText("Sin ventas", centerX, centerY + dpToPx(16), paintTextLabel);
            return;
        }

        arcBounds.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);

        float startAngle = -90f;
        for (CategoryData cd : categoryList) {
            float sweepAngle = (float) ((cd.getAmount() / totalSales) * 360f);
            if (sweepAngle < 2) sweepAngle = 2; // Arreglar segmentos mínimos

            paintArc.setColor(cd.getColor());
            canvas.drawArc(arcBounds, startAngle, sweepAngle - 2f, false, paintArc);

            startAngle += sweepAngle;
        }

        // Texto en el centro del anillo
        String strTotal = String.format(Locale.US, "S/ %.0f", totalSales);
        canvas.drawText(strTotal, centerX, centerY - dpToPx(2), paintTextTotal);
        canvas.drawText("Total ventas", centerX, centerY + dpToPx(16), paintTextLabel);
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    private float spToPx(float sp) {
        return sp * getResources().getDisplayMetrics().scaledDensity;
    }
}
