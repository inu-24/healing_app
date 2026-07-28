package com.example.healingjourney;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class ProfileImageHelper {

    // Each user gets their own saved photo file, named by their user ID
    private static File getImageFile(Context context) {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "guest";
        File dir = new File(context.getFilesDir(), "profile_images");
        if (!dir.exists()) dir.mkdirs();
        return new File(dir, userId + ".jpg");
    }

    // Call this right after the user picks a photo
    public static void saveProfileImage(Context context, Uri imageUri) {
        try {
            InputStream input = context.getContentResolver().openInputStream(imageUri);
            Bitmap original = BitmapFactory.decodeStream(input);
            if (original == null) return;

            // Resize so the saved file stays small
            Bitmap resized = Bitmap.createScaledBitmap(original, 300, 300, true);

            File file = getImageFile(context);
            FileOutputStream out = new FileOutputStream(file);
            resized.compress(Bitmap.CompressFormat.JPEG, 85, out);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Call this anywhere you want to display the saved photo (falls back to the
    // default app icon if the user hasn't picked one yet)
    public static void loadProfileImage(Context context, ImageView imageView) {
        File file = getImageFile(context);
        if (file.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
                return;
            }
        }
        imageView.setImageResource(R.mipmap.ic_launcher_round);
    }
}