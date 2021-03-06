package com.alexcux.encrysharemob;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;
import java.util.Locale;

public class offline_mode extends AppCompatActivity {

    public static offline_mode activeOfflineActivity;
    private LinearLayout chatsPanel;
    private RelativeLayout menuPanel;
    private TextView nameTextView;
    private TextView idTextView;
    private ImageButton menuBtn;
    private ImageButton refreshBtn;


    @Override
    protected void onDestroy() {
        loggedWindow.isOffline = false;
        super.onDestroy();
    }

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        activeOfflineActivity = this;
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
        setContentView(R.layout.activity_offline_mode);
        updateQrCode(findViewById(R.id.qrcode));
        chatsPanel = findViewById(R.id.allChatsPanel);
        menuPanel = findViewById(R.id.leftMenuPanel);
        menuBtn = findViewById(R.id.menuBtn);
        menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //???????? ?????????? ????????, ???? ???????????? ???????? ?? ???????????????????? ????????
                if (chatsPanel.getVisibility() == View.VISIBLE) {
                    chatsPanel.setVisibility(View.GONE);
                    menuPanel.setVisibility(View.VISIBLE);
                } else {
                    chatsPanel.setVisibility(View.VISIBLE);
                    menuPanel.setVisibility(View.GONE);
                }
            }
        });

        findViewById(R.id.settingsBtn).setOnClickListener(new View.OnClickListener() {
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
                                                                "???? ?????????????? ?????????????? ????????!",
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
                                        sendIntent.putExtra(Intent.EXTRA_TEXT, "?? ?????????????????? ?????????? ???????????????????? ???????????????????? EncryShare, ???????????????? ?? ???????????????????????? ??????????:\nhttps://alexc-ux.github.io/EncryShareWebsite/\n\n???????????????? ???????? ?? ??????, ?????? ID:\n"+getSharedPreferences("main", MODE_PRIVATE).getString("userid", ""));
                                        sendIntent.setType("text/plain");
                                        startActivity(Intent.createChooser(sendIntent,"????????????????????"));
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

        nameTextView = findViewById(R.id.username);
        idTextView = findViewById(R.id.userid);
        idTextView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                ClipData clipData = ClipData.newPlainText("text", getSharedPreferences("main", MODE_PRIVATE).getString("userid", ""));
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                clipboardManager.setPrimaryClip(clipData);
                Toast.makeText(getApplicationContext(),"ID ????????????????????!",Toast.LENGTH_SHORT).show();
            }

        });

        if (!getSharedPreferences("main", MODE_PRIVATE).getString("username", "").trim().equals("") && !getSharedPreferences("main", MODE_PRIVATE).getString("userid", "").trim().equals("")) {
            nameTextView.setText(getSharedPreferences("main", MODE_PRIVATE).getString("username", ""));
            idTextView.setText("#" + getSharedPreferences("main", MODE_PRIVATE).getString("userid", ""));
        } else {
            menuBtn.setOnClickListener(null);
        }

        refreshBtn = findViewById(R.id.refreshBtn);
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setContentView(R.layout.activity_logged_window);
                Intent onlineMode = new Intent(getApplicationContext(), loggedWindow.class);
                finish();
                startActivity(onlineMode);
            }
        });


        AddChatsTOList();
    }

    public void AddChatsTOList(){
        Runnable adding = new Runnable() {
            @Override
            public void run() {
                File sharedPreferenceFile = new File("/data/data/"+ getPackageName()+ "/shared_prefs/");
                File[] listFiles = sharedPreferenceFile.listFiles();
                chatsPanel.removeAllViews();
                for (File file : listFiles) {
                    try {
                        String chat_id = file.getName().split("\\.")[0];
                        JSONObject chatObject = new JSONObject();
                        chatObject.put("chat_id",chat_id);
                        chatObject.put("chat_name",getSharedPreferences(chat_id, MODE_PRIVATE).getString("chat_name","??hat name unavailable"));
                        Chat newChat = new Chat(chatObject.getString("chat_name"), chatObject.getString("chat_id"), getSharedPreferences(chatObject.getString("chat_id"), MODE_PRIVATE).getString("messages",""));
                        if (Integer.parseInt(chat_id)>0){
                            final View view = getLayoutInflater().inflate(R.layout.chat_template, null);
                            RelativeLayout newChatLayout = view.findViewById(R.id.chatTemplate);
                            TextView newChatName = view.findViewById(R.id.chatTemplate_name);
                            newChatName.setText(chatObject.getString("chat_name"));
                            TextView newChatId = view.findViewById(R.id.chatTemplate_id);
                            newChatId.setText(chatObject.getString("chat_id"));

                            newChatLayout.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    newChat.Open(getApplicationContext(), loggedWindow.lw);
                                }
                            });
                            chatsPanel.addView(view);
                        }
                    }catch (Exception e){

                    }
                }
            }
        };

        runOnUiThread(adding);
    }

    private void updateQrCode(ImageView qr){
        Runnable qrUpdater = new Runnable() {
            @Override
            public void run() {
                String image64 = getSharedPreferences("main", MODE_PRIVATE).getString("bitmap","");
                while (image64.equals("")){
                    synchronized (this){
                        try {
                            wait(1200);
                            image64 = getSharedPreferences("main", MODE_PRIVATE).getString("qrcode","");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                byte[] imageAsBytes = Base64.decode(image64.getBytes(), Base64.DEFAULT);
                if (imageAsBytes.length>0){
                    qr.setVisibility(View.VISIBLE);
                    qr.setImageBitmap(BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length));
                }else{
                    qr.setVisibility(View.GONE);
                }
            }
        };
        runOnUiThread(qrUpdater);
    }
}