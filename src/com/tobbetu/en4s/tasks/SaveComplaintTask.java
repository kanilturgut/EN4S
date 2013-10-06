package com.tobbetu.en4s.tasks;

import java.io.IOException;

import org.json.JSONException;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.tobbetu.en4s.R;
import com.tobbetu.en4s.backend.Complaint;
import com.tobbetu.en4s.backend.Image;
import com.tobbetu.en4s.helpers.BetterAsyncTask;
import com.tobbetu.en4s.helpers.ComplaintRejectedException;

public class SaveComplaintTask extends BetterAsyncTask<Void, Complaint> {

    private final String TAG = "SaveComplaintTask";
    private Context context = null;
    private Complaint complaint = null;
    private Image image = null;

    public SaveComplaintTask(Context c, Complaint cmp, Image img) {
        this.context = c;
        this.complaint = cmp;
        this.image = img;
    }

    @Override
    protected Complaint task(Void... arg0) throws Exception {
        Complaint savedComplaint = complaint.save(image);
        return savedComplaint;
    }

    @Override
    protected void onSuccess(Complaint result) {

        Toast.makeText(context, R.string.nc_accepted, Toast.LENGTH_SHORT)
                .show();

        // TODO implement notification (mustafa)
    }

    @Override
    protected void onFailure(Exception error) {
        Log.e(TAG, "SaveTask Failed", error);
        if (error instanceof IOException) {
            // TODO save complaint to send in future (mustafa)
            Toast.makeText(context, R.string.network_failed_msg,
                    Toast.LENGTH_LONG).show();
        } else if (error instanceof JSONException) {
            Toast.makeText(context, R.string.api_changed, Toast.LENGTH_LONG)
                    .show();
        } else if (error instanceof ComplaintRejectedException) {
            Toast.makeText(context, R.string.nc_compalint_rejected,
                    Toast.LENGTH_LONG).show();
        }
    }

}
