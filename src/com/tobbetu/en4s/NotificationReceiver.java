package com.tobbetu.en4s;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class NotificationReceiver extends BroadcastReceiver {

    public static final int NOTIFICATION_ID = 1;
    private static final String TAG = "NotificationReceiver";

    private GoogleCloudMessaging gcm = null;
    private NotificationManager mNotificationManager = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (gcm == null) {
            gcm = GoogleCloudMessaging.getInstance(context);
        }

        Bundle extras = intent.getExtras();
        String messageType = gcm.getMessageType(intent);

        if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)
                && !extras.isEmpty()) {
            Log.e(TAG, extras.getString("msg"));
            sendNotification(context, extras);
        }
    }

    private void sendNotification(Context context, Bundle extras) {
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);
        }

        String type = extras.getString("type");
        type = type == null ? "INVALID" : type;

        if (type.equals("duy")) {
            /**
             * TODO action tanimlanacak
             * 
             * Intent solved = new Intent(context, DetailsActivity.class);
             * solved.putExtras(extras); PendingIntent action =
             * PendingIntent.getActivity(context, 0, solved,
             * PendingIntent.FLAG_UPDATE_CURRENT);
             */

            // TODO belediye iconlari yerlestirilecek
            Bitmap icon = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.ic_launcher);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                    context)
                    .setSmallIcon(R.drawable.logo)
                    .setLargeIcon(icon)
                    .setContentTitle(extras.getString("title"))
                    .setContentText(extras.getString("msg"))
                    .setAutoCancel(true)
                    .setStyle(
                            new NotificationCompat.BigTextStyle()
                                    .bigText(extras.getString("msg")));

            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        } else {
            // Invalid notification
        }

    }
}
