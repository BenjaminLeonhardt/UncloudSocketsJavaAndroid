package com.example.uncloudandroid;

import java.net.Socket;

public class ChatObject {
    public Socket chatPeer;
    public String chatText = "";
    public String namePeer = "";
    public String IP = "";

    public ChatObject() {
    }

    public ChatObject(Socket chatPeer, String chatText) {
        this.chatPeer = chatPeer;
        this.chatText = chatText;
    }

    public Socket getChatPeer() {
        return chatPeer;
    }

    public void setChatPeer(Socket chatPeer) {
        this.chatPeer = chatPeer;
    }

    public String getChatText() {
        return chatText;
    }

    public void setChatText(String chatText) {
        this.chatText = chatText;
    }
}
