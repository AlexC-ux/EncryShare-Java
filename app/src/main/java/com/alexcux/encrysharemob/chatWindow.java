package com.alexcux.encrysharemob;

import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;

public class chatWindow extends AppCompatActivity {

    public static final String floppy = "h@eKcOAn16DQ6JNw%1Waaij~LCrZJ|FyTj9FAqztnst|huOS7jNfU8p*WlDrILn~eJwQQ02Fj?PTnC6okPEC~34j@0{xX#VR6Miq~jIRdPxWE6?IH*ZYJoCKHMO8@S{IG~kB6F0R22nJ}9~ZbT7wj7nz~EPyoQ$Zvd0}wSSLzogbmJofVYv5%k}FvTt~lB#jDquvI5uNM$nirG{Cx1WojxJO*rMO4A*tWVV%6tctiD8#vNoJd@Yd0de69loQ%EG915#RKy%~SyL}o61VVWz8#IJHVwgXSryOXA5?6jwg@t";
    public static chatWindow lastOpenedChat;
    public static chatWindow activeChatWindow;
    public int exitAccep = 0;
    public ScrollView scrollChatMessages;
    private boolean scrolledToBottom = true;
    private TextView chatName;
    private LinearLayout chatMessages;
    private TextView newMsgTemplate;
    private ImageButton sendMsgBtn;
    private ImageButton exitBtn;
    private EditText messageText;
    private LinearLayout chatMembersLayout;
    private ImageButton menubtn;
    private Button addMemberBtn;
    private Button removeMemberBtn;
    private Button exitChatBtn;
    private LinearLayout chatMenuLayout;
    private ClipboardManager clipboardManager;
    private ClipData clipData;
    private EditText addMemberEditText;
    private EditText removeMemberEditText;
    private Thread membersTh;

    @Override
    protected void onDestroy(){
        String chatNameI = getIntent().getStringExtra("chat");
        if (chatNameI==null){
            Chat.activeChat = new Chat("","","");
        }else {
            Intent lw = new Intent(this, loggedWindow.class);
            startActivity(lw);
            setContentView(R.layout.activity_logged_window);
            finish();
        }
        membersTh.interrupt();
        super.onDestroy();


    }

    @Override
    protected void onPause(){
        Chat.activeChat = new Chat("", "", "");
        super.onPause();
    }
    @Override
    protected void onResume(){
        super.onResume();
        String cn = chatName.getText().toString();
        String cname = cn.split("#")[0];
        String cid = cn.split("#")[1];
        Chat.activeChat = new Chat(cname,cid,getSharedPreferences(cid, MODE_PRIVATE).getString("messages",""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_window);
        activeChatWindow = this;

        String chatNameI = getIntent().getStringExtra("chat");
        if (chatNameI != null) {
            String chatIdI = chatNameI.split("#")[1];
            chatNameI = chatNameI.split("#")[0];

            Chat.activeChat = new Chat(chatNameI, chatIdI, getSharedPreferences(chatIdI, MODE_PRIVATE).getString("messages", ""));
        }


        scrollChatMessages = findViewById(R.id.scrollChatMessages);
        scrollChatMessages.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                if (scrollChatMessages != null) {
                    if (scrollChatMessages.getChildAt(0).getBottom() <= (scrollChatMessages.getHeight() + scrollChatMessages.getScrollY())) {
                        //scroll view is at bottom
                        if (!scrolledToBottom) {
                            scrolledToBottom = true;
                        }

                    } else {
                        String e = "e";
                        //scroll view is not at bottom
                        if (scrolledToBottom) {
                            scrolledToBottom = false;
                        }
                    }
                }
            }
        });
        chatMembersLayout = findViewById(R.id.chatMembersLayout);
        messageText = findViewById(R.id.newMsgText);
        sendMsgBtn = findViewById(R.id.sendMsgBtn);
        exitBtn = findViewById(R.id.exitBtn);
        exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        newMsgTemplate = findViewById(R.id.msgTemplate);
        chatName = findViewById(R.id.chat_name);
        chatName.setText(Chat.activeChat.ChatName+"#"+Chat.activeChat.ChatId);
        if (chatName.getText().toString().contains("#")){
            if (chatName.getText().toString().split("#").length>2){
                chatName.setText(chatName.getText().toString().split("#")[0]+"#"+chatName.getText().toString().split("#")[1]);
            }
        }
        chatMessages = findViewById(R.id.chatMessages);
        chatMessages.removeAllViews();

        //?????????????????? ???????????? ??????????????????????????
        startGettingMembers();
        AddAllMessages();
        chatMenuLayout = findViewById(R.id.chatMenuLayout);

        menubtn = findViewById(R.id.inchatMenuBtn);
        menubtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RotateAnimation ra = new RotateAnimation(0, 55, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                ra.setDuration(100);
                ra.setInterpolator(new LinearInterpolator());
                ra.setRepeatCount(0);
                menubtn.startAnimation(ra);
                if (chatMenuLayout.getVisibility() == View.GONE) {
                    chatMenuLayout.setVisibility(View.VISIBLE);
                    exitAccep = 0;
                } else {
                    chatMenuLayout.setVisibility(View.GONE);
                }
            }
        });



        addMemberBtn = findViewById(R.id.addMemberBtn);
        addMemberBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {

                if (addMemberEditText.getVisibility() == View.GONE) {
                    addMemberEditText.setVisibility(View.VISIBLE);
                } else if (!addMemberEditText.getText().toString().trim().equals("")) {
                    String mid = addMemberEditText.getText().toString().trim();
                    mid = Uri.encode(mid);
                    new MakeReq().execute(chatWindow.this.getString(R.string.apiUrl) + "addMember.php?api_key=" + getSharedPreferences("main", MODE_PRIVATE).getString("api_key", "") + "&chat_id=" + Chat.activeChat.ChatId + "&member_id=" + mid);
                    String password = getSharedPreferences(Chat.activeChat.ChatId, MODE_PRIVATE).getString("password","");
                    if (password.equals("")){
                        password = customEncryptorAES.getRandomKey();
                        getSharedPreferences(Chat.activeChat.ChatId, MODE_PRIVATE).edit().putString("password",password).commit();
                    }
                    JSONObject passwordString = new JSONObject();
                    customEncryptorRSA rsa = new customEncryptorRSA();
                    try {
                        passwordString.put("chat_id",Chat.activeChat.ChatId);
                        passwordString.put("password",Uri.encode(password));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    String pswdString = passwordString.toString();
                    new MakeReq().execute(chatWindow.this.getString(R.string.apiUrl) + "sData.php?api_key=" + getSharedPreferences("main", MODE_PRIVATE).getString("api_key", "") + "&type=pswd&recepient=" + mid + "&data=" +Uri.encode(rsa.encrypt(pswdString)));
                    addMemberEditText.setText("");
                    addMemberEditText.setVisibility(View.GONE);
                } else {
                    addMemberEditText.setText("");
                    addMemberEditText.setVisibility(View.GONE);
                }
            }
        });

        removeMemberBtn = findViewById(R.id.removeMemberBtn);
        removeMemberEditText = findViewById(R.id.removeMemberEditText);
        removeMemberBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (removeMemberEditText.getVisibility() == View.GONE) {
                    removeMemberEditText.setVisibility(View.VISIBLE);
                } else if (!removeMemberEditText.getText().toString().trim().equals("")) {
                    String mid = removeMemberEditText.getText().toString().trim();
                    mid = Uri.encode(mid);
                    new MakeReq().execute(chatWindow.this.getString(R.string.apiUrl) + "addMember.php?api_key=" + getSharedPreferences("main", MODE_PRIVATE).getString("api_key", "") + "&remove&chat_id=" + Chat.activeChat.ChatId + "&member_id=" + mid);
                    removeMemberEditText.setText("");
                    removeMemberEditText.setVisibility(View.GONE);
                } else {
                    removeMemberEditText.setText("");
                    removeMemberEditText.setVisibility(View.GONE);
                }
            }
        });

        exitChatBtn = findViewById(R.id.exitChatBtn);
        exitChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                exitAccep++;
                switch (exitAccep) {
                    case 1:
                        Toast.makeText(getApplicationContext(), "?????????????? ???? ???????????? ???????????? ???? ???????? ?????? 2 ????????, ?????????? ??????????.", Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        Toast.makeText(getApplicationContext(), "?????????????? ???? ???????????? ???????????? ???? ???????? ?????? ??????, ?????????? ??????????.", Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        File cdata = new File("/data/data/"+ getPackageName()+ "/shared_prefs/"+Chat.activeChat.ChatId+".xml");
                        cdata.delete();
                        if (loggedWindow.offlineMode){
                            offline_mode.activeOfflineActivity.AddChatsTOList();
                        }
                        new MakeReq().execute(chatWindow.this.getString(R.string.apiUrl) + "addMember.php?api_key=" + getSharedPreferences("main", MODE_PRIVATE).getString("api_key", "") + "&remove&chat_id=" + Chat.activeChat.ChatId + "&member_id=" + getSharedPreferences("main", MODE_PRIVATE).getString("userid", ""));
                        exitAccep = 0;
                        chatWindow.this.finish();
                        break;
                }
            }
        });


        addMemberEditText = findViewById(R.id.addMemberEditText);

        if (chatNameI != null) {
            if (lastOpenedChat != null) {
                lastOpenedChat.finish();
            }

        }
        lastOpenedChat = this;


        new GetChatOwner().execute(chatWindow.this.getString(R.string.apiUrl) + "getChatInfo.php?api_key=" + getSharedPreferences("main", MODE_PRIVATE).getString("api_key", "") + "&chat_id=" + Chat.activeChat.ChatId);
        sendMsgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!messageText.getText().toString().trim().equals("")){
                    try {
                        //?????????????????? ????????????????
                        new GetMembers(chatWindow.this).execute(chatWindow.this.getString(R.string.apiUrl) + "getChatInfo.php?api_key=" + getSharedPreferences("main", MODE_PRIVATE).getString("api_key", "") + "&chat_id=" + Chat.activeChat.ChatId);
                        msgService.pause = 0;
                        //???????????????? ???????????? ??????????????????
                        Calendar c = Calendar.getInstance();
                        String message = Uri.encode("("+c.get(Calendar.HOUR_OF_DAY)+":"+c.get(Calendar.MINUTE)  +") "+messageText.getText().toString().trim());
                        String key = getSharedPreferences(Chat.activeChat.ChatId, MODE_PRIVATE).getString("password","");
                        String vector = Chat.activeChat.ChatId + Integer.toString(Integer.parseInt(Chat.activeChat.ChatId) * Integer.parseInt(Chat.activeChat.ChatId) * 12) + Chat.activeChat.ChatId + floppy;
                        customEncryptorAES encryptor = new customEncryptorAES(key,vector);


                        message = Uri.encode(encryptor.encrypt(message));
                        String data = new JSONObject()
                                .put("senderName", getSharedPreferences("main", MODE_PRIVATE).getString("username", ""))
                                .put("senderId", getSharedPreferences("main", MODE_PRIVATE).getString("userid", ""))
                                .put("chatId", Chat.activeChat.ChatId)
                                .put("message", message)
                                .toString();
                                //?????????????????? ???????????? ?? ???????? ??????????
                                messageText.setText("");

                        //???????????????? ?????????????? ????????????????????????
                        if (Chat.activeChat.Members != null) {
                            for (String member : Chat.activeChat.Members) {
                                new MakeReq().execute(chatWindow.this.getString(R.string.apiUrl) + "sData.php?api_key=" + getSharedPreferences("main", MODE_PRIVATE).getString("api_key", "") + "&type=text&recepient=" + member.split("#")[1] + "&data=" + data);
                                scrollChatMessages.post(new Runnable() {

                                    @Override
                                    public void run() {
                                        scrollChatMessages.fullScroll(ScrollView.FOCUS_DOWN);
                                    }
                                });
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        });
    }


    //???????????????? ???????????????????? ?????????? ??????????????????
    public void AddNewMessage(String message) {
        View newMessage = getLayoutInflater().inflate(R.layout.chat_message_template, null);
        TextView newMsg = newMessage.findViewById(R.id.msgTemplate);
        newMsg.setText(message);
        newMsg.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                PopupMenu popupMenu = new PopupMenu(getApplicationContext(), view);
                popupMenu.inflate(R.menu.message_onclick);

                popupMenu
                        .setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                String[] msg = message.split("\n");
                                StringBuilder sb = new StringBuilder();
                                for (int i = 1;i<msg.length;i++){
                                    sb.append(msg[i]);
                                    sb.append("\n");
                                }
                                msg = sb.toString().substring(0,sb.toString().length()-1).split("\\) ");
                                sb = new StringBuilder();
                                for (int i = 1;i<msg.length;i++){
                                    sb.append(msg[i]);
                                    sb.append(") ");
                                }
                                String msgText = sb.toString().substring(0,sb.toString().length()-2);

                                switch (item.getItemId()) {
                                    case R.id.copychatbtn:
                                        clipData = ClipData.newPlainText("text",msgText);
                                        clipboardManager=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
                                        clipboardManager.setPrimaryClip(clipData);
                                        Toast.makeText(getApplicationContext(),"?????????? ?????????????????? ????????????????????!",Toast.LENGTH_SHORT).show();
                                        return true;
                                    case R.id.replychatbtn:
                                        messageText.requestFocus();
                                        messageText.setText("\n\n|vvvvvvvvvvvvvvvvvvvvv|\n"+msgText+"\n|vvvvvvvvvvvvvvvvvvvvv|");
                                        messageText.requestFocus();
                                        return true;
                                    default:
                                        return false;
                                }

                            }
                        });
                popupMenu.show();
            return true;}
        });
        chatMessages.addView(newMessage);
        if (scrolledToBottom){
            scrollChatMessages.post(new Runnable() {

                @Override
                public void run() {
                    scrollChatMessages.fullScroll(ScrollView.FOCUS_DOWN);
                }
            });
        }else {
            Toast.makeText(getApplicationContext(),"???? ???????????????? ?????????? ?????????????????? ?? ?????????????? ????????!",Toast.LENGTH_SHORT).show();
        }
    }

    private void AddAllMessages() {

        if (Chat.activeChat.ChatMessages != null) {
            JSONArray messages = Chat.activeChat.ChatMessages;
            for (int i = 0; i < messages.length(); i++) {

                try {
                    JSONObject message = new JSONObject(messages.getString(i));
                    AddNewMessage(message.getString("senderName") + "#" + message.getString("senderId") + ":\n" + message.getString("message"));
                    scrollChatMessages.post(new Runnable() {

                        @Override
                        public void run() {
                            scrollChatMessages.fullScroll(ScrollView.FOCUS_DOWN);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public void AddNewMember(String member) {
        View newMember = getLayoutInflater().inflate(R.layout.chat_message_template, null);
        TextView newMsg = newMember.findViewById(R.id.msgTemplate);
        newMsg.setText(member);
        newMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clipData = ClipData.newPlainText("text",member.split("#")[1]);
                clipboardManager=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
                clipboardManager.setPrimaryClip(clipData);
                Toast.makeText(getApplicationContext(),"ID ???????????????????????? "+member+" ????????????????????!",Toast.LENGTH_SHORT).show();
            }
        });
        chatMembersLayout.addView(newMember);
    }

    private void startGettingMembers(){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (true) {

                    new GetMembers(chatWindow.this).execute(chatWindow.this.getString(R.string.apiUrl) + "getChatInfo.php?api_key=" + getSharedPreferences("main", MODE_PRIVATE).getString("api_key", "") + "&chat_id=" + Chat.activeChat.ChatId);
                    synchronized(this) {
                        try {
                            wait(2000);
                        } catch(InterruptedException ie){}
                    }
                }

            }
        };
        membersTh = new Thread(runnable);
        // ?????????????????? ??????????
        membersTh.start();
    }

    public static class GetMessages extends AsyncTask<String, String, String> {


        HttpURLConnection connection = null;
        BufferedReader reader = null;
        loggedWindow loggedWindow;
        chatWindow chatWindow;
        AppCompatActivity ap;

        public GetMessages(AppCompatActivity ap) {
            this.ap = ap;
            if (ap.getClass()==chatWindow.class){
                chatWindow = (com.alexcux.encrysharemob.chatWindow) ap;
            }else if (ap.getClass()==loggedWindow.class){
                loggedWindow = (com.alexcux.encrysharemob.loggedWindow) ap;
            }
        }


        @Override
        protected String doInBackground(String... strings) {
            try {
                //?????????????????? ??????????????
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

            try {
                if (result!=null) {

                if (result.contains("Error")) {
                    msgService.noResponseCounter++;
                } else {
                    msgService.noResponseCounter = 0;
                    JSONObject jsonObject = new JSONObject(result);
                    if (jsonObject.getString("type").equals("text")) {
                        JSONObject messageObject = new JSONObject(jsonObject.getString("data"));
                        String chatId = messageObject.getString("chatId");
                        String senderId = messageObject.getString("senderId");
                        String senderName = messageObject.getString("senderName");
                        String message = messageObject.getString("message");
                        String key = ap.getSharedPreferences(chatId, MODE_PRIVATE).getString("password","");
                        String vector = chatId + Integer.toString(Integer.parseInt(chatId) * Integer.parseInt(chatId) * 12) + chatId + floppy;

                        message = new customEncryptorAES(key, vector).decrypt(Uri.decode(message));
                        message = Uri.decode(message);
                        Chat actChat = null;
                        for (int i = 0; i < com.alexcux.encrysharemob.loggedWindow.Chats.length; i++) {
                            if (com.alexcux.encrysharemob.loggedWindow.Chats[i].ChatId.equals(chatId)) {
                                actChat = com.alexcux.encrysharemob.loggedWindow.Chats[i];
                            }
                        }

                        if (actChat != null && loggedWindow != null) {
                            if (Chat.activeChat.ChatId.equals(actChat.ChatId)) {
                                new GetMessages(com.alexcux.encrysharemob.chatWindow.activeChatWindow).onPostExecute(result);
                            } else {
                                actChat.UpdateMessages(loggedWindow, senderName, senderId, message);
                            }

                        } else if (actChat != null && chatWindow != null) {
                            actChat.UpdateMessages(chatWindow, senderName, senderId, message);

                        }
                    }else if (jsonObject.getString("type").equals("pswd")){
                        JSONObject jb = new JSONObject(jsonObject.getString("data"));
                        String password = Uri.decode(jb.getString("password"));
                        ap.getSharedPreferences(jb.getString("chat_id"), MODE_PRIVATE).edit().putString("password",password).commit();
                    }
                }
            }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class GetMembers extends AsyncTask<String, String, String> {

        HttpURLConnection connection = null;
        BufferedReader reader = null;
        chatWindow chatWindow;

        public GetMembers(chatWindow cw) {
            chatWindow = cw;
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                //?????????????????? ??????????????
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

            try {
                if (result!=null){
                    JSONObject jsonObject = new JSONObject(result);
                    JSONArray membersArray = jsonObject.getJSONArray("members");
                    Chat.activeChat.Members = new String[membersArray.length()];
                    chatMembersLayout.removeAllViews();
                    for (int i = 0; i < membersArray.length(); i++) {
                        JSONObject currentMember = membersArray.getJSONObject(i);
                        String membername = currentMember.getString("member_name") + "#" + currentMember.getString("member_id");
                        Chat.activeChat.Members[i] = membername;
                        chatWindow.AddNewMember(membername);
                    }
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    }

    private class MakeReq extends AsyncTask<String, String, String> {

        HttpURLConnection connection = null;
        BufferedReader reader = null;

        @Override
        protected String doInBackground(String... strings) {
            try {
                //?????????????????? ??????????????
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
        }
    }

    private class GetChatOwner extends AsyncTask<String, String, String>{

        HttpURLConnection connection = null;
        BufferedReader reader = null;

        @Override
        protected String doInBackground(String... strings) {
            try {
                //?????????????????? ??????????????
                URL url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                if (!loggedWindow.offlineMode){
                    connection.connect();
                }
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



            try {
                if (result!=null){
                    JSONObject chatInfo = new JSONObject(result);
                    String owner = chatInfo.getString("chat_owner");
                    String uid = getSharedPreferences("main", MODE_PRIVATE).getString("userid", "");
                    if (!owner.equals(uid)){
                        addMemberBtn.setVisibility(View.INVISIBLE);
                        removeMemberBtn.setVisibility(View.GONE);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}