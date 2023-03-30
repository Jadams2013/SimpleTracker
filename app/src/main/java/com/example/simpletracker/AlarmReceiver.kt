package com.example.simpletracker

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.preference.PreferenceManager


class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("!!Alarm Bell!!", "Alarm just fired!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")

        val newIntent = Intent(context, MainActivity::class.java)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, newIntent, PendingIntent.FLAG_IMMUTABLE)

        var builder = NotificationCompat.Builder(context, context.getString(R.string.channel_name))
            .setSmallIcon(R.drawable.baseline_calendar_today_24)
            .setContentTitle("Simple Tracker Reminder")
            .setContentText("Remember to record data for ${intent.getStringExtra("tagName")}")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Remember to record data for ${intent.getStringExtra("tagName")}\n${intent.getStringExtra("tagNote")}"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notifManger = NotificationManagerCompat.from(context)
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("Error","notification permission not granted for current context")
            // Consider calling ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        notifManger.notify(intent.getIntExtra("tagId",0), builder)

    }


}