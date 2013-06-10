package com.tobbetu.en4s.backend;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.string;
import android.graphics.Bitmap;
import android.util.Base64;
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

    private String base64image() {
        return Base64.encodeToString(bitmapToByteArray(this.bmp),
                Base64.DEFAULT);
    }

    private String toJSON() {
        JSONObject image = new JSONObject();
        try {
            image.put("hash", sha256hash());
            image.put("pic", base64image());
        } catch (JSONException e) {
            Log.e("Complaint.toJSON", "Unexpected JSONException", e);
        }
        return image.toString();
    }

    public void upload(String oid) throws IOException {
        Log.d("[JSON]", this.toJSON());
        HttpResponse post = Requests.post("http://en4s.msimav.net/upload/" + oid,
                this.toJSON());
        if (!Requests.checkStatusCode(post, HttpStatus.SC_CREATED)) {
            // TODO throw exception
            Log.d(getClass().getName(), "Status Code in not 201");
        }
    }

}