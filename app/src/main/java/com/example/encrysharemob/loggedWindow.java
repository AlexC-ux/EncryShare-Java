package com.example.encrysharemob;

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
import android.view.MenuItem;
import android.view.View;
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
import java.util.Locale;
import java.util.TimeZone;

public class loggedWindow extends AppCompatActivity {

    private static final String CHANNEL_ID = "Chat channel";
    private static final int NOTIFY_ID = 101;
    public static Thread chatsThread;
    public static loggedWindow lw;
    public static Chat[] Chats = new Chat[0];
    boolean needKey = false;
    private Button shareBtn;
    private Button newChatBtn;
    private ImageButton menuBtn;
    private LinearLayout chatsPanel;
    private RelativeLayout menuPanel;
    private EditText newChatName;
    private TextView nameTextView;
    private TextView idTextView;
    private ClipboardManager clipboardManager;
    private ClipData clipData;
    private Button langBtn;
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
        chatsThread.interrupt();
        super.onDestroy();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState){
        super.onPostCreate(savedInstanceState);
        new CheckVersion().execute(getResources().getString(R.string.versionUrl));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String languageToLoad  = getSharedPreferences("lang", MODE_PRIVATE).getString("value","ru"); // your language
        Locale locale = new Locale(languageToLoad);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
        setContentView(R.layout.activity_logged_window);


        chats_preloader = findViewById(R.id.chats_preloader);
        langBtn = findViewById(R.id.langBtn);
        langBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(getApplicationContext(), view);
                popupMenu.inflate(R.menu.langs);

                popupMenu
                        .setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                switch (item.getItemId()) {
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
                popupMenu.show();
            }
        });

        shareBtn = findViewById(R.id.share);
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                String vls = "Я использую EncryShare - самый безопасный мессенджер, пользуйся и ты!\nhttps://alexc-ux.github.io/EncryShareWebsite\n\n\nДобавляй меня в чат:\n"+getSharedPreferences("main", MODE_PRIVATE).getString("userid", "");
                sendIntent.putExtra(Intent.EXTRA_TEXT, vls);
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent,"Поделиться"));
            }
        });
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

    private void updateLocale() {
        Intent intent = new Intent(getApplicationContext(),loggedWindow.class);
        finish();
        startActivity(intent);
    }


    private void startGettingMessages() {
        //TODO получение сообщени
        if (!isMyServiceRunning(msgService.class)){
            Intent msgService = new Intent(this, msgService.class);
            msgService.putExtras(this.getIntent());
            startService(msgService);
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

    private void startGettingChats(){
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

    private class GetUserData extends AsyncTask<String, String, String> {

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
                //todo проверка на изменение чатов
                JSONObject jsonObject = new JSONObject(result);
                String name = jsonObject.getString("name");
                String id = jsonObject.getString("id");
                getSharedPreferences("main", MODE_PRIVATE).edit().putString("username", name).commit();
                getSharedPreferences("main", MODE_PRIVATE).edit().putString("userid", id).commit();
                nameTextView.setText(getSharedPreferences("main", MODE_PRIVATE).getString("username", ""));
                idTextView.setText("#" + getSharedPreferences("main", MODE_PRIVATE).getString("userid", ""));
            } catch (JSONException e) {
                //e.printStackTrace();
            }
        }
    }

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
                e.printStackTrace();
            } finally {
                if (connection != null)
                    connection.disconnect();
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    //e.printStackTrace();
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
                    //result = result.split("\\[")[1].split("\\]")[0];
                    JSONObject jsonObject = new JSONObject(result);
                    JSONArray e = jsonObject.getJSONArray("Chats");
                    chatsPanel.removeAllViews();
                    Chats = new Chat[e.length()];
                    for (int i = 0; i<e.length();i++){
                        JSONObject chatObject = new JSONObject(e.getString(i));
                        Chat newChat = new Chat(chatObject.getString("chat_name"), chatObject.getString("chat_id"), getSharedPreferences(chatObject.getString("chat_id"), MODE_PRIVATE).getString("messages",""));
                        Chats[i] = newChat;
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
                    Chat newchat = null;

                    if (needKey){
                    for (Chat c :
                            Chats) {
                        if (linearSearch(oldChats, c) < 0 && oldChats.length>0) {
                            newchat = c;
                            //todo закрепление пароля за чатом

                            String generatedString = customEncryptorAES.getRandomKey();
                            getSharedPreferences(newchat.ChatId, MODE_PRIVATE).edit().putString("password",generatedString).commit();
                            needKey = false;
                        }
                    }
                        }else{
                        String d = "";
                    }


                    }
            } catch (JSONException e) {
                //e.printStackTrace();
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

            new GetUserChats().execute(loggedWindow.this.getString(R.string.apiUrl)+"getChats.php?api_key="+getSharedPreferences("main", MODE_PRIVATE).getString("api_key", ""));
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

            if (!result.equals(null)){
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

