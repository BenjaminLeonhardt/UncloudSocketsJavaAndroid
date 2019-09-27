package com.example.uncloudandroid;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

import static com.example.uncloudandroid.ChatActivity.chatListe;


public class PeersListeActivity extends AppCompatActivity {

    public static final boolean debug = false;
    public static final int heartbeat=1;
    public static final int sendeAlleDateien=2;
    public static final int sendeDatei=3;
    public static final int chatOeffnen=4;
    public static final int chatMessage=5;
    public static final int SERVERPORT = 5000;
    public static final String SERVERIP = "192.168.2.101";
    public static PrintWriter out;
    public static BufferedReader in;
    public RecyclerView recyclerView;
    public RecyclerView.LayoutManager layoutManager;
    public ArrayList<Peer> list = new ArrayList<>();
    public RecyclerAdapter adapter;
    public static Semaphore semaphoreHeartbeat =  new Semaphore(0);
    public static ServerSocket serverSocket;
    public static ServerSocket chatServerSocket;
    public static String benutzerName="";
    public static String eigeneIP="";
    public Context context = this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_peers_liste);


            serverSocket = new ServerSocket(5001);
            chatServerSocket = new ServerSocket(5002);


            Intent intent = getIntent();
            String ipAdresse = intent.getStringExtra("IPAdresse");
            int port = Integer.parseInt(intent.getStringExtra("Port"));
            benutzerName = intent.getStringExtra("Name");

            heartbeat thread = new heartbeat(ipAdresse,port);
            thread.start();

            Listener listener = new Listener();
            listener.start();

            ListenerChat listenerChat = new ListenerChat();
            listenerChat.start();

            semaphoreHeartbeat.acquire();

            recyclerView = findViewById(R.id.recyclerview);
            layoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(layoutManager);

            adapter = new RecyclerAdapter(list,this);
            recyclerView.setHasFixedSize(true);
            recyclerView.setAdapter(adapter);



            //UIAktualisierer aktualisierer = new UIAktualisierer(this);
            //aktualisierer.start();


        } catch (InterruptedException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }

    }

    public class UIAktualisierer extends Thread{
        Context context;

        public UIAktualisierer(){

        }

        public UIAktualisierer(Context context){
            this.context = context;
        }

        @Override
        public void run() {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {

                    try {
                        recyclerView = findViewById(R.id.recyclerview);
                        layoutManager = new LinearLayoutManager(context);
                        recyclerView.setLayoutManager(layoutManager);

                        adapter = new RecyclerAdapter(list, context);
                        recyclerView.setHasFixedSize(true);
                        recyclerView.setAdapter(adapter);

                    }catch (Exception ex) {
                        System.out.println(ex.toString());
                    }
                }
            });
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        recyclerView = findViewById(R.id.recyclerview);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new RecyclerAdapter(list,this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
    }

    public class Listener extends Thread{
        @Override
        public void run() {
            try {
                while (true){
                    Socket socket = serverSocket.accept();

                    Worker worker = new Worker(socket);
                    worker.start();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class Worker extends Thread{

        Socket socketPeer;

        public Worker(Socket socket){
            socketPeer = socket;
        }

        @Override
        public void run() {
            try {
                PrintWriter outputWriter = new PrintWriter(socketPeer.getOutputStream(),true);
                BufferedReader inputReader = new BufferedReader(new InputStreamReader(socketPeer.getInputStream()));
                while(socketPeer.isConnected()){

                    char [] buffer = new char[65535];
                    int responseLength = 0;
                    responseLength = inputReader.read(buffer,0, buffer.length);

                    if(responseLength>0) {
                        System.out.println("anzahl empfangener bytes: " + responseLength);
                        System.out.println(buffer);
                        String content = new String(buffer);

                        int begin = content.indexOf("beg{");
                        int ende = content.indexOf("}end");

                        String contentOhneHeaderUndTailer = "";
                        for (int j = begin + 4; j < ende; j++) {
                            contentOhneHeaderUndTailer += content.charAt(j);
                        }
                        int aktion = -1;

                        aktion = Integer.parseInt("" + contentOhneHeaderUndTailer.charAt(0));
                        if (aktion == sendeAlleDateien) {

                            java.io.File[] files = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).listFiles();

                            String answer = "beg{2☻";
                            for (int i = 0; i < files.length; i++)
                            {
                                answer += files[i].getName() + ";" + files[i].length()+"♦";
                            }
                            answer += "}end";

                            Log.d("Files", answer);

                            outputWriter.write(answer);
                            outputWriter.flush();

                        }else if(aktion == sendeDatei) {
                            String[] mesageItems = contentOhneHeaderUndTailer.split(":");
                            if (mesageItems[3].contains("..\\")|| mesageItems[3].contains("../")) {
                                return;
                            }

                            SendDatei(socketPeer.getOutputStream(), mesageItems[3]);
                        }

                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void SendDatei(OutputStream out, String mesageItem) {

        try {
            java.io.File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) +"/"+ mesageItem);
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] buffer = new byte[(int) file.length()];
            fileInputStream.read(buffer,0, buffer.length);
            out.write(buffer);
            out.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Socket socket = null;

    public class heartbeat extends Thread{
        String ServerIp="";
        int ServerPort=-1;
        NetworkInterface netInter = null;

        public heartbeat(String _ip,int _port){
            ServerIp=_ip;
            ServerPort=_port;
        }


        @Override
        public void run() {
            char[] buf= new char[65535];

            try {
                if(debug){
                    ServerIp = SERVERIP;
                }else{
                    netInter = NetworkInterface.getByName("wlan0");
                    List<InetAddress> liste = Collections.list(netInter.getInetAddresses());
                    InetAddress tmp = liste.get(liste.size() - 1);
                    eigeneIP = tmp.getHostAddress();
                }

                socket = new Socket(ServerIp,ServerPort);
                out = new PrintWriter(socket.getOutputStream(),true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            } catch (SocketException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            while(true){
                int responseLength;
                try {
                    out.write("beg{1:"+benutzerName+":"+eigeneIP+":Android:♥}end");
                    out.flush();
                    responseLength = in.read(buf,0, buf.length);
                    if(responseLength>0){
                        //System.out.println("anzahl empfangener bytes: "+responseLength);
                        //System.out.println(buf);
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
                        if (aktion == heartbeat) {
                            int indexAktionsTrenner = responseOhneHeaderUndTailer.indexOf("☻");
                            String responeOhneAktion = "";
                            String[] responeOhneAktionArray = responseOhneHeaderUndTailer.split("☻");



                            if(responeOhneAktionArray.length>1){
                                responeOhneAktion = responeOhneAktionArray[1];


                                ArrayList<Peer> peersListeLokal = new ArrayList<Peer>();

                                ArrayList<String> peersAlsStrings = new ArrayList<String>();
                                String [] peersAlsStringsArray = responeOhneAktion.split("♥");


                                for (String item : peersAlsStringsArray) {
                                    if (!item.equals("")) {
                                        Peer p = new Peer();
                                        String[] einzelnerPeerAlsArray = item.split(":");
                                        if(einzelnerPeerAlsArray.length>=3){
                                            p.ip = einzelnerPeerAlsArray[0];
                                            p.name = einzelnerPeerAlsArray[1];
                                            p.os = einzelnerPeerAlsArray[2];

                                            peersListeLokal.add(p);
                                        }

                                    }
                                }

                                if(list.size()!=peersListeLokal.size()){

                                    list = peersListeLokal;
                                }
                                semaphoreHeartbeat.release();
                                UIAktualisierer aktualisierer = new UIAktualisierer(getApplicationContext());
                                aktualisierer.start();
                            }
                        }
                    }
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public class ListenerChat extends Thread{
        @Override
        public void run() {
            try {
                while (true){
                    Socket socket = chatServerSocket.accept();



                    WorkerChat workerChat = new WorkerChat(socket);
                    workerChat.start();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class ChatReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Intent in = new Intent("chatAktualisieren");
            Bundle extras = new Bundle();
            //extras.putString("chatAktualisieren", otpCode);
            in.putExtras(extras);
            context.sendBroadcast(in);
        }
    }


    public class WorkerChat extends Thread{

        Socket socketPeer;

        public WorkerChat(Socket socket){
            socketPeer = socket;
        }

        @Override
        public void run() {
            try {
                PrintWriter outputWriter = new PrintWriter(socketPeer.getOutputStream(),true);
                BufferedReader inputReader = new BufferedReader(new InputStreamReader(socketPeer.getInputStream()));
                while(socketPeer.isConnected()){

                    char [] buffer = new char[65535];
                    int responseLength = 0;
                    responseLength = inputReader.read(buffer,0, buffer.length);

                    if(responseLength>0) {
                        System.out.println("anzahl empfangener bytes: " + responseLength);
                        System.out.println(buffer);
                        String content = new String(buffer);

                        int begin = content.indexOf("beg{");
                        int ende = content.indexOf("}end");

                        String contentOhneHeaderUndTailer = "";
                        for (int j = begin + 4; j < ende; j++) {
                            contentOhneHeaderUndTailer += content.charAt(j);
                        }
                        int aktion = -1;

                        aktion = Integer.parseInt("" + contentOhneHeaderUndTailer.charAt(0));
                        if (aktion == chatOeffnen) {

                            ChatObject chatObject = new ChatObject();

                        }else if(aktion == chatMessage) {
                            String [] contentGesplittet =  contentOhneHeaderUndTailer.split("☻");
                            final String empfangenerText = contentGesplittet[3];
                            boolean gefunden = false;
                            for (ChatObject obj: chatListe) {
                                if(obj.IP.equals(contentGesplittet[2])){
                                    gefunden = true;
                                    obj.chatText += "\n"+ contentGesplittet[1]+": "+empfangenerText;


                                }
                            }
                            if(!gefunden){
                                ChatObject chatObject = new ChatObject();
                                chatObject.chatPeer = socketPeer;
                                chatObject.chatText += "\n"+ contentGesplittet[1]+": "+ empfangenerText;
                                chatObject.namePeer = contentGesplittet[1];
                                chatObject.IP = contentGesplittet[2];
                                chatListe.add(chatObject);
                            }


                        }

                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
