package com.example.encrysharemob;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.channels.Channel;

public class Chat extends AppCompatActivity {

    public static Chat activeChat = new Chat("","","");
    public String ChatName;
    public String ChatId;
    public JSONArray ChatMessages;
    public String[] Members;


    public Chat(String chatName, String chatId, String chatData){
        ChatName = chatName;
        ChatId = chatId;
        try {
            JSONArray jsonObject = new JSONArray(chatData);
            ChatMessages = jsonObject;
        } catch (JSONException e) {
            //e.printStackTrace();
        }
    }

    public void Open(Context context, AppCompatActivity ap){
        activeChat = this;
        Intent intent = new Intent(context.getApplicationContext(), chatWindow.class);
        ap.startActivity(intent);
    }

    public void UpdateMessages(AppCompatActivity ap, String senderName, String senderId, String messageText){
        try {
            JSONObject message = new JSONObject()
                    .put("senderName",senderName)
                    .put("senderId",senderId)
                    .put("message", messageText);
            if (ChatMessages!=null){
                ChatMessages = ChatMessages.put(ChatMessages.length()+1,message.toString());
            }else{
                ChatMessages = new JSONArray().put(0,message);
            }
            ap.getSharedPreferences(ChatId, MODE_PRIVATE).edit().remove("messages");
            ap.getSharedPreferences(ChatId, MODE_PRIVATE).edit().putString("messages",ChatMessages.toString()).commit();
            if (ap.getClass() == chatWindow.class){
                chatWindow cw = (chatWindow) ap;
                cw.AddNewMessage(senderName+"#"+senderId+":\n"+messageText);
            }else{
                loggedWindow lw = (loggedWindow)ap;
                lw.MakeNotif(ChatName+"#"+ChatId,senderName+"#"+senderId+": "+messageText);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
