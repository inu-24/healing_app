package com.example.healingjourney;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatActivity extends BaseActivity {

    private static final String API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent";
    private static final String API_KEY = BuildConfig.GEMINI_API_KEY;

    private static final String SYSTEM_PROMPT =
            "You are Healing Bot, a warm, supportive companion inside a wellness app. " +
                    "You listen carefully, validate feelings, and suggest helpful coping methods " +
                    "such as art therapy and breathing exercises. " +
                    "You are not a therapist and cannot diagnose conditions. " +
                    "If someone is in crisis, encourage them to contact emergency support. " +
                    "Keep replies concise and conversational.";

    private LinearLayout chatContainer;
    private ScrollView scrollChat;
    private EditText etMessage;
    private ImageView btnSend;
    private TextView btnBack;
    private TextView btnShareChat;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    // Keeps the running conversation so the model has context
    private final List<JSONObject> conversationHistory = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        setupBottomNav();

        chatContainer = findViewById(R.id.chatContainer);
        scrollChat = findViewById(R.id.scrollChat);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnBack = findViewById(R.id.btnBack);
        btnShareChat = findViewById(R.id.btnShareChat);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnBack.setOnClickListener(v -> finish());

        btnShareChat.setOnClickListener(v -> shareRecentChat());

        loadChatHistory();

        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();
            if (TextUtils.isEmpty(text)) return;

            addMessageBubble(text, true);
            saveMessageToFirestore("user", text);
            etMessage.setText("");
            sendToGemini(text);

        });

        // Optional buttons already in your layout
        View btnSaveCanvas = findViewById(R.id.btnSaveCanvas);
        View btnTryExercise = findViewById(R.id.btnTryExercise);
        if (btnSaveCanvas != null) {
            btnSaveCanvas.setOnClickListener(v ->
                    Toast.makeText(this, "Saved to canvas", Toast.LENGTH_SHORT).show());
        }
        if (btnTryExercise != null) {
            btnTryExercise.setOnClickListener(v ->
                    addMessageBubble("Let's try a breathing exercise: breathe in for 4 seconds, " +
                            "hold for 4, and out for 6. Repeat that five times.", false));
        }
    }

    /** Sends the user's message + full history to Gemini and appends the reply when it arrives. */
    private void sendToGemini(String userText) {

        try {

            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");
            userMsg.put("content", userText);

            conversationHistory.add(userMsg);

            JSONObject body = new JSONObject();
            JSONArray contents = new JSONArray();

            // Add system prompt
            JSONObject systemContent = new JSONObject();
            JSONArray systemParts = new JSONArray();

            JSONObject systemText = new JSONObject();
            systemText.put("text", SYSTEM_PROMPT);

            systemParts.put(systemText);
            systemContent.put("parts", systemParts);

            contents.put(systemContent);

            // Add conversation history
            for (JSONObject message : conversationHistory) {

                JSONObject content = new JSONObject();
                JSONArray parts = new JSONArray();
                JSONObject textPart = new JSONObject();

                textPart.put("text", message.getString("content"));

                parts.put(textPart);
                content.put("parts", parts);

                contents.put(content);
            }

            body.put("contents", contents);

            RequestBody requestBody = RequestBody.create(
                    body.toString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(API_URL + "?key=" + API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .post(requestBody)
                    .build();

            TextView typingBubble = addMessageBubble("Typing...", false);

            httpClient.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() ->
                            typingBubble.setText("Sorry, I couldn't connect. Please try again."));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String rawBody = response.body() != null ? response.body().string() : "";

                    runOnUiThread(() ->
                            handleGeminiResponse(rawBody, response.isSuccessful(), typingBubble));
                }

            });

        } catch (Exception e) {
            addMessageBubble("Something went wrong: " + e.getMessage(), false);
        }

    }

    private void handleGeminiResponse(String rawBody, boolean success, TextView typingBubble) {

        try {

            if (!success) {
                typingBubble.setText(
                        "Sorry, I'm having trouble connecting right now. Please try again in a moment.");
                android.util.Log.e("ChatActivity", "Gemini API Error: " + rawBody);
                return;
            }

            JSONObject json = new JSONObject(rawBody);
            JSONArray candidates = json.getJSONArray("candidates");
            JSONObject candidate = candidates.getJSONObject(0);
            JSONObject content = candidate.getJSONObject("content");
            JSONArray parts = content.getJSONArray("parts");
            String reply = parts.getJSONObject(0).getString("text");

            typingBubble.setText(reply);

            // Save AI response
            JSONObject assistantMsg = new JSONObject();
            assistantMsg.put("role", "assistant");
            assistantMsg.put("content", reply);

            conversationHistory.add(assistantMsg);
            saveMessageToFirestore("assistant", reply);

        } catch (Exception e) {
            typingBubble.setText("Couldn't read Gemini response:\n" + e.getMessage());
        }

    }

    private TextView addMessageBubble(String text, boolean isUser) {
        LinearLayout row = new LinearLayout(this);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rowParams.bottomMargin = dp(12);
        row.setLayoutParams(rowParams);
        row.setOrientation(LinearLayout.HORIZONTAL);

        TextView bubble = new TextView(this);
        LinearLayout.LayoutParams bubbleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        bubble.setLayoutParams(bubbleParams);
        bubble.setMaxWidth(dp(260));
        bubble.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        bubble.setPadding(dp(12), dp(12), dp(12), dp(12));
        bubble.setText(text);

        if (isUser) {
            row.setGravity(Gravity.END);
            bubble.setTextColor(getResources().getColor(android.R.color.white));
            bubble.setBackgroundResource(R.drawable.bubble_user);
            row.addView(bubble);
        } else {
            row.setGravity(Gravity.START);
            bubble.setTextColor(0xFF1B5E20);
            bubble.setBackgroundResource(R.drawable.bubble_ai);

            ImageView icon = new ImageView(this);
            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(32), dp(32));
            iconParams.setMarginEnd(dp(8));
            iconParams.topMargin = dp(4);
            icon.setLayoutParams(iconParams);
            icon.setImageResource(R.drawable.ic_bot_avatar);

            row.addView(icon);
            row.addView(bubble);
        }

        chatContainer.addView(row);
        scrollChat.post(() -> scrollChat.fullScroll(View.FOCUS_DOWN));
        return bubble;
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }

    /** Saves one message to this user's chat history in Firestore. */
    private void saveMessageToFirestore(String role, String content) {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();

        Map<String, Object> data = new HashMap<>();
        data.put("role", role);
        data.put("content", content);
        data.put("timestamp", com.google.firebase.Timestamp.now());

        db.collection("users").document(userId)
                .collection("chatMessages")
                .add(data);
    }

    /** Loads this user's past messages (oldest first) and rebuilds the chat screen. */
    private void loadChatHistory() {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("users").document(userId)
                .collection("chatMessages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String role = doc.getString("role");
                        String content = doc.getString("content");
                        if (content == null) continue;

                        boolean isUser = "user".equals(role);
                        addMessageBubble(content, isUser);

                        try {
                            JSONObject msg = new JSONObject();
                            msg.put("role", role);
                            msg.put("content", content);
                            conversationHistory.add(msg);
                        } catch (JSONException ignored) { }
                    }
                });
    }

    /** Shares the last 5 messages of this conversation through any app the user picks. */
    private void shareRecentChat() {
        if (conversationHistory.isEmpty()) {
            Toast.makeText(this, "No messages to share yet", Toast.LENGTH_SHORT).show();
            return;
        }

        int start = Math.max(0, conversationHistory.size() - 5);
        StringBuilder shareText = new StringBuilder("My Healing Journey chat:\n\n");

        for (int i = start; i < conversationHistory.size(); i++) {
            try {
                JSONObject msg = conversationHistory.get(i);
                String who = "user".equals(msg.getString("role")) ? "Me" : "Healing Bot";
                shareText.append(who).append(": ").append(msg.getString("content")).append("\n\n");
            } catch (JSONException ignored) { }
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText.toString());
        startActivity(Intent.createChooser(shareIntent, "Share your chat"));
    }
}