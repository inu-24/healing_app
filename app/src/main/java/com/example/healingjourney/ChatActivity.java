package com.example.healingjourney;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatActivity extends BaseActivity {

    private LinearLayout chatContainer;
    private ScrollView scrollChat;
    private EditText etMessage;

    private OkHttpClient client;
    private Handler mainHandler;

    // ✅ Use BuildConfig to inject API key securely
    private static final String API_KEY = BuildConfig.GEMINI_API_KEY;

    // ✅ Working model
    private static final String API_URL =
            "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent?key=" + API_KEY;

    // ✅ NLP System Prompt - Mental health context
    private static final String SYSTEM_PROMPT =
            "You are Healing Bot, a compassionate AI " +
                    "mental health assistant in the Healing " +
                    "Journey app that uses art therapy. " +
                    "You use Natural Language Processing (NLP) " +
                    "to understand users emotions from their " +
                    "text. Your role is to: " +
                    "1. Listen and provide emotional support. " +
                    "2. Identify emotional distress from language. " +
                    "3. Encourage art therapy and mandala coloring. " +
                    "4. Give warm, empathetic responses. " +
                    "5. Keep responses to 2-3 sentences. " +
                    "6. If crisis detected, suggest professional help. " +
                    "Always respond in a caring, supportive way.";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        setupBottomNav();

        // Check if API key is set
        if (API_KEY.isEmpty() || API_KEY.equals("YOUR_API_KEY_HERE")) {
            Toast.makeText(this,
                    "Please set your Gemini API key in local.properties",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        mainHandler = new Handler(Looper.getMainLooper());

        chatContainer = findViewById(R.id.chatContainer);
        scrollChat = findViewById(R.id.scrollChat);
        etMessage = findViewById(R.id.etMessage);
        TextView btnBack = findViewById(R.id.btnBack);
        ImageView btnSend = findViewById(R.id.btnSend);

        btnBack.setOnClickListener(v -> finish());

        // ✅ Welcome message
        addAIMessage("Hi! 💚 How are you feeling today? " +
                "I'm here to listen and support you " +
                "on your healing journey. 🌿");

        btnSend.setOnClickListener(v -> {
            String message = etMessage.getText()
                    .toString().trim();
            if (!message.isEmpty()) {
                addUserMessage(message);
                etMessage.setText("");
                addTypingIndicator();
                callGeminiAPI(message);
            } else {
                Toast.makeText(this,
                        "Please type a message",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void callGeminiAPI(String userMessage) {
        try {
            // ✅ Build NLP request
            JSONObject systemPart = new JSONObject();
            systemPart.put("text", SYSTEM_PROMPT);

            JSONObject systemContent = new JSONObject();
            systemContent.put("role", "user");
            JSONArray systemParts = new JSONArray();
            systemParts.put(systemPart);
            systemContent.put("parts", systemParts);

            JSONObject modelAck = new JSONObject();
            modelAck.put("role", "model");
            JSONArray modelParts = new JSONArray();
            JSONObject modelPart = new JSONObject();
            modelPart.put("text",
                    "I understand. I'm Healing Bot, " +
                            "here to support you with " +
                            "empathy and care. 💚");
            modelParts.put(modelPart);
            modelAck.put("parts", modelParts);

            JSONObject userContent = new JSONObject();
            userContent.put("role", "user");
            JSONArray userParts = new JSONArray();
            JSONObject userPart = new JSONObject();
            userPart.put("text", userMessage);
            userParts.put(userPart);
            userContent.put("parts", userParts);

            JSONArray contents = new JSONArray();
            contents.put(systemContent);
            contents.put(modelAck);
            contents.put(userContent);

            JSONObject generationConfig = new JSONObject();
            generationConfig.put("temperature", 0.8);
            generationConfig.put("maxOutputTokens", 250);
            generationConfig.put("topP", 0.9);

            JSONObject requestBody = new JSONObject();
            requestBody.put("contents", contents);
            requestBody.put("generationConfig",
                    generationConfig);

            RequestBody body = RequestBody.create(
                    requestBody.toString(),
                    MediaType.get(
                            "application/json; charset=utf-8"));

            Request request = new Request.Builder()
                    .url(API_URL)
                    .post(body)
                    .addHeader("Content-Type",
                            "application/json")
                    .build();

            client.newCall(request).enqueue(
                    new Callback() {
                        @Override
                        public void onFailure(
                                Call call, IOException e) {
                            mainHandler.post(() -> {
                                removeTypingIndicator();
                                addAIMessage(
                                        "⚠️ Connection failed. " +
                                                "Please check your " +
                                                "internet connection.");
                            });
                        }

                        @Override
                        public void onResponse(
                                Call call,
                                Response response)
                                throws IOException {
                            String responseBody =
                                    response.body().string();
                            mainHandler.post(() -> {
                                removeTypingIndicator();
                                try {
                                    JSONObject json =
                                            new JSONObject(
                                                    responseBody);

                                    if (json.has("error")) {
                                        String errMsg = json
                                                .getJSONObject("error")
                                                .getString("message");
                                        addAIMessage(
                                                "⚠️ " + errMsg);
                                        return;
                                    }

                                    String reply = json
                                            .getJSONArray("candidates")
                                            .getJSONObject(0)
                                            .getJSONObject("content")
                                            .getJSONArray("parts")
                                            .getJSONObject(0)
                                            .getString("text");

                                    addAIMessage(reply.trim());

                                } catch (Exception e) {
                                    addAIMessage(
                                            "⚠️ Error processing " +
                                                    "response. Please " +
                                                    "try again.");
                                }
                            });
                        }
                    });

        } catch (Exception e) {
            removeTypingIndicator();
            addAIMessage("⚠️ Error: " + e.getMessage());
        }
    }

    private void addUserMessage(String message) {
        TextView tv = new TextView(this);
        tv.setText(message);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(14);
        tv.setPadding(32, 24, 32, 24);
        tv.setBackgroundResource(R.drawable.bubble_user);

        LinearLayout wrapper = new LinearLayout(this);
        wrapper.setGravity(Gravity.END);
        wrapper.setLayoutParams(new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT));
        wrapper.setPadding(0, 0, 0, 16);
        wrapper.addView(tv);

        chatContainer.addView(wrapper);
        scrollToBottom();
    }

    private void addAIMessage(String message) {
        LinearLayout msgLayout = new LinearLayout(this);
        msgLayout.setOrientation(
                LinearLayout.HORIZONTAL);
        msgLayout.setLayoutParams(new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT));
        msgLayout.setGravity(Gravity.START);
        msgLayout.setPadding(0, 0, 0, 16);

        ImageView avatar = new ImageView(this);
        avatar.setImageResource(
                R.mipmap.ic_launcher_round);
        LayoutParams avatarParams =
                new LayoutParams(40, 40);
        avatarParams.setMargins(0, 8, 8, 0);
        avatar.setLayoutParams(avatarParams);
        msgLayout.addView(avatar);

        TextView tv = new TextView(this);
        tv.setText(message);
        tv.setTextColor(Color.parseColor("#1B5E20"));
        tv.setTextSize(14);
        tv.setPadding(32, 24, 32, 24);
        tv.setBackgroundResource(R.drawable.bubble_ai);
        tv.setMaxWidth(700);

        LinearLayout textWrapper =
                new LinearLayout(this);
        textWrapper.setLayoutParams(new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT));
        textWrapper.addView(tv);
        msgLayout.addView(textWrapper);

        chatContainer.addView(msgLayout);
        scrollToBottom();
    }

    @SuppressLint("SetTextI18n")
    private void addTypingIndicator() {
        LinearLayout wrapper = new LinearLayout(this);
        wrapper.setOrientation(
                LinearLayout.HORIZONTAL);
        wrapper.setGravity(Gravity.START);
        wrapper.setLayoutParams(new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT));
        wrapper.setTag("typing_wrapper");
        wrapper.setPadding(0, 0, 0, 16);

        ImageView avatar = new ImageView(this);
        avatar.setImageResource(
                R.mipmap.ic_launcher_round);
        LayoutParams avatarParams =
                new LayoutParams(40, 40);
        avatarParams.setMargins(0, 8, 8, 0);
        avatar.setLayoutParams(avatarParams);
        wrapper.addView(avatar);

        TextView tv = new TextView(this);
        tv.setText("Healing Bot is typing... 💚");
        tv.setTextColor(Color.parseColor("#4CAF50"));
        tv.setTextSize(12);
        tv.setPadding(32, 16, 32, 16);
        tv.setBackgroundResource(R.drawable.bubble_ai);
        wrapper.addView(tv);

        chatContainer.addView(wrapper);
        scrollToBottom();
    }

    private void removeTypingIndicator() {
        for (int i = chatContainer.getChildCount() - 1;
             i >= 0; i--) {
            Object tag = chatContainer
                    .getChildAt(i).getTag();
            if (tag != null &&
                    tag.toString().equals("typing_wrapper")) {
                chatContainer.removeViewAt(i);
                break;
            }
        }
    }

    private void scrollToBottom() {
        scrollChat.post(() ->
                scrollChat.fullScroll(
                        ScrollView.FOCUS_DOWN));
    }
}