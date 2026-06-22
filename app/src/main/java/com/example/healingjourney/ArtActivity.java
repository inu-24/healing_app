package com.example.healingjourney;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class ArtActivity extends BaseActivity {

    DrawingView drawingView;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    TextView btnFill;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_art);
        setupBottomNav();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        drawingView = findViewById(R.id.drawingView);

        // Load mandala if selected
        int mandalaId = getIntent().getIntExtra("mandalaId", -1);
        if (mandalaId != -1) {
            Bitmap mandala = BitmapFactory.decodeResource(
                    getResources(), mandalaId);
            drawingView.setBackgroundBitmap(mandala);
        }

        TextView btnBack = findViewById(R.id.btnBack);
        TextView btnSave = findViewById(R.id.btnSave);
        TextView btnEraser = findViewById(R.id.btnEraser);
        TextView btnUndo = findViewById(R.id.btnUndo);
        TextView btnRedo = findViewById(R.id.btnRedo);
        TextView btnClear = findViewById(R.id.btnClear);
        btnFill = findViewById(R.id.btnFill);
        SeekBar seekBrushSize = findViewById(R.id.seekBrushSize);
        Button btnAnalyze = findViewById(R.id.btnAnalyzeEmotion);

        // ✅ Default fill mode
        drawingView.setMode(DrawingView.Mode.FILL);
        btnFill.setText("🪣");

        // ✅ Toggle Fill / Draw mode
        btnFill.setOnClickListener(v -> {
            if (drawingView.getMode() == DrawingView.Mode.FILL) {
                drawingView.setMode(DrawingView.Mode.DRAW);
                btnFill.setText("✏️");
                Toast.makeText(this, "Draw mode ✏️",
                        Toast.LENGTH_SHORT).show();
            } else {
                drawingView.setMode(DrawingView.Mode.FILL);
                btnFill.setText("🪣");
                Toast.makeText(this, "Fill mode 🪣",
                        Toast.LENGTH_SHORT).show();
            }
        });

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

        btnEraser.setOnClickListener(v -> drawingView.setEraser());
        btnUndo.setOnClickListener(v -> drawingView.undo());
        btnRedo.setOnClickListener(v -> drawingView.redo());
        btnClear.setOnClickListener(v -> drawingView.clearCanvas());
        btnBack.setOnClickListener(v -> finish());

        seekBrushSize.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar s, int p, boolean b) {
                        drawingView.setStrokeWidth(p + 25f);
                    }
                    @Override public void onStartTrackingTouch(SeekBar s) {}
                    @Override public void onStopTrackingTouch(SeekBar s) {}
                });

        btnSave.setOnClickListener(v -> saveDrawingLocally());

        btnAnalyze.setOnClickListener(v -> {
            int dominantColor = drawingView.getDominantColor();
            Intent intent = new Intent(
                    ArtActivity.this, EmotionActivity.class);
            intent.putExtra("dominantColor", dominantColor);
            startActivity(intent);
        });
    }

    private void saveDrawingLocally() {
        try {
            Toast.makeText(this, "Saving... 💾",
                    Toast.LENGTH_SHORT).show();

            Bitmap bitmap = drawingView.getBitmap();
            String fileName = "artwork_" +
                    System.currentTimeMillis() + ".png";

            java.io.FileOutputStream fos =
                    openFileOutput(fileName, MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();

            if (mAuth.getCurrentUser() != null) {
                String userId = mAuth.getCurrentUser().getUid();
                Map<String, Object> artData = new HashMap<>();
                artData.put("fileName", fileName);
                artData.put("timestamp",
                        com.google.firebase.Timestamp.now());
                artData.put("userId", userId);

                db.collection("artworks")
                        .add(artData)
                        .addOnSuccessListener(ref ->
                                Toast.makeText(this,
                                        "Artwork saved! 🎨",
                                        Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e ->
                                Toast.makeText(this,
                                        "Error: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show());
            }
        } catch (Exception e) {
            Toast.makeText(this,
                    "Save failed: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }
}