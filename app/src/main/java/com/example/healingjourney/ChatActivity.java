package com.example.healingjourney;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.LinearLayout.LayoutParams;

public class ChatActivity extends AppCompatActivity {

    LinearLayout chatContainer;
    ScrollView scrollChat;
    EditText etMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatContainer = findViewById(R.id.chatContainer);
        scrollChat = findViewById(R.id.scrollChat);
        etMessage = findViewById(R.id.etMessage);

        TextView btnBack = findViewById(R.id.btnBack);
        ImageView btnSend = findViewById(R.id.btnSend);

        btnBack.setOnClickListener(v -> finish());

        btnSend.setOnClickListener(v -> {
            String message = etMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                addUserMessage(message);
                etMessage.setText("");
                // Auto AI reply
                scrollChat.postDelayed(this::addAIMessage,
                        1000
                );
            }
        });
    }

    private void addUserMessage(String message) {
        TextView tv = new TextView(this);
        tv.setText(message);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(13);
        tv.setPadding(32, 24, 32, 24);
        tv.setBackgroundResource(R.drawable.bubble_user);

        LinearLayout wrapper = new LinearLayout(this);
        wrapper.setGravity(Gravity.END);
        LayoutParams params = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 24);
        wrapper.setLayoutParams(params);
        wrapper.addView(tv);

        chatContainer.addView(wrapper);
        scrollToBottom();
    }

    @SuppressLint("SetTextI18n")
    private void addAIMessage() {
        TextView tv = new TextView(this);
        tv.setText("Thank you for sharing. How does that make you feel? 💚");
        tv.setTextColor(Color.parseColor("#1B5E20"));
        tv.setTextSize(13);
        tv.setPadding(32, 24, 32, 24);
        tv.setBackgroundResource(R.drawable.bubble_ai);

        LayoutParams params = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 24);
        tv.setLayoutParams(params);

        chatContainer.addView(tv);
        scrollToBottom();
    }

    private void scrollToBottom() {
        scrollChat.post(() ->
                scrollChat.fullScroll(ScrollView.FOCUS_DOWN));
    }
}