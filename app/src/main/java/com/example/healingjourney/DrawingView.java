package com.example.healingjourney;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DrawingView extends View {

    private Paint paint;
    private Paint mandalaOverlayPaint;
    private Path path;
    private ArrayList<Path> paths = new ArrayList<>();
    private ArrayList<Paint> paints = new ArrayList<>();
    private ArrayList<Path> undonePaths = new ArrayList<>();
    private ArrayList<Paint> undonePaints = new ArrayList<>();

    private int currentColor = Color.parseColor("#2E7D32");
    private float strokeWidth = 30f; // ✅ Bigger default for coloring
    private Bitmap backgroundBitmap = null;

    public Map<Integer, Integer> colorUsageCount = new HashMap<>();

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupPaint();
    }

    private void setupPaint() {
        paint = new Paint();
        paint.setColor(currentColor);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(strokeWidth);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        path = new Path();

        // ✅ Paint for drawing mandala on top of colors
        mandalaOverlayPaint = new Paint();
        mandalaOverlayPaint.setAntiAlias(true);
        mandalaOverlayPaint.setFilterBitmap(true);
    }

    public void setBackgroundBitmap(Bitmap bitmap) {
        this.backgroundBitmap = bitmap;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Step 1: White background
        canvas.drawColor(Color.WHITE);

        // Step 2: Draw color strokes FIRST (below mandala)
        for (int i = 0; i < paths.size(); i++) {
            canvas.drawPath(paths.get(i), paints.get(i));
        }
        canvas.drawPath(path, paint);

        // Step 3: Draw mandala ON TOP so lines stay visible
        if (backgroundBitmap != null) {
            canvas.drawBitmap(
                    backgroundBitmap,
                    null,
                    new RectF(0, 0, getWidth(), getHeight()),
                    mandalaOverlayPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                path.moveTo(x, y);
                return true;
            case MotionEvent.ACTION_MOVE:
                path.lineTo(x, y);
                break;
            case MotionEvent.ACTION_UP:
                paths.add(new Path(path));
                paints.add(new Paint(paint));
                if (currentColor != Color.WHITE) {
                    colorUsageCount.merge(currentColor, 1, Integer::sum);
                }
                path.reset();
                break;
        }
        invalidate();
        return true;
    }

    public void setColor(int color) {
        currentColor = color;
        paint.setColor(color);
        paint.setStrokeWidth(strokeWidth);
        // ✅ Wider stroke for coloring feel
        paint.setStyle(Paint.Style.STROKE);
    }

    public void setStrokeWidth(float width) {
        strokeWidth = width;
        paint.setStrokeWidth(width);
    }

    public void setEraser() {
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(60f);
    }

    public void undo() {
        if (!paths.isEmpty()) {
            undonePaths.add(paths.remove(paths.size() - 1));
            undonePaints.add(paints.remove(paints.size() - 1));
            invalidate();
        }
    }

    public void redo() {
        if (!undonePaths.isEmpty()) {
            paths.add(undonePaths.remove(undonePaths.size() - 1));
            paints.add(undonePaints.remove(undonePaints.size() - 1));
            invalidate();
        }
    }

    public void clearCanvas() {
        paths.clear();
        paints.clear();
        undonePaths.clear();
        undonePaints.clear();
        colorUsageCount.clear();
        path.reset();
        invalidate();
    }

    public Bitmap getBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(
                getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        draw(canvas);
        return bitmap;
    }

    public int getDominantColor() {
        if (colorUsageCount.isEmpty()) return Color.GREEN;
        return colorUsageCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .get()
                .getKey();
    }
}