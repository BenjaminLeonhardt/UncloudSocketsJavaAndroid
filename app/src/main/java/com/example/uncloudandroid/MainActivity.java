package com.example.uncloudandroid;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public void verbindenButtonClick(View view){
        EditText ipText = findViewById(R.id.ipAdresseEditText);
        EditText port = findViewById(R.id.portEditText);
        EditText name = findViewById(R.id.nameEditText);

        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1000);
        int permission = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        if(permission == PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this,"zugriff auf daten erlaubt",Toast.LENGTH_LONG).show();
            /*File directory2 = getFilesDir();
            File[] listOfFiles1  = directory2.listFiles();
            File[] listOfFiles = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).listFiles();

            String path = Environment.getExternalStorageDirectory().toString()+"/Download";
            File directory = new File(path);
            File[] files = directory.listFiles();*/
        }else{
            Toast.makeText(this,"kein zugriff auf daten erhalten",Toast.LENGTH_LONG).show();
        }

        String ipString = ipText.getText().toString();
        String portString = port.getText().toString();
        String nameString = name.getText().toString();


        Intent intent = new Intent(this,PeersListeActivity.class);
        intent.putExtra("IPAdresse",ipString);
        intent.putExtra("Port",portString);
        intent.putExtra("Name",nameString);
        startActivity(intent);
    }


}
