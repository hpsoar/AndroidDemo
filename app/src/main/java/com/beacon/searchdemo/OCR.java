package com.beacon.searchdemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by huangpeng on 10/28/16.
 */
public class OCR {
    public static String TAG = "OCR";
    public static String DATA_PATH = "";

    public static String regcognize(String path, String lang) {
        Bitmap bitmap = loadImage(path);
        return recognize(bitmap, lang);
    }

    public static String recognize(Bitmap image, String lang) {
        if (image == null) {
            return "";
        }

        Log.v(TAG, "Before baseApi");

        TessBaseAPI baseApi = new TessBaseAPI();
        baseApi.setDebug(true);
        baseApi.init(DATA_PATH, lang);
        baseApi.setImage(image);

        String recognizedText = baseApi.getUTF8Text();

        baseApi.end();

        // You now have the text in recognizedText var, you can do anything with it.
        // We will display a stripped out trimmed alpha-numeric version of it (if lang is eng)
        // so that garbage doesn't make it to the display.

        Log.v(TAG, "OCRED TEXT: " + recognizedText);

        if ( lang.equalsIgnoreCase("eng") ) {
            recognizedText = recognizedText.replaceAll("[^a-zA-Z0-9]+", " ");
        }

        recognizedText = recognizedText.trim();

        return recognizedText;
    }

    private static Bitmap loadImage(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        options.inJustDecodeBounds = false;

        File file = new File(path);
        if (!file.exists()) {
            Log.v(TAG, "file doesn't exists: " + path);
        }

        Bitmap bitmap = BitmapFactory.decodeFile(path, options);

        if (bitmap == null) {
            Log.v(TAG, "failed to load image: " + path);
            return bitmap;
        }

        try {
            ExifInterface exif = new ExifInterface(path);
            int exifOrientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            Log.v(TAG, "Orient: " + exifOrientation);

            int rotate = 0;

            switch (exifOrientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
            }

            Log.v(TAG, "Rotation: " + rotate);

            if (rotate != 0) {

                // Getting width & height of the given image.
                int w = bitmap.getWidth();
                int h = bitmap.getHeight();

                // Setting pre rotate
                Matrix mtx = new Matrix();
                mtx.preRotate(rotate);

                // Rotating Bitmap
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, false);
            }

            // Convert to ARGB_8888, required by tess
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        } catch (IOException e) {
            Log.e(TAG, "Couldn't correct orientation: " + e.toString());
        }

        return bitmap;
    }
}
