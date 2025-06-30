package com.example.bdapps.utils;

import android.graphics.Bitmap;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

public class ImageUtil {
    public static String bitmapToBase64(Bitmap bitmap, Bitmap.CompressFormat format, int quality) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(format, quality, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();

        String encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);

        String prefix;
        if (format == Bitmap.CompressFormat.PNG) {
            prefix = "data:image/png;base64,";
        } else {
            prefix = "data:image/jpeg;base64,";
        }

        return prefix + encodedImage;
    }
}
