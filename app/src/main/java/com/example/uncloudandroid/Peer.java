package com.example.uncloudandroid;

import java.net.Socket;

public class Peer {
    public Socket socket;
    public int id;
    public String name;
    public String ip;
    public String os;
    public long letzterKontakt;

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public void setLetzterKontakt(long letzterKontakt) {
        this.letzterKontakt = letzterKontakt;
    }

    public Socket getSocket() {
        return socket;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIp() {
        return ip;
    }

    public String getOs() {
        return os;
    }

    public long getLetzterKontakt() {
        return letzterKontakt;
    }
}
