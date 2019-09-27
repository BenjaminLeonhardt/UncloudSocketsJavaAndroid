package com.example.uncloudandroid;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

public class PeerFilesActivity extends AppCompatActivity {

    public static String ipAdresse;
    String name;
    String os;
    int peersPort = 5001;

    public ArrayList<File> listFiles = new ArrayList<>();
    public static final int heartbeat=1;
    public static final int sendeAlleDateien=2;
    public static final int sendeDatei=3;
    public static final int chatOeffnen=4;
    public static final int chatMessage=5;
    TextView ipAdresseView;
    TextView nameView;
    TextView osView;
    public RecyclerView recyclerView;
    public RecyclerView.LayoutManager layoutManager;
    public FileRecyclerAdapter adapter;
    public static Semaphore semaphoreFilesListe =  new Semaphore(0);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peer_files);

        ipAdresse = getIntent().getStringExtra("ipAdresse");
        name = getIntent().getStringExtra("name");
        os = getIntent().getStringExtra("os");

        ipAdresseView = findViewById(R.id.ip);
        nameView = findViewById(R.id.name);
        osView = findViewById(R.id.os);

        ipAdresseView.setText(ipAdresse);
        nameView.setText(name);
        osView.setText(os);

        GetFiles thread = new GetFiles(ipAdresse);
        thread.start();
        try {
            semaphoreFilesListe.acquire();
            recyclerView = findViewById(R.id.FilesRecyclerview);
            layoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(layoutManager);

            adapter = new FileRecyclerAdapter(listFiles,this);
            recyclerView.setHasFixedSize(true);
            recyclerView.setAdapter(adapter);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void chatOeffnenButtonClick(View view){

        Intent intent = new Intent(this,ChatActivity.class);
        intent.putExtra("IPAdresse",ipAdresse);
        intent.putExtra("Name",name);
        startActivity(intent);
    }


    public static boolean esWirdGedownloadet=false;
    public class GetFiles extends Thread{


        String ip="";
        int port=peersPort;
        NetworkInterface netInter = null;
        public PrintWriter out;
        public BufferedReader in;
        public Socket socket = null;

        public GetFiles(String ip){

            this.ip = ip;
        }

        @Override
        public void run() {
            char[] buf= new char[65535];

            try {
                socket = new Socket(ip,port);
                int timeout = 1000;
                socket.setSoTimeout(timeout);
                out = new PrintWriter(socket.getOutputStream(),true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            } catch (SocketException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            out.write("beg{2:"+PeersListeActivity.benutzerName+":"+ip+":Android:♥}end");
            out.flush();
            listFiles = new ArrayList<>();
            int responseLength;
            while(true){
                if(esWirdGedownloadet){
                    UIAktualisiererFile aktualisierer = new UIAktualisiererFile(getApplicationContext());
                    aktualisierer.start();
                }

                responseLength = 0;
                try {

                    responseLength = in.read(buf,0, buf.length);
                    if(responseLength>0){
                        System.out.println("anzahl empfangener bytes: "+responseLength);
                        System.out.println(buf);
                        String response = new String(buf);
                        int begin = response.indexOf("beg{");
                        int ende = response.indexOf("}end");
                        String responseOhneHeaderUndTailer = "";
                        for (int j = begin + 4; j < ende; j++) {
                            responseOhneHeaderUndTailer += response.charAt(j);
                        }
                        int aktion = -1;

                        if (responseOhneHeaderUndTailer.length() > 1) {
                            aktion = Integer.parseInt("" + responseOhneHeaderUndTailer.charAt(0));
                        }
                        if (aktion == sendeAlleDateien) {
                            String[] ohneAktion = responseOhneHeaderUndTailer.split("☻");
                            String[] dateienMitGroesse = ohneAktion[1].split("♦");
                            for(String item : dateienMitGroesse){
                                String[] dateiUndGroesse = item.split(";");
                                File file = new File(dateiUndGroesse[0],dateiUndGroesse[1]);
                                listFiles.add(file);

                            }
                            semaphoreFilesListe.release();

                        }

                    }else if(responseLength==0) {
                        break;

                    }

                    Thread.sleep(1000);
                }catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    //e.printStackTrace();
                }
            }
        }
    }

    public class UIAktualisiererFile extends Thread{
        Context context;

        public UIAktualisiererFile(){

        }

        public UIAktualisiererFile(Context context){
            this.context = context;
        }

        @Override
        public void run() {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {

                    try {
                        recyclerView = findViewById(R.id.FilesRecyclerview);
                        layoutManager = new LinearLayoutManager(context);
                        recyclerView.setLayoutManager(layoutManager);

                        adapter = new FileRecyclerAdapter(listFiles,context);
                        recyclerView.setHasFixedSize(true);
                        recyclerView.setAdapter(adapter);

                    }catch (Exception ex) {
                        System.out.println(ex.toString());
                    }
                }
            });
        }
    }
}
