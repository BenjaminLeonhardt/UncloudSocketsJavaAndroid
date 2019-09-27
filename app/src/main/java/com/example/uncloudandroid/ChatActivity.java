package com.example.uncloudandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {

    public static ArrayList<ChatObject> chatListe = new ArrayList<>();

    String ip="";
    String name="";

    public static boolean runChat = false;
    TextView chatText;
    EditText neueNachricht;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatText = findViewById(R.id.chatTextView);
        neueNachricht = findViewById(R.id.neueNachrichtEditText);
        ip = getIntent().getStringExtra("IPAdresse");
        name = getIntent().getStringExtra("Name");


        for (ChatObject obj : chatListe) {
            if(obj.namePeer.equals(name)){
                chatText.setText(obj.chatText);
                break;
            }
        }


        ChatAktualisierer ca = new ChatAktualisierer();
        ca.start();

        if(savedInstanceState!=null){
            chatText.setText(savedInstanceState.getString("chatText"));
            neueNachricht.setText(savedInstanceState.getString("neueNachrichtText"));
        }
    }



    public void chatNachrichtSendenButtonClick(View view){

        TextView chatText = findViewById(R.id.chatTextView);
        EditText neueNachricht = findViewById(R.id.neueNachrichtEditText);//string text = "beg{" + "5" + "☻" + Form1.eigenerName + "☻" + ipAddress.ToString() + "☻" + chatEingabeFeld.Text + "☻" + "}end";

        for (ChatObject obj : chatListe) {
            if(obj.namePeer.equals(name)){
                try {
                    PrintWriter outputWriter = new PrintWriter(obj.chatPeer.getOutputStream(),true);
                    outputWriter.write("beg{" + "5" + "☻" + PeersListeActivity.benutzerName + "☻" + PeersListeActivity.eigeneIP + "☻" + neueNachricht.getText() + "☻" + "}end");
                    outputWriter.flush();
                    obj.chatText += "\n" + PeersListeActivity.benutzerName +": " + neueNachricht.getText();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        neueNachricht.setText("");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        TextView chatText = findViewById(R.id.chatTextView);
        EditText neueNachricht = findViewById(R.id.neueNachrichtEditText);

        outState.putString("chatText", chatText.getText()+"");
        outState.putString("neueNachrichtText", neueNachricht.getText()+"");
    }


    public class ChatAktualisierer extends Thread{

        @Override
        public void run() {
            super.run();
            runChat=true;
            while(runChat) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (ChatObject obj : chatListe) {
                            if (obj.namePeer.equals(name)) {
                                if (!chatText.getText().equals(obj.chatText))
                                    chatText.setText(obj.chatText);
                                break;
                            }
                        }
                    }
                });
            }
        }
    }
}
