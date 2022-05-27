package com.alexcux.encrysharemob;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class author extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_author);


        findViewById(R.id.exitBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        AddClickTriger(R.id.authorgithub,"https://github.com/AlexC-ux");
        AddClickTriger(R.id.siomavk,"https://vk.com/id292655047");
        AddClickTriger(R.id.vlads23,"https://github.com/VladS23");
        AddClickTriger(R.id.rimuru,"https://t.me/IRIMURUI");
        AddClickTriger(R.id.sycho,"https://vk.com/id486671968");

    }

    private void AddClickTriger(int id,String link){
        findViewById(id).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openPage= new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                startActivity(openPage);
            }
        });

    }
}