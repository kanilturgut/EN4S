package com.tobbetu.en4s.backend;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

public class Image {

    public static final String SIZE_ORIG = "";
    public static final String SIZE_512 = ".512";
    public static final String SIZE_256 = ".256";
    public static final String SIZE_128 = ".128";

    private Bitmap bmp;

    public Bitmap getBmp() {
        return bmp;
    }

    public void setBmp(Bitmap bmp) {
        this.bmp = bmp;
    }

    public byte[] bitmapToByteArray(Bitmap btmp) {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        btmp.compress(Bitmap.CompressFormat.PNG, 100, out);

        return out.toByteArray();

    }

    public String sha256hash() {
        MessageDigest digest = null;
        String hash = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            digest.update(bitmapToByteArray(this.bmp));

            // Dirty hack to convert byte array into hex string
            hash = new BigInteger(1, digest.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            Log.e(getClass().getName(), "NoSuchAlgorithmException throwed", e);
        }
        return hash;
    }

    public String base64image() {
        return Base64.encodeToString(bitmapToByteArray(this.bmp),
                Base64.DEFAULT);
    }

    public static Image download(String url) throws IOException {
        Image dl = new Image();
        byte[] bitmapdata = Requests.download(url);
        dl.setBmp(BitmapFactory.decodeByteArray(bitmapdata, 0,
                bitmapdata.length));
        return dl;
    }

    public static String getImageURL(String url, String size) {
        String newUrl = String.format("%s%s.jpg",
                url.substring(0, url.lastIndexOf('.')), size);
        Log.d("Image.getSmallImage", newUrl);
        return newUrl;
    }

}
