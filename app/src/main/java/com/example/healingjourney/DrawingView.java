package com.example.healingjourney;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;

public class DrawingView extends View {

    private Paint paint;
    private Path path;
    private final ArrayList<Path> paths = new ArrayList<>();
    private final ArrayList<Paint> paints = new ArrayList<>();
    private final ArrayList<Path> undonePaths = new ArrayList<>();
    private final ArrayList<Paint> undonePaints = new ArrayList<>();

    private int currentColor = Color.BLACK;
    private float strokeWidth = 10f;
    private boolean isEraser = false;

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
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.WHITE);
        for (int i = 0; i < paths.size(); i++) {
            canvas.drawPath(paths.get(i), paints.get(i));
        }
        canvas.drawPath(path, paint);
    }

    @SuppressLint("ClickableViewAccessibility")
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
                path.reset();
                break;
        }
        invalidate();
        return true;
    }

    public void setColor(int color) {
        currentColor = color;
        isEraser = false;
        paint.setColor(color);
        paint.setStrokeWidth(strokeWidth);
    }

    public void setStrokeWidth(float width) {
        strokeWidth = width;
        paint.setStrokeWidth(width);
    }

    public void setEraser() {
        isEraser = true;
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(40f);
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
        path.reset();
        invalidate();
    }

    public Bitmap getBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        draw(canvas);
        return bitmap;
    }
}