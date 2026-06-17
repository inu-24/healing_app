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

    // ✅ Add your mandala drawable IDs here
    int[] mandalaImages = {
            R.drawable.mandala1,
            R.drawable.mandala2,
            R.drawable.mandala3,
            R.drawable.mandala4,
            R.drawable.mandala5
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mandala);

        TextView btnBack = findViewById(R.id.btnBack);
        GridView gridMandalas = findViewById(R.id.gridMandalas);

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