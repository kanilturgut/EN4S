package com.tobbetu.en4s.tasks;

import java.io.IOException;

import org.json.JSONException;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.tobbetu.en4s.R;
import com.tobbetu.en4s.backend.Image;
import com.tobbetu.en4s.complaint.Complaint;
import com.tobbetu.en4s.complaint.ComplaintRejectedException;
import com.tobbetu.en4s.helpers.BetterAsyncTask;

public class SaveComplaintTask extends BetterAsyncTask<Void, Complaint> {

    private final String TAG = "SaveComplaintTask";
    private Context context = null;
    private Complaint complaint = null;
    private Image image = null;
    private NotificationManager mNotifyMgr;
    private int notificationID;
    private Bitmap enforceIcon;

    public SaveComplaintTask(Context c, Complaint cmp, Image img) {
        this.context = c;
        this.complaint = cmp;
        this.image = img;
        this.notificationID = cmp.getTitle().hashCode();
        this.mNotifyMgr = (NotificationManager) context
                .getSystemService(Activity.NOTIFICATION_SERVICE);
        this.enforceIcon = BitmapFactory.decodeResource(c.getResources(),
                R.drawable.ic_launcher);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                context)
                .setSmallIcon(R.drawable.logo)
                .setLargeIcon(image.getBmp())
                // TODO duzgun enforce iconu
                .setContentTitle(complaint.getTitle())
                .setTicker(
                        context.getResources().getString(
                                R.string.nc_cat_sending))
                .setOngoing(true)
                .setContentText(
                        context.getResources().getString(
                                R.string.nc_cat_sending))
                .setProgress(0, 0, true);

        mNotifyMgr.notify(notificationID, mBuilder.build());
    }

    @Override
    protected Complaint task(Void... arg0) throws Exception {
        Complaint savedComplaint = complaint.save(image);
        return savedComplaint;
    }

    @Override
    protected void onSuccess(Complaint result) {

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                context)
                .setSmallIcon(R.drawable.logo)
                .setLargeIcon(enforceIcon)
                .setContentTitle(result.getTitle())
                .setContentText(
                        context.getResources().getString(R.string.nc_accepted))
                .setAutoCancel(true)
                .setStyle(
                        new NotificationCompat.BigPictureStyle()
                                .setSummaryText(
                                        context.getResources().getString(
                                                R.string.nc_accepted))
                                .bigPicture(image.getBmp()));

        mNotifyMgr.notify(notificationID, mBuilder.build());
    }

    @Override
    protected void onFailure(Exception error) {
        Log.e(TAG, "SaveTask Failed", error);
        mNotifyMgr.cancel(notificationID);
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
