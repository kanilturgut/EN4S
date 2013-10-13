package com.tobbetu.en4s.cache;

import java.io.IOException;
import java.util.NoSuchElementException;

import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;

import com.tobbetu.en4s.R;
import com.tobbetu.en4s.backend.Image;
import com.tobbetu.en4s.helpers.BetterAsyncTask;

public class Cache {

    private static Cache instance = null;
    private static final int cacheSize = 4 * 1024 * 1024; // 4MiB
    private final LruCache<String, Image> cache;
    private final static String TAG = "Cache";

    private Cache() {
        cache = new LruCache<String, Image>(cacheSize);
    }

    public static Cache getInstance() {
        if (instance == null) {
            instance = new Cache();
        }
        return instance;
    }

    public void getImage(String url, ImageView iv) {
        Log.d(TAG, "Cache URL: " + url);
        Image img;
        synchronized (cache) {
            img = cache.get(url);
        }
        if (img != null) {
            Log.d(TAG, "Cache HIT: " + url);
            iv.setImageBitmap(img.getBmp());
        } else {
            Log.d(TAG, "Cache MISS: " + url);
            DownloadTask dt = new DownloadTask(url, iv);
            dt.execute();
        }

    }

    private class DownloadTask extends BetterAsyncTask<Void, Image> {

        private final String url;
        private final ImageView iv;

        public DownloadTask(String url, ImageView iv) {
            this.iv = iv;
            this.url = url;
        }

        @Override
        protected Image task(Void... arg0) throws Exception {
            return Image.download(this.url);
        }

        @Override
        protected void onSuccess(Image result) {
            Log.d(TAG, "Cache SET: " + url);
            if (iv != null && result != null) {
                synchronized (cache) {
                    cache.put(this.url, result);
                }
                iv.setImageBitmap(result.getBmp());
            } else {
                Log.d(TAG, "iv: " + iv + ", result: " + result);
            }
        }

        @Override
        protected void onFailure(Exception error) {
            if (error instanceof IOException
                    || error instanceof NoSuchElementException) {
                iv.setImageResource(R.drawable.failed);
            }
        }

    }
}
