package com.alexcux.encrysharemob;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    public  static SharedPreferences mainPrefs;

    private EditText name_field;
    private EditText login_field;
    private EditText password_field;
    private Button regBtn;
    private Button langBtn;

    private void updateLocale(){
        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
        finish();
        startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        new customEncryptorRSA.MakeReq().execute(this.getString(R.string.apiUrl)+"reg.php");
        String languageToLoad  = getSharedPreferences("lang", MODE_PRIVATE).getString("value","ru"); // your language
        Locale locale = new Locale(languageToLoad);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
        mainPrefs = getSharedPreferences("main", MODE_PRIVATE);
        if (getSharedPreferences("main", MODE_PRIVATE).getString("api_key","") != ""){
            //Если ключ уже установлен, то переходим дальше...
            Intent intent = new Intent(getApplicationContext(), loggedWindow.class);
            startActivity(intent);
            finish();

        }
        else {

            File sharedPreferenceFile = new File("/data/data/"+ getPackageName()+ "/shared_prefs/");
            File[] listFiles = sharedPreferenceFile.listFiles();
            for (File file : listFiles) {
                file.delete();
            }
            setContentView(R.layout.activity_main);
            name_field = findViewById(R.id.name_field);
            login_field = findViewById(R.id.login_field);
            password_field = findViewById(R.id.password_field);
            regBtn = findViewById(R.id.regBtn);

            regBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (name_field.getText().toString().length() > 4 && !name_field.getText().toString().trim().equals("") && login_field.getText().toString().length() > 4) {
                        String name = name_field.getText().toString();
                        String login = login_field.getText().toString();
                        String password = password_field.getText().toString();

                        String url = MainActivity.this.getString(R.string.apiUrl) + "reg.php?act=reg&name=" + name + "&login=" + login + "&pswd_hash=4Ie4bzlCyAwj5LZ1GkdPVnHCifDChBI8os8ps2TIv2dveAHl3Er0Yj0NSPco8P5sk62kTTbuQxkvhRbmqZAZZ4hWid97YyG5TrLMCUWlFyYyDp2i3IqLVD40ToUBUFtthRv6xKCN13G3WdURMwi2OzN2QnQPeqG5iSlqYY4TPfcZ";

                        new GetUrlText().execute(url);

                    } else {
                        Toast.makeText(MainActivity.this, R.string.wrongRegFieldsRU, Toast.LENGTH_LONG).show();
                    }
                }

            });
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

        }
    }
    private class GetUrlText extends AsyncTask<String, String, String> {

        protected void onPreExecute(){
            super.onPreExecute();
            regBtn.setText(R.string.loadingRU);
        }


        @Override
        protected String doInBackground(String... strings){
            HttpURLConnection connection = null;
            BufferedReader reader = null;


            try {
                //Обработка запроса
                URL url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer stringBuffer = new StringBuffer();
                String line = "";


                while((line = reader.readLine())!=null)
                    stringBuffer.append(line).append("\n");

                return stringBuffer.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if (connection!=null)
                    connection.disconnect();

                try {
                if (reader!=null) {

                    reader.close();
                }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


            }

            return null;
        }

        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);

            try {
                JSONObject jsonObject = new JSONObject(result);
                String apiKey = jsonObject.getString("api_key");
                getSharedPreferences("main", MODE_PRIVATE).edit().putString("api_key",apiKey).commit();
                Intent intent = new Intent(getApplicationContext(), loggedWindow.class);
                startActivity(intent);
                finish();
                //Получение API KEY!!
                //getSharedPreferences("main", MODE_PRIVATE).getString("api_key","")
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }
}


