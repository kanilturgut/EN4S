package com.tobbetu.en4s.cache;

import java.io.IOException;
import java.util.HashMap;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.tobbetu.en4s.backend.Image;

public class Cache {

    private static Cache instance = null;
    private HashMap<String, Image> cache;
    private final static String TAG = Cache.class.getCanonicalName(); 
    
    private Cache() {
        cache = new HashMap<String, Image>();
    }
    
    public static Cache getInstance() {
        if (instance == null) {
            instance = new Cache();
        }
        return instance;
    }
    
    public void getImage(String url, ImageView iv) {
        Log.d(TAG, "Cache URL: " + url);
        if (cache.containsKey(url)) {
            Log.d(TAG, "Cache HIT: " + url);
            Image img = cache.get(url);
            iv.setImageBitmap(img.getBmp());
        } else {
            Log.d(TAG, "Cache MISS: " + url);
            DownloadTask dt = new DownloadTask(url, iv);
            dt.execute();
        }
        
    }
    
    private class DownloadTask extends AsyncTask<String, Image, Image> {
        
        private String url;
        private ImageView iv;
        
        public DownloadTask(String url, ImageView iv) {
            this.iv = iv;
            this.url = url;
        }

        @Override
        protected Image doInBackground(String... arg0) {
            try {
                return Image.download(this.url);
            } catch (IOException e) {
                return null;
            }
        }
        
        @Override
        protected void onPostExecute(Image result) {
            super.onPostExecute(result);
            if(!isCancelled()) {
                Log.d(TAG, "Cache SET: " + url);
                if(iv != null && result != null) {
                    cache.put(this.url, result);
                    iv.setImageBitmap(result.getBmp());    
                } else {
                    Log.d(TAG, "RESULT: " + result);
                }
                    
            }
        }
        
    }
}
