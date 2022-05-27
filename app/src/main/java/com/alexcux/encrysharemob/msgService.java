package com.alexcux.encrysharemob;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.renderscript.RenderScript;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class msgService extends IntentService {
    public static int pause = 500;
    public static int activePause = 500;
    public static int passivePause = 6000;
    public static int noResponseToPassive = 100;
    public static int noResponseCounter = 0;
    final String LOG_TAG = "myLogs";
    public msgService() {
        super("msgService");
    }

    public void onCreate() {
        super.onCreate();

    }

    @SuppressLint("WrongThread")
    @Override
    protected void onHandleIntent(Intent intent) {
        String title = "EncrySHare";
        String notifText = "";
        Log.d(LOG_TAG, "onCreate");
        String nid = "212";
        int NOTIFICATION_ID = Integer.parseInt(nid);
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        String CHANNEL_ID = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CHANNEL_ID = title;
            CharSequence name = "encryshare";
            String Description = "encryshare messages";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mChannel.setDescription(Description);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mChannel.setShowBadge(false);
            notificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder b = new NotificationCompat.Builder(this, CHANNEL_ID);

        b.setOngoing(true)
                .setContentTitle("Фоновый сервис")
                .setContentText("обработки входящих сообщений")
                .setSmallIcon(R.drawable.ic_encryshare_notif)
                .setPriority(2)
                .setTicker("EncryShare");

        this.startForeground(112,b.build());
        /*msgService msgService = this;
        msgService.startForeground(112,b.build());
        stopSelf();*/


                while (true) {

                    new chatWindow.GetMessages(loggedWindow.lw).execute(getString(R.string.apiUrl) + "rData.php?api_key=" + getSharedPreferences("main", MODE_PRIVATE).getString("api_key", ""));
                    synchronized(this) {
                        try {
                            wait(pause);
                            if (noResponseCounter==noResponseToPassive){
                                pause = passivePause;
                            }
                            if (noResponseCounter<noResponseToPassive){
                                pause = activePause;
                            }
                        } catch(InterruptedException ie){}
                    }
                }

    }

    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        //Log.d(LOG_TAG, "onDestroy");
    }
}