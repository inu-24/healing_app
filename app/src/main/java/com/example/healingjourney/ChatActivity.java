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

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChatActivity extends BaseActivity {

    private LinearLayout chatContainer;
    private ScrollView scrollChat;
    private EditText etMessage;

    private OkHttpClient client;
    private Handler mainHandler;

    // Read API key from BuildConfig
    private static final String API_KEY = BuildConfig.GEMINI_API_KEY;

    private static final String API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key="
                    + API_KEY;

    private static final String SYSTEM_PROMPT =
            "You are a compassionate mental health support assistant " +
                    "called Healing Bot in the Healing Journey app. " +
                    "Your role is to provide emotional support, " +
                    "encourage art therapy, and help users express " +
                    "their feelings. Always be warm, empathetic and " +
                    "supportive. Keep responses concise (2-3 sentences). " +
                    "If someone seems in crisis, encourage professional help.";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        setupBottomNav();

        // Initialize OkHttpClient
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        mainHandler = new Handler(Looper.getMainLooper());

        // Initialize views
        chatContainer = findViewById(R.id.chatContainer);
        scrollChat = findViewById(R.id.scrollChat);
        etMessage = findViewById(R.id.etMessage);

        TextView btnBack = findViewById(R.id.btnBack);
        ImageView btnSend = findViewById(R.id.btnSend);

        btnBack.setOnClickListener(v -> finish());

        // Add welcome message
        addAIMessage("Hi! 💚 How are you feeling today? I'm here to listen and support you.");

        btnSend.setOnClickListener(v -> {
            String message = etMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                addUserMessage(message);
                etMessage.setText("");
                addTypingIndicator();
                callGeminiAPI(message);
            } else {
                Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void callGeminiAPI(String userMessage) {
        // Check if API key is empty
        if (API_KEY.isEmpty() || API_KEY.equals("")) {
            removeTypingIndicator();
            addAIMessage("⚠️ API key is not configured. Please add your Gemini API key to local.properties file.");
            return;
        }

        try {
            // Build the request body
            JSONObject textPart = new JSONObject();
            textPart.put("text", SYSTEM_PROMPT + "\n\nUser: " + userMessage);

            JSONArray parts = new JSONArray();
            parts.put(textPart);

            JSONObject content = new JSONObject();
            content.put("parts", parts);

            JSONArray contents = new JSONArray();
            contents.put(content);

            JSONObject requestBody = new JSONObject();
            requestBody.put("contents", contents);

            // Create request body
            RequestBody body = RequestBody.create(
                    requestBody.toString(),
                    MediaType.get("application/json; charset=utf-8")
            );

            // Build request
            Request request = new Request.Builder()
                    .url(API_URL)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build();

            // Execute request asynchronously
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    mainHandler.post(() -> {
                        removeTypingIndicator();
                        addAIMessage("⚠️ Connection failed. Please check your internet connection.");
                        e.printStackTrace();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();

                    mainHandler.post(() -> {
                        removeTypingIndicator();

                        try {
                            JSONObject json = new JSONObject(responseBody);

                            // Check for error
                            if (json.has("error")) {
                                JSONObject errorObj = json.getJSONObject("error");
                                String errorMessage = errorObj.getString("message");

                                // Check for specific API key error
                                if (errorMessage.contains("API key") || errorMessage.contains("key")) {
                                    addAIMessage("⚠️ Invalid API key. Please check your Gemini API key in local.properties.");
                                } else {
                                    addAIMessage("⚠️ Error: " + errorMessage);
                                }
                                return;
                            }

                            // Parse the response
                            JSONArray candidates = json.getJSONArray("candidates");
                            if (candidates.length() > 0) {
                                JSONObject candidate = candidates.getJSONObject(0);
                                JSONObject contentObj = candidate.getJSONObject("content");
                                JSONArray partsArray = contentObj.getJSONArray("parts");
                                JSONObject part = partsArray.getJSONObject(0);
                                String reply = part.getString("text");

                                addAIMessage(reply);
                            } else {
                                addAIMessage("⚠️ No response received. Please try again.");
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            addAIMessage("⚠️ Unable to process server response. Please try again.");
                        }
                    });
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
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
                LayoutParams.WRAP_CONTENT
        ));
        wrapper.addView(tv);

        chatContainer.addView(wrapper);
        scrollToBottom();
    }

    private void addAIMessage(String message) {
        TextView tv = new TextView(this);
        tv.setText(message);
        tv.setTextColor(Color.parseColor("#1B5E20"));
        tv.setTextSize(14);
        tv.setPadding(32, 24, 32, 24);
        tv.setBackgroundResource(R.drawable.bubble_ai);

        LinearLayout wrapper = new LinearLayout(this);
        wrapper.setGravity(Gravity.START);
        wrapper.setLayoutParams(new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        ));
        wrapper.addView(tv);

        chatContainer.addView(wrapper);
        scrollToBottom();
    }

    @SuppressLint("SetTextI18n")
    private void addTypingIndicator() {
        TextView tv = new TextView(this);
        tv.setText("Healing Bot is typing... 💚");
        tv.setTextColor(Color.parseColor("#4CAF50"));
        tv.setTextSize(12);
        tv.setPadding(32, 16, 32, 16);
        tv.setTag("typing_indicator");

        LinearLayout wrapper = new LinearLayout(this);
        wrapper.setGravity(Gravity.START);
        wrapper.setLayoutParams(new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        ));
        wrapper.addView(tv);
        wrapper.setTag("typing_wrapper");

        chatContainer.addView(wrapper);
        scrollToBottom();
    }

    private void removeTypingIndicator() {
        for (int i = chatContainer.getChildCount() - 1; i >= 0; i--) {
            Object tag = chatContainer.getChildAt(i).getTag();
            if (tag != null && tag.toString().equals("typing_wrapper")) {
                chatContainer.removeViewAt(i);
                break;
            }
        }
    }

    private void scrollToBottom() {
        scrollChat.post(() ->
                scrollChat.fullScroll(ScrollView.FOCUS_DOWN)
        );
    }
}