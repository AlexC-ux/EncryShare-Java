package com.example.encrysharemob;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class msgService extends IntentService {
    public static int pause;
    public static int activePause = 500;
    public static int passivePause = 3500;
    public static int noResponseToPassive = 30;
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
                .setTicker("EncryShare");

        startForeground(112,b.build());




                while (true) {

                    new chatWindow.GetMessages(loggedWindow.lw).execute(getString(R.string.apiUrl) + "rData.php?api_key=" + getSharedPreferences("main", MODE_PRIVATE).getString("api_key", ""));
                    if (noResponseCounter==noResponseToPassive){
                        pause = passivePause;
                    }
                    if (noResponseCounter<noResponseToPassive){
                        pause = activePause;
                    }
                    synchronized(this) {
                        try {
                            wait(pause);
                        } catch(InterruptedException ie){}
                    }
                }

    }

    public void onDestroy() {
        super.onDestroy();
        //Log.d(LOG_TAG, "onDestroy");
    }
}