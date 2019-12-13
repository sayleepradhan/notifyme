package com.example.android.notifyme

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat


class MainActivity : AppCompatActivity() {

    private val buttonNotify: View  by lazyFindView(R.id.notify)

    private val buttonUpdate: View by lazyFindView(R.id.update)

    private val buttonCancel: View by lazyFindView(R.id.cancel)

    private val mNotificationManager: NotificationManager by lazy {
        (getSystemService(Context.NOTIFICATION_SERVICE)) as NotificationManager
    }

    private val mNotificationReceiver: NotificationReceiver by lazy {
        NotificationReceiver()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setNotificationButtonState(
            isNotifyEnabled =  true,
            isUpdateEnabled = false,
            isCancelEnabled = false
        )

        buttonNotify.setOnClickListener {
            sendNotification()
        }

        buttonUpdate.setOnClickListener {
            updateNotification()
        }

        buttonCancel.setOnClickListener {
            cancelNotification()
        }

        createNotificationChannel()
        registerReceiver(mNotificationReceiver, IntentFilter(ACTION_UPDATE_NOTIFICATION))
        registerReceiver(mNotificationReceiver, IntentFilter(ACTION_DISMISS_NOTIFICATION))
    }

    override fun onDestroy() {
        unregisterReceiver(mNotificationReceiver)
        super.onDestroy()
    }

    private fun updateNotification() {

        setNotificationButtonState(
            isNotifyEnabled =  false,
            isUpdateEnabled = false,
            isCancelEnabled = true
        )

        mNotificationManager.notify(
            NOTIFICATION_ID,
            getNotificationBuilder()
                .setStyle(
                    NotificationCompat.BigPictureStyle()
                        .bigPicture(
                            BitmapFactory.decodeResource(resources, R.drawable.mascot_1)
                        )
                        .setBigContentTitle("Notification Updated!")
                )
                .build()
            )
    }

    private fun cancelNotification() {
        setNotificationButtonState(
            isNotifyEnabled =  true,
            isUpdateEnabled = false,
            isCancelEnabled = false
        )
        mNotificationManager.cancel(NOTIFICATION_ID)
    }

    private fun sendNotification() {
        setNotificationButtonState(
            isNotifyEnabled =  false,
            isUpdateEnabled = true,
            isCancelEnabled = true
        )

        getNotificationBuilder()
            .addAction(
                R.drawable.ic_update,
                "Update Notification",
                PendingIntent.getBroadcast(
                this,
                    NOTIFICATION_ID,
                    Intent(ACTION_UPDATE_NOTIFICATION),
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
        mNotificationManager.notify(
            NOTIFICATION_ID,
            getNotificationBuilder().build()
        )
    }

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.O) {
            // Create a NotificationChannel
            NotificationChannel(
                PRIMARY_CHANNEL_ID,
                "Mascot Notification",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                this.enableLights(true)
                this.lightColor = Color.RED
                this.enableVibration(true)
                this.description = "Notification from Mascot"
                mNotificationManager.createNotificationChannel(this)
            }
        }
    }

    private fun getNotificationBuilder(): NotificationCompat.Builder {
        return NotificationCompat.Builder(this, PRIMARY_CHANNEL_ID)
            .setContentTitle("You've been notified")
            .setContentText("This is your notification text")
            .setSmallIcon(R.drawable.ic_android)
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    NOTIFICATION_ID,
                    Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT
                    )
            )
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setDeleteIntent(
                PendingIntent.getBroadcast(
                    this,
                    NOTIFICATION_ID,
                    Intent(ACTION_DISMISS_NOTIFICATION),
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            .apply {
                priority = NotificationCompat.PRIORITY_HIGH
            }
    }

    private fun setNotificationButtonState(
        isNotifyEnabled: Boolean,
        isUpdateEnabled: Boolean,
        isCancelEnabled: Boolean
    ) {
        buttonNotify.isEnabled = isNotifyEnabled
        buttonUpdate.isEnabled = isUpdateEnabled
        buttonCancel.isEnabled = isCancelEnabled
    }

    private fun <T : View> lazyFindView(@IdRes resId: Int): Lazy<T> {
        return lazy(LazyThreadSafetyMode.NONE) { findViewById<T>(resId) }
    }

    companion object {
        private const val NOTIFICATION_ID = 0
        private const val PRIMARY_CHANNEL_ID = "primary_notification_channel"
        private const val ACTION_UPDATE_NOTIFICATION = "com.example.android.notifyme.ACTION_UPDATE_NOTIFICATION"
        private const val ACTION_DISMISS_NOTIFICATION = "com.example.android.notifyme.ACTION_DISMISS_NOTIFICATION"
    }

    inner class NotificationReceiver: BroadcastReceiver() {
        init {

        }

        override fun onReceive(p0: Context?, p1: Intent?) {
            p1?.let {intent ->
                when(intent.action) {
                    ACTION_DISMISS_NOTIFICATION -> {
                        this@MainActivity.cancelNotification()
                    }
                    ACTION_UPDATE_NOTIFICATION -> {
                        this@MainActivity.updateNotification()
                    }
                }
            }
        }
    }
}
