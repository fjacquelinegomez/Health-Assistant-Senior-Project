package com.example.healthassistant;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

// Helper class that deals with creating the notification channel, sending out notifications + notification permissions
// https://developer.android.com/develop/ui/views/notifications/build-notification <- Notification documentation and tutorial (really helpful implementing this)
public class NotificationHelper {
    private static final String CHANNEL_ID = "medication_reminder_channel"; // Channel identifier
    private Context context;

    // Notification constructor that passes in the current context and creates the notification channel
    public NotificationHelper(Context context) {
        this.context = context;
        createNotificationChannel();
    }

    // Creates a notification channel (notification group for Health Assistant's notifications)
    // This is required in Android 8.0
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Names the channel and how important the notifications are
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Medication Reminder", NotificationManager.IMPORTANCE_DEFAULT);

            // Accesses the system's notification services and registers Health Assistant's Medication Reminder channel to it
            NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    // Checks if the user has notification permissions on, will request for notification permissions if not
    // This is required for Android 13+ (API 33+)
    // https://www.youtube.com/watcgh?v=96IBhBs-k1M -> Used this to help with notifiation consent
    public boolean checkNotificationPermission(Activity activity, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // If permission hasn't been granted yet, it request for it here as a pop up
            if (activity.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                activity.requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, requestCode);
                return false; // Permission not granted
            }
        }
        return true; // Permission granted
    }

    // Builds the notification (how it'll be displayed + the content)
    public void showNotification(String title, String message, int notificationId) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, Homescreen.class), PendingIntent.FLAG_IMMUTABLE);

        // Builds the notification with the given information passed in
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.medicalsupport)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(pendingIntent) // what will happen when the user taps on the notif
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build();

        // Actually shows the notification to the user
        if (notificationManager != null) {
            notificationManager.notify(notificationId, notification);
        }
    }
}
