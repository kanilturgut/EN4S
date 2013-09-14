package com.tobbetu.en4s.helpers;

import android.os.AsyncTask;
import android.util.Log;

public abstract class BetterAsyncTask<Params, Result> extends
        AsyncTask<Params, Void, Boolean> {

    private Result value = null;
    private Exception error = null;

    protected abstract Result task(Params... arg0) throws Exception;

    protected abstract void onSuccess(Result result);

    protected abstract void onFailure(Exception error);

    protected void onStop() {

    }

    @Override
    protected Boolean doInBackground(Params... arg0) {
        try {
            value = task(arg0);
            return true;
        } catch (Exception e) {
            error = e;
            cancel(true);
            return false;
        }
    }

    @Override
    protected final void onPostExecute(Boolean result) {
        onSuccess(value);
    }

    @Override
    protected final void onCancelled(Boolean result) {
        if (error != null) {
            Log.d("BetterAsyncTask", String.format("%s failed due to %s",
                    getClass().getSimpleName(), error.getClass()
                            .getSimpleName()));
            onFailure(error);
        } else {
            Log.d("BetterAsyncTask", String.format("%s stopped by developer",
                    getClass().getSimpleName()));
            onStop();
        }
    }
}
