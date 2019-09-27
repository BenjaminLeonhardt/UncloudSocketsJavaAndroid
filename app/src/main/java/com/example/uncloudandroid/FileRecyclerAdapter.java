package com.example.uncloudandroid;

import android.content.Context;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.example.uncloudandroid.PeerFilesActivity.esWirdGedownloadet;

public class FileRecyclerAdapter extends RecyclerView.Adapter<FileRecyclerAdapter.MyFileViewHolder>  {

    public Context context;

    private List<File> list;

    public FileRecyclerAdapter(ArrayList<File> list, Context context){
        this.list = list;
        this.context = context;
    }


    @NonNull
    @Override
    public FileRecyclerAdapter.MyFileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_view_layout,parent,false);
        MyFileViewHolder myFileViewHolder = new MyFileViewHolder(view, context, list);
        return myFileViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull FileRecyclerAdapter.MyFileViewHolder holder, int position) {
        holder.name.setText(list.get(position).name);
        holder.groesse.setText(list.get(position).groesse);
        holder.progress.setText(list.get(position).progress);
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyFileViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView name;
        TextView groesse;
        TextView progress;
        Context context;
        List<File> fileList;

        public MyFileViewHolder(@NonNull View view, Context context, List<File> list) {
            super(view);
            groesse = view.findViewById(R.id.fileSize);
            name = view.findViewById(R.id.fileName);
            progress = view.findViewById(R.id.progress);
            view.setOnClickListener(this);
            this.context = context;
            fileList = list;
        }

        @Override
        public void onClick(View v) {
            try{
                GetFile getFile = new GetFile(v, fileList.get(getAdapterPosition()).name,fileList.get(getAdapterPosition()).groesse,getAdapterPosition(),fileList,context);
                getFile.start();
            }catch (Exception e){
                e.printStackTrace();
            }


            /*Intent intent = new Intent(context, PeerFilesActivity.class);
            intent.putExtra("name",peerList.get(getAdapterPosition()).name);
            intent.putExtra("groesse",peerList.get(getAdapterPosition()).groesse);
            intent.putExtra("progress",peerList.get(getAdapterPosition()).progress);
            context.startActivity(intent);*/
        }
    }

    public static class GetFile extends Thread {
        View ipView;
        String ipPeer = "";
        String eigeneIP = "";
        String dateiName;
        int groesse=-1;
        NetworkInterface netInter = null;
        int positionInListe = -1;
        List<File> fileList;
        Context context;

        public GetFile(View view, String name,String groesse, int position,List<File> fileList, Context context) {
            try {
                //ipView = view.findViewById(R.id.ip);
                ipPeer = PeerFilesActivity.ipAdresse;
                dateiName = name;
                this.groesse = Integer.parseInt(groesse);
                netInter = NetworkInterface.getByName("wlan0");
                List<InetAddress> liste = Collections.list(netInter.getInetAddresses());
                InetAddress tmp = liste.get(liste.size() - 1);
                eigeneIP = tmp.getHostAddress();
                positionInListe = position;
                this.fileList = fileList;
                this.context = context;

            } catch (SocketException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void run() {
            super.run();
            FileOutputStream writer = null;
            BufferedWriter bw = null;
            Socket socket = null;
            int geschrieben = 0;
            try {
                socket = new Socket(ipPeer, 5001);
                socket.setSoTimeout(1000);
                PrintWriter outputWriter = new PrintWriter(socket.getOutputStream(), true);
                InputStream inputStream = socket.getInputStream();

                while (socket.isConnected()) {

                    byte[] buffer = new byte[65535];
                    //string text = "beg{" + (int)aktionEnum.sendeDatei + ":" + nameTextBox.Text + ":" + ipAddress.ToString() + ":" + filename + ":♥" + "}end";
                    outputWriter.write("beg{3:" + PeersListeActivity.benutzerName + ":" + eigeneIP + ":" + dateiName + ":♥}end");
                    outputWriter.flush();
                    int responseLength = 0;
                    responseLength = inputStream.read(buffer, 0, buffer.length);

                    writer = new FileOutputStream(Environment.getExternalStorageDirectory().toString()+"/Download/"+dateiName);

                    while (responseLength > 0) {
                        esWirdGedownloadet = true;
                        writer.write( buffer,0,responseLength);
                        geschrieben += responseLength;
                        long fortschrittInProzent = ((geschrieben * 100L) / groesse);
                        fileList.get(positionInListe).progress = String.valueOf(fortschrittInProzent) + "%";

                        responseLength = inputStream.read(  buffer, 0, responseLength);
                    }

                }
            } catch (Exception ex) {
                try {
                    esWirdGedownloadet = false;
                    writer.flush();
                    writer.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ex.printStackTrace();
            }
        }
    }
}
