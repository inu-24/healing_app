package com.example.healingjourney;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.LinkedList;
import java.util.Queue;

public class DrawingView extends View {

    private Paint paint;
    private Paint mandalaOverlayPaint;
    private Path path;
    private ArrayList<Path> paths = new ArrayList<>();
    private ArrayList<Paint> paints = new ArrayList<>();
    private ArrayList<Path> undonePaths = new ArrayList<>();
    private ArrayList<Paint> undonePaints = new ArrayList<>();

    private int currentColor = Color.parseColor("#2E7D32");
    private float strokeWidth = 30f;
    private Bitmap backgroundBitmap = null;
    private Bitmap coloringBitmap = null;
    private Canvas coloringCanvas = null;

    public enum Mode { DRAW, FILL }
    private Mode currentMode = Mode.FILL;

    public Map<Integer, Integer> colorUsageCount = new ConcurrentHashMap<>();

    // ✅ Simple value holder for a colour's share of total usage
    public static class ColorShare {
        public final int color;
        public final float percentage;

        public ColorShare(int color, float percentage) {
            this.color = color;
            this.percentage = percentage;
        }
    }

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
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

        mandalaOverlayPaint = new Paint();
        mandalaOverlayPaint.setAntiAlias(true);
        mandalaOverlayPaint.setFilterBitmap(true);
        mandalaOverlayPaint.setXfermode(
                new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
    }

    public void setBackgroundBitmap(Bitmap bitmap) {
        this.backgroundBitmap = bitmap;
        invalidate();
    }

    public void setMode(Mode mode) {
        this.currentMode = mode;
    }

    public Mode getMode() {
        return currentMode;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        coloringBitmap = Bitmap.createBitmap(w, h,
                Bitmap.Config.ARGB_8888);
        coloringCanvas = new Canvas(coloringBitmap);
        coloringCanvas.drawColor(Color.TRANSPARENT,
                PorterDuff.Mode.CLEAR);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.WHITE);

        if (coloringBitmap != null) {
            canvas.drawBitmap(coloringBitmap, 0, 0, null);
        }

        for (int i = 0; i < paths.size(); i++) {
            canvas.drawPath(paths.get(i), paints.get(i));
        }
        canvas.drawPath(path, paint);

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

        if (currentMode == Mode.FILL) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                final int touchX = (int) x;
                final int touchY = (int) y;
                final int fillColor = currentColor;
                new Thread(() -> {
                    try {
                        floodFill(touchX, touchY, fillColor);
                        colorUsageCount.merge(fillColor, 10, Integer::sum);
                    } catch (Exception e) {
                        e.printStackTrace();
                        post(() -> Toast.makeText(getContext(),
                                "Couldn't fill that spot — try tapping a different area 🎨",
                                Toast.LENGTH_SHORT).show());
                    } finally {
                        post(this::invalidate);
                    }
                }).start();
                return true;
            }
        } else {
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
        }
        invalidate();
        return true;
    }

    private synchronized void floodFill(int x, int y, int fillColor) {
        if (coloringBitmap == null) return;

        int width = coloringBitmap.getWidth();
        int height = coloringBitmap.getHeight();

        if (x < 0 || x >= width || y < 0 || y >= height) return;

        // Build reference bitmap
        Bitmap refBitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        Canvas refCanvas = new Canvas(refBitmap);
        refCanvas.drawColor(Color.WHITE);
        refCanvas.drawBitmap(coloringBitmap, 0, 0, null);
        if (backgroundBitmap != null) {
            refCanvas.drawBitmap(backgroundBitmap, null,
                    new RectF(0, 0, width, height), null);
        }

        int targetColor = refBitmap.getPixel(x, y);

        if (isBlackOrDark(targetColor)) return;
        if (targetColor == fillColor) return;

        int[] refPixels = new int[width * height];
        refBitmap.getPixels(refPixels, 0, width, 0, 0, width, height);

        int[] colorPixels = new int[width * height];
        coloringBitmap.getPixels(colorPixels, 0, width, 0, 0, width, height);

        boolean[] visited = new boolean[width * height];
        Queue<Integer> queue = new LinkedList<>();
        queue.add(y * width + x);
        visited[y * width + x] = true;

        while (!queue.isEmpty()) {
            int idx = queue.poll();
            int cx = idx % width;
            int cy = idx / width;

            colorPixels[idx] = fillColor;

            int[][] neighbors = {{cx+1,cy},{cx-1,cy},{cx,cy+1},{cx,cy-1}};
            for (int[] n : neighbors) {
                int nx = n[0], ny = n[1];
                if (nx < 0 || nx >= width || ny < 0 || ny >= height) continue;
                int nIdx = ny * width + nx;
                if (visited[nIdx]) continue;
                int nColor = refPixels[nIdx];
                if (isBlackOrDark(nColor)) continue;
                if (!isSimilarColor(nColor, targetColor)) continue;
                visited[nIdx] = true;
                queue.add(nIdx);
            }
        }

        coloringBitmap.setPixels(colorPixels, 0, width, 0, 0, width, height);
        refBitmap.recycle();
    }

    private boolean isBlackOrDark(int color) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        return (r < 80 && g < 80 && b < 80);
    }

    private boolean isSimilarColor(int c1, int c2) {
        int rDiff = Math.abs(Color.red(c1) - Color.red(c2));
        int gDiff = Math.abs(Color.green(c1) - Color.green(c2));
        int bDiff = Math.abs(Color.blue(c1) - Color.blue(c2));
        return (rDiff + gDiff + bDiff) < 120;
    }

    public void setColor(int color) {
        currentColor = color;
        paint.setColor(color);
        paint.setStrokeWidth(strokeWidth);
        paint.setStyle(Paint.Style.STROKE);
    }

    public void setStrokeWidth(float width) {
        strokeWidth = width;
        paint.setStrokeWidth(width);
    }

    public void setEraser() {
        currentColor = Color.WHITE;
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

    public synchronized void clearCanvas() {
        paths.clear();
        paints.clear();
        undonePaths.clear();
        undonePaints.clear();
        colorUsageCount.clear();
        path.reset();
        if (coloringBitmap != null) {
            coloringBitmap.eraseColor(Color.TRANSPARENT);
        }
        invalidate();
    }

    public Bitmap getBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(
                getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        draw(canvas);
        return bitmap;
    }

    // ✅ Kept for backward compatibility — single most-used colour
    public int getDominantColor() {
        if (colorUsageCount.isEmpty()) return Color.GREEN;
        return colorUsageCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .get()
                .getKey();
    }

    /**
     * ✅ Returns up to the top 3 colours used, each with its share
     * of total colour usage as a percentage (0–100). Sorted by
     * usage descending. Empty list if nothing has been coloured/drawn.
     */
    public List<ColorShare> getColorDistribution() {
        List<ColorShare> result = new ArrayList<>();
        if (colorUsageCount.isEmpty()) return result;

        long total = 0;
        for (int count : colorUsageCount.values()) {
            total += count;
        }
        if (total == 0) return result;

        List<Map.Entry<Integer, Integer>> sorted =
                new ArrayList<>(colorUsageCount.entrySet());
        sorted.sort((a, b) -> b.getValue() - a.getValue());

        int limit = Math.min(3, sorted.size());
        for (int i = 0; i < limit; i++) {
            Map.Entry<Integer, Integer> entry = sorted.get(i);
            float percentage = (entry.getValue() / (float) total) * 100f;
            result.add(new ColorShare(entry.getKey(), percentage));
        }
        return result;
    }

    /**
     * ✅ Roughly what percentage (0–100) of the canvas has been
     * drawn/filled on so far, based on the flood-filled regions
     * plus any freehand strokes.
     */
    public float getCanvasCoverage() {
        if (coloringBitmap == null) return 0f;

        int width = coloringBitmap.getWidth();
        int height = coloringBitmap.getHeight();
        if (width == 0 || height == 0) return 0f;

        Bitmap coverageBitmap = Bitmap.createBitmap(
                width, height, Bitmap.Config.ARGB_8888);
        Canvas coverageCanvas = new Canvas(coverageBitmap);

        // Filled regions
        coverageCanvas.drawBitmap(coloringBitmap, 0, 0, null);
        // Freehand strokes (draw mode)
        for (int i = 0; i < paths.size(); i++) {
            coverageCanvas.drawPath(paths.get(i), paints.get(i));
        }

        int[] pixels = new int[width * height];
        coverageBitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        long coveredCount = 0;
        for (int pixel : pixels) {
            if (Color.alpha(pixel) > 10) coveredCount++;
        }
        coverageBitmap.recycle();

        return (coveredCount / (float) pixels.length) * 100f;
    }
}