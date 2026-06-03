package com.example.healingjourney;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class ArtActivity extends AppCompatActivity {

    DrawingView drawingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_art);

        drawingView = findViewById(R.id.drawingView);

        TextView btnBack = findViewById(R.id.btnBack);
        TextView btnSave = findViewById(R.id.btnSave);
        TextView btnEraser = findViewById(R.id.btnEraser);
        TextView btnUndo = findViewById(R.id.btnUndo);
        TextView btnRedo = findViewById(R.id.btnRedo);
        TextView btnClear = findViewById(R.id.btnClear);
        SeekBar seekBrushSize = findViewById(R.id.seekBrushSize);
        Button btnAnalyze = findViewById(R.id.btnAnalyzeEmotion);

        // Color buttons
        findViewById(R.id.colorBlack).setOnClickListener(v ->
                drawingView.setColor(Color.BLACK));
        findViewById(R.id.colorRed).setOnClickListener(v ->
                drawingView.setColor(Color.parseColor("#F44336")));
        findViewById(R.id.colorGreen).setOnClickListener(v ->
                drawingView.setColor(Color.parseColor("#2E7D32")));
        findViewById(R.id.colorBlue).setOnClickListener(v ->
                drawingView.setColor(Color.parseColor("#1565C0")));
        findViewById(R.id.colorYellow).setOnClickListener(v ->
                drawingView.setColor(Color.parseColor("#FFC107")));
        findViewById(R.id.colorPurple).setOnClickListener(v ->
                drawingView.setColor(Color.parseColor("#7B1FA2")));
        findViewById(R.id.colorWhite).setOnClickListener(v ->
                drawingView.setColor(Color.WHITE));

        // Tool buttons
        btnEraser.setOnClickListener(v -> drawingView.setEraser());
        btnUndo.setOnClickListener(v -> drawingView.undo());
        btnRedo.setOnClickListener(v -> drawingView.redo());
        btnClear.setOnClickListener(v -> drawingView.clearCanvas());
        btnBack.setOnClickListener(v -> finish());

        // Brush size
        seekBrushSize.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar s, int progress, boolean b) {
                        drawingView.setStrokeWidth(progress + 5f);
                    }
                    @Override public void onStartTrackingTouch(SeekBar s) {}
                    @Override public void onStopTrackingTouch(SeekBar s) {}
                });

        // Save
        btnSave.setOnClickListener(v ->
                Toast.makeText(this, "Canvas saved! 🎨", Toast.LENGTH_SHORT).show());

        // Analyze Emotion
        btnAnalyze.setOnClickListener(v ->
                startActivity(new Intent(ArtActivity.this,EmotionActivity.class)));
    }
}