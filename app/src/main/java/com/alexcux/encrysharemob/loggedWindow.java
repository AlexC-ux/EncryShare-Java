package com.alexcux.encrysharemob;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Locale;
import java.util.TimeZone;

public class loggedWindow extends AppCompatActivity {

    private static final String CHANNEL_ID = "Chat channel";
    private static final int NOTIFY_ID = 101;
    public static Thread chatsThread;
    public static loggedWindow lw;
    public static Intent msgServiceObj;
    public static Chat[] Chats = new Chat[0];
    boolean needKey = false;
    private Button newChatBtn;
    private ImageButton settingsBtn;
    private ImageButton refreshBtn;
    private ImageButton menuBtn;
    private LinearLayout chatsPanel;
    private RelativeLayout menuPanel;
    private EditText newChatName;
    private TextView nameTextView;
    private TextView idTextView;
    private ClipboardManager clipboardManager;
    private ClipData clipData;
    private TextView chats_preloader;

    public static int linearSearch(Chat[] array, Chat elementToSearch) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].ChatId.equals(elementToSearch.ChatId)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    protected void onPause(){
        msgService.pause = msgService.passivePause;
        super.onPause();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onResume(){
        super.onResume();
        msgService.pause = msgService.activePause;
        if (chatsThread!=null){
            if (chatsThread.getState() == Thread.State.TERMINATED)
            chatsThread.start();
        }
    }

    @Override
    protected void onDestroy(){
        msgService.noResponseCounter = msgService.noResponseToPassive-2;
        chatsThread.interrupt();
        super.onDestroy();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState){
        super.onPostCreate(savedInstanceState);
        new CheckVersion().execute(getResources().getString(R.string.versionUrl));
    }

    private void reUpdateChats(){
        Runnable chatsThUpdater = new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                chatsThread.interrupt();
                while(!chatsThread.isInterrupted()){
                    synchronized (this){
                        try {
                            wait(150);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                firstChatsUpd = false;
                new CheckVersion().execute(getResources().getString(R.string.versionUrl));
                startGettingChats();
            }
        };
        new Thread(chatsThUpdater).start();
    }

    private void startRefreshAnim(){
        RotateAnimation ra = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        ra.setDuration(1500);
        ra.setInterpolator(new LinearInterpolator());
        ra.setRepeatCount(9999);
        refreshBtn.startAnimation(ra);
    }

    private void stopRefreshAnim(){
        RotateAnimation ra = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        ra.setDuration(0);
        ra.setInterpolator(new LinearInterpolator());
        ra.setRepeatCount(0);
        refreshBtn.startAnimation(ra);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String languageToLoad  = getSharedPreferences("lang", MODE_PRIVATE).getString("value",""); // your language
        Locale myLocale = new Locale(languageToLoad);
        Locale.setDefault(myLocale);
        android.content.res.Configuration config = new android.content.res.Configuration();
        config.locale = myLocale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        super.onCreate(savedInstanceState);
        switch (getSharedPreferences("main", MODE_PRIVATE).getString("theme", "sys")){
            case "sys":
                AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            case "light":
                AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "dark":
                AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_YES);
                break;

        }
        setContentView(R.layout.activity_logged_window);
        refreshBtn = findViewById(R.id.refreshBtn);
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopService(msgServiceObj);
                startService(msgServiceObj);
                reUpdateChats();
                Toast.makeText(getApplicationContext(),
                        "Обновление..",
                        Toast.LENGTH_SHORT).show();
            }
        });
        settingsBtn = findViewById(R.id.settingsBtn);
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PopupMenu popupMenu = new PopupMenu(getApplicationContext(), view);
                popupMenu.inflate(R.menu.settings);

                popupMenu
                        .setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                switch (item.getItemId()) {
                                    case R.id.langpopup:
                                        PopupMenu LANGpopupMenu = new PopupMenu(getApplicationContext(), view);
                                        LANGpopupMenu.inflate(R.menu.langs);
                                        LANGpopupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                            @Override
                                            public boolean onMenuItemClick(MenuItem menuItem) {
                                                switch (menuItem.getItemId()){
                                                    case R.id.russian:
                                                        getSharedPreferences("lang", MODE_PRIVATE).edit().putString("value","ru").commit();
                                                        Toast.makeText(getApplicationContext(),
                                                                "Вы выбрали русский язык!",
                                                                Toast.LENGTH_SHORT).show();
                                                        updateLocale();
                                                        return true;
                                                    case R.id.english:
                                                        getSharedPreferences("lang", MODE_PRIVATE).edit().putString("value","en").commit();
                                                        Toast.makeText(getApplicationContext(),
                                                                "You have selected English!",
                                                                Toast.LENGTH_SHORT).show();
                                                        updateLocale();
                                                        return true;
                                                    default:
                                                        return false;
                                                }
                                            }
                                        });
                                        LANGpopupMenu.show();
                                        return true;
                                    case R.id.sharepopup:
                                        Intent sendIntent = new Intent();
                                        sendIntent.setAction(Intent.ACTION_SEND);
                                        sendIntent.putExtra(Intent.EXTRA_TEXT, "Я использую самый безопасный мессенджер EncryShare, скачивай с официального сайта:\nhttps://alexc-ux.github.io/EncryShareWebsite/\n\nДобавляй меня в чат, мой ID:\n"+getSharedPreferences("main", MODE_PRIVATE).getString("userid", ""));
                                        sendIntent.setType("text/plain");
                                        startActivity(Intent.createChooser(sendIntent,"Поделиться"));
                                        return true;
                                    case R.id.themepopup:
                                        PopupMenu THEMEpopupMenu = new PopupMenu(getApplicationContext(), view);
                                        THEMEpopupMenu.inflate(R.menu.themes);
                                        THEMEpopupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                            @Override
                                            public boolean onMenuItemClick(MenuItem menuItem) {
                                                switch (menuItem.getItemId()){
                                                    case R.id.lighttheme:
                                                        getSharedPreferences("main", MODE_PRIVATE).edit().putString("theme","light").commit();
                                                        recreate();
                                                        return true;
                                                    case R.id.darktheme:
                                                        getSharedPreferences("main", MODE_PRIVATE).edit().putString("theme","dark").commit();
                                                        recreate();
                                                        return true;
                                                    case R.id.systemtheme:
                                                        getSharedPreferences("main", MODE_PRIVATE).edit().putString("theme","sys").commit();
                                                        recreate();
                                                        return true;
                                                    default:
                                                        return false;
                                                }
                                            }
                                        });
                                        THEMEpopupMenu.show();
                                        return true;
                                    case R.id.authorpopup:
                                        startActivity(new Intent(getApplicationContext(),author.class));
                                        return true;
                                    default:
                                        return true;
                                }

                            }
                        });
                popupMenu.show();
            }
        });



        chats_preloader = findViewById(R.id.chats_preloader);


        String chatNameI = getIntent().getStringExtra("chat");
        if (chatNameI != null) {
            Chat.activeChat = new Chat(chatNameI, chatNameI.split("#")[1], getSharedPreferences(chatNameI.split("#")[1], MODE_PRIVATE).getString("messages", ""));
            Chat.activeChat.Open(getApplicationContext(),this);
        }

        lw = this;

        newChatName=findViewById(R.id.newChatName);

        newChatBtn = findViewById(R.id.newChat);
        menuBtn = findViewById(R.id.menuBtn);
        chatsPanel = findViewById(R.id.allChatsPanel);
        menuPanel = findViewById(R.id.leftMenuPanel);
        nameTextView = findViewById(R.id.username);
        idTextView = findViewById(R.id.userid);
        menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //если видно чаты, то прячем чаты и показываем меню
                if (chatsPanel.getVisibility() == View.VISIBLE) {
                    chatsPanel.setVisibility(View.GONE);
                    menuPanel.setVisibility(View.VISIBLE);
                } else {
                    chatsPanel.setVisibility(View.VISIBLE);
                    menuPanel.setVisibility(View.GONE);
                }
            }
        });
        idTextView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                clipData = ClipData.newPlainText("text",getSharedPreferences("main", MODE_PRIVATE).getString("userid", ""));
                clipboardManager=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
                clipboardManager.setPrimaryClip(clipData);
                Toast.makeText(getApplicationContext(),"ID скопирован!",Toast.LENGTH_SHORT).show();
            }

        });
        newChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (newChatName.getVisibility() == View.GONE){
                    newChatName.setVisibility(View.VISIBLE);
                }else if (!newChatName.getText().toString().trim().equals("")){
                    needKey = true;
                    new CreateChat().execute(loggedWindow.this.getString(R.string.apiUrl)+"createChat.php?api_key="+getSharedPreferences("main", MODE_PRIVATE).getString("api_key", "")+"&chat_name=" + Uri.encode(newChatName.getText().toString()));
                    Toast.makeText(getApplicationContext(),"Отправка запроса для создания чата...",Toast.LENGTH_SHORT).show();
                    newChatName.setVisibility(View.GONE);
                    newChatName.setText("");

                    InputMethodManager keyboard = (InputMethodManager) loggedWindow.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                    keyboard.hideSoftInputFromWindow(newChatName.getWindowToken(), 0);
                }else {
                    needKey = false;
                    newChatName.setVisibility(View.GONE);
                    newChatName.setText("");
                }
            }
        });

        if (!getSharedPreferences("main", MODE_PRIVATE).getString("username", "").trim().equals("") && !getSharedPreferences("main", MODE_PRIVATE).getString("userid", "").trim().equals("")) {
            nameTextView.setText(getSharedPreferences("main", MODE_PRIVATE).getString("username", ""));
            idTextView.setText("#" + getSharedPreferences("main", MODE_PRIVATE).getString("userid", ""));
        } else {
            String url = loggedWindow.this.getString(R.string.apiUrl) + "reg.php?act=login&api_key=" + getSharedPreferences("main", MODE_PRIVATE).getString("api_key", "");
            new GetUserData().execute(url);
        }
        startGettingChats();
        startGettingMessages();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        Locale locale = new Locale(getSharedPreferences("lang", MODE_PRIVATE).getString("value","ru"));
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getBaseContext().getResources().updateConfiguration(config, null);
    }

    @SuppressWarnings("deprecation")
    private void updateLocale() {
        String languageToLoad  = getSharedPreferences("lang", MODE_PRIVATE).getString("value",""); // your language
        Locale myLocale = new Locale(languageToLoad);
        Locale.setDefault(myLocale);
        android.content.res.Configuration config = new android.content.res.Configuration();
        config.locale = myLocale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

        Intent intent = new Intent(this, loggedWindow.class);
        startActivity(intent);
        finish();
        overridePendingTransition(0, 0);
    }


    private void startGettingMessages() {
        //TODO получение сообщени
        if (!isMyServiceRunning(msgService.class)){
            msgServiceObj = new Intent(this, msgService.class);
            msgServiceObj.putExtras(this.getIntent());
            startService(msgServiceObj);
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private static boolean firstChatsUpd = false;
    private void startGettingChats(){
        if (! firstChatsUpd){
            startRefreshAnim();
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (true) {

                    new GetUserChats().execute(loggedWindow.this.getString(R.string.apiUrl)+"getChats.php?api_key="+getSharedPreferences("main", MODE_PRIVATE).getString("api_key", ""));
                    synchronized(this) {
                        try {
                            wait(1000);
                        } catch(InterruptedException ie){}
                    }
                }

            }
        };
        chatsThread = new Thread(runnable);
        // Запускаем поток
        chatsThread.start();
    }

    public void MakeNotif(String title, String notifText){
        String nid = title.split("#")[1];
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

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_encryshare_notif)
                .setContentTitle(title)
                .setContentText(notifText)
                .setAutoCancel(true);


        Intent resultChatIntent = new Intent(getApplicationContext(), loggedWindow.class);
        Intent logw = new Intent(getApplicationContext(),loggedWindow.class);
        resultChatIntent.putExtra("chat",title);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
        stackBuilder.addParentStack(loggedWindow.class);
        stackBuilder.addNextIntent(resultChatIntent);


        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_ONE_SHOT);


        builder.setContentIntent(resultPendingIntent);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    public class GetUserData extends AsyncTask<String, String, String> {

        HttpURLConnection connection = null;
        BufferedReader reader = null;

        @Override
        protected String doInBackground(String... strings) {
            try {
                //Обработка запроса
                URL url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer stringBuffer = new StringBuffer();
                String line = "";


                while ((line = reader.readLine()) != null)
                    stringBuffer.append(line).append("\n");
                return stringBuffer.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null)
                    connection.disconnect();
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try {
                JSONObject jsonObject = new JSONObject(result);
                String name = jsonObject.getString("name");
                String id = jsonObject.getString("id");
                getSharedPreferences("main", MODE_PRIVATE).edit().putString("username", name).commit();
                getSharedPreferences("main", MODE_PRIVATE).edit().putString("userid", id).commit();
                nameTextView.setText(getSharedPreferences("main", MODE_PRIVATE).getString("username", ""));
                idTextView.setText("#" + getSharedPreferences("main", MODE_PRIVATE).getString("userid", ""));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    public static boolean isOffline = false;
    private void StartOfflineMode(){
        if (!isOffline){
            isOffline = true;
            stopService(msgServiceObj);
            chatsThread.interrupt();
            Intent offlineIntent = new Intent(getBaseContext(),offline_mode.class);
            startActivity(offlineIntent);
            finish();
        }

    }
    public static boolean offlineMode = false;
    private class GetUserChats extends AsyncTask<String, String, String> {


        HttpURLConnection connection = null;
        BufferedReader reader = null;

        @Override
        protected String doInBackground(String... strings) {
            try {
                //Обработка запроса
                URL url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();



                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer stringBuffer = new StringBuffer();
                String line = "";


                while ((line = reader.readLine()) != null)
                    stringBuffer.append(line).append("\n");
                return stringBuffer.toString();
            } catch (IOException e) {
                if (offlineMode){
                    offlineMode = false;
                }else{
                    offlineMode = true;
                    if (!isOffline){
                        StartOfflineMode();
                    }
                }
            } finally {
                if (connection != null)
                    connection.disconnect();
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    stopRefreshAnim();
                }
            }

            return null;
        }



        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try {
                if (result!=null){
                    Chat[] oldChats = Chats;
                    Chat newchat = null;

                    //result = result.split("\\[")[1].split("\\]")[0];
                    JSONObject jsonObject = new JSONObject(result);
                    JSONArray e = jsonObject.getJSONArray("Chats");
                    chatsPanel.removeAllViews();
                    Chats = new Chat[e.length()];
                    for (int i = 0; i<e.length();i++){
                        JSONObject chatObject = new JSONObject(e.getString(i));
                        Chat newChat = new Chat(chatObject.getString("chat_name"), chatObject.getString("chat_id"), getSharedPreferences(chatObject.getString("chat_id"), MODE_PRIVATE).getString("messages",""));
                        Chats[i] = newChat;
                    }
                    if (needKey==true){
                        if (oldChats.length>0){
                            for (int i=0;i<Chats.length;i++) {
                                if (linearSearch(oldChats, Chats[i]) < 0) {
                                    needKey = false;
                                    newchat = Chats[i];
                                    getSharedPreferences(newchat.ChatId, MODE_PRIVATE).edit().putString("chat_name", newchat.ChatName).commit();
                                    //todo закрепление пароля за чатом
                                    String generatedString = customEncryptorAES.getRandomKey();
                                    getSharedPreferences(newchat.ChatId, MODE_PRIVATE).edit().putString("password", generatedString).commit();
                                    break;
                                }
                            }
                        }else{
                            if (Chats.length>0){
                                needKey = false;
                                //todo закрепление пароля за чатом
                                String generatedString = customEncryptorAES.getRandomKey();
                                getSharedPreferences(Chats[0].ChatId, MODE_PRIVATE).edit().putString("password",generatedString).commit();
                            }
                        }


                    }
                    for (int i = 0; i<e.length();i++){
                        JSONObject chatObject = new JSONObject(e.getString(i));
                        Chat newChat = new Chat(chatObject.getString("chat_name"), chatObject.getString("chat_id"), getSharedPreferences(chatObject.getString("chat_id"), MODE_PRIVATE).getString("messages",""));
                        final View view = getLayoutInflater().inflate(R.layout.chat_template, null);
                        RelativeLayout newChatLayout = view.findViewById(R.id.chatTemplate);
                        TextView newChatName = view.findViewById(R.id.chatTemplate_name);
                        newChatName.setText(chatObject.getString("chat_name"));
                        TextView newChatId = view.findViewById(R.id.chatTemplate_id);
                        newChatId.setText(chatObject.getString("chat_id"));

                        newChatLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                newChat.Open(getApplicationContext(), loggedWindow.this);
                            }
                        });
                        chatsPanel.addView(view);
                    }

                    if (chats_preloader!=null){
                        chats_preloader.setVisibility(View.GONE);
                    }
                        }
                if (!firstChatsUpd) {
                    firstChatsUpd = true;
                    stopRefreshAnim();
                }

                stopRefreshAnim();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class CreateChat extends AsyncTask<String, String, String> {

        HttpURLConnection connection = null;
        BufferedReader reader = null;

        @Override
        protected String doInBackground(String... strings) {
            try {
                //Обработка запроса
                URL url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer stringBuffer = new StringBuffer();
                String line = "";


                while ((line = reader.readLine()) != null)
                    stringBuffer.append(line).append("\n");
                return stringBuffer.toString();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null)
                    connection.disconnect();
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            //new GetUserChats().execute(loggedWindow.this.getString(R.string.apiUrl)+"getChats.php?api_key="+getSharedPreferences("main", MODE_PRIVATE).getString("api_key", ""));
        }
    }

    private class CheckVersion extends AsyncTask<String, String, String> {

        HttpURLConnection connection = null;
        BufferedReader reader = null;

        @Override
        protected String doInBackground(String... strings) {
            try {
                //Обработка запроса
                URL url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer stringBuffer = new StringBuffer();
                String line = "";


                while ((line = reader.readLine()) != null)
                    stringBuffer.append(line).append("\n");
                return stringBuffer.toString();
            } catch (IOException e) {
                //e.printStackTrace();
            } finally {
                if (connection != null)
                    connection.disconnect();
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (result!=null){
                String appversion = BuildConfig.VERSION_NAME;
                if (!result.contains(appversion)){
                    String nid = "0";
                    int NOTIFICATION_ID = Integer.parseInt(nid);
                    NotificationManager notificationManager = (NotificationManager) lw.getSystemService(Context.NOTIFICATION_SERVICE);
                    String CHANNEL_ID = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        CHANNEL_ID = "update";
                        CharSequence name = "update";
                        String Description = "update";
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

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(lw, CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_encryshare_notif)
                            .setContentTitle(getResources().getString(R.string.updateTitle))
                            .setContentText(getResources().getString(R.string.updateText))
                            .setAutoCancel(true);


                    Intent resultChatIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.devSiteUrl)));
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
                    stackBuilder.addParentStack(loggedWindow.class);
                    stackBuilder.addNextIntent(resultChatIntent);

                    PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_ONE_SHOT);

                    builder.setContentIntent(resultPendingIntent);
                    notificationManager.notify(NOTIFICATION_ID, builder.build());
            }
            }
        }
    }

}

