package com.example.sms;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Telephony;

import androidx.core.app.NotificationCompat;

public class SmsReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "SMS_ALERT_CHANNEL";
    private static final int NOTIFICATION_ID = 101;

    @Override
    public void onReceive(Context context, Intent intent) {

        if (!Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
            return;
        }

        android.telephony.SmsMessage[] messages =
                Telephony.Sms.Intents.getMessagesFromIntent(intent);

        if (messages == null || messages.length == 0) {
            return;
        }

        String sender = messages[0].getDisplayOriginatingAddress();
        StringBuilder messageBody = new StringBuilder();

        for (android.telephony.SmsMessage message : messages) {
            messageBody.append(message.getMessageBody());
        }

        showNotification(context, sender, messageBody.toString());
    }

    private void showNotification(
            Context context,
            String sender,
            String message
    ) {

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(
                        Context.NOTIFICATION_SERVICE
                );

        // Create notification channel for Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "SMS Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );

            channel.setDescription("Alerts for incoming SMS messages");

            notificationManager.createNotificationChannel(channel);
        }

        Intent openIntent = new Intent(context, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT |
                        PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle("New SMS from " + sender)
                        .setContentText(message)
                        .setStyle(
                                new NotificationCompat.BigTextStyle()
                                        .bigText(message)
                        )
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent);

        notificationManager.notify(
                NOTIFICATION_ID,
                builder.build()
        );
    }
}
