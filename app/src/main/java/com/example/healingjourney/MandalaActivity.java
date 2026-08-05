package com.example.healingjourney;

import android.content.Intent;
import android.os.Bundle;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.view.View;
import android.view.ViewGroup;
import android.content.Context;

public class MandalaActivity extends BaseActivity {

    // ✅ Mandala sets — a different set of designs per mood/stress level
    int[] highStressMandalas = {
            R.drawable.mandala_high_1,
            R.drawable.mandala_high_2,
            R.drawable.mandala_high_3,
            R.drawable.mandala_high_4,
            R.drawable.mandala_high_5
    };

    int[] mediumStressMandalas = {
            R.drawable.mandala_medium_1,
            R.drawable.mandala_medium_2,
            R.drawable.mandala_medium_3,
            R.drawable.mandala_medium_4,
            R.drawable.mandala_medium_5
    };

    int[] lowStressMandalas = {
            R.drawable.mandala_low_1,
            R.drawable.mandala_low_2,
            R.drawable.mandala_low_3,
            R.drawable.mandala_low_4,
            R.drawable.mandala_low_5
    };

    int[] mandalaImages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mandala);

        TextView btnBack = findViewById(R.id.btnBack);
        TextView tvMoodTip = findViewById(R.id.tvMoodTip);
        GridView gridMandalas = findViewById(R.id.gridMandalas);

        String stressLevel = getIntent().getStringExtra("stressLevel");
        if (stressLevel == null) stressLevel = "Medium";

        switch (stressLevel) {
            case "High":
                mandalaImages = highStressMandalas;
                break;
            case "Low":
                mandalaImages = lowStressMandalas;
                break;
            default:
                mandalaImages = mediumStressMandalas;
        }

        if (tvMoodTip != null) {
            switch (stressLevel) {
                case "High":
                    tvMoodTip.setText(getString(R.string.feeling_stressed_these_gentle_mandalas) +
                            getString(R.string.can_help_you_slow_down_and_breathe));
                    break;
                case "Low":
                    tvMoodTip.setText("You're in a good place! Pick any " +
                            getString(R.string.mandala_that_inspires_you_today));
                    break;
                default:
                    tvMoodTip.setText(getString(R.string.take_a_moment_for_yourself_choose) +
                            "whichever mandala speaks to you. 🎨");
            }
        }

        btnBack.setOnClickListener(v -> finish());

        // Set up grid adapter
        gridMandalas.setAdapter(new MandalaAdapter(this));

        // When mandala is clicked → open ArtActivity with selected mandala
        gridMandalas.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(MandalaActivity.this, ArtActivity.class);
            intent.putExtra("mandalaId", mandalaImages[position]);
            startActivity(intent);
            finish();
        });
    }

    // Adapter for mandala grid
    class MandalaAdapter extends BaseAdapter {
        Context context;

        MandalaAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() { return mandalaImages.length; }

        @Override
        public Object getItem(int position) { return mandalaImages[position]; }

        @Override
        public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(context);
                imageView.setLayoutParams(new ViewGroup.LayoutParams(400, 400));
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageView.setPadding(8, 8, 8, 8);
            } else {
                imageView = (ImageView) convertView;
            }
            imageView.setImageResource(mandalaImages[position]);
            return imageView;
        }
    }
}