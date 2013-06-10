package com.tobbetu.en4s.backend;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.graphics.Bitmap;
import android.util.Log;

public class Image {

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

}
