package com.example.uncloudandroid;

public class File {

    String name;
    String groesse;
    String progress;

    public File(String name, String groesse) {
        this.name = name;
        this.groesse = groesse;
    }

    public String getProgress() {
        return progress;
    }

    public void setProgress(String progress) {
        this.progress = progress;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroesse() {
        return groesse;
    }

    public void setGroesse(String groesse) {
        this.groesse = groesse;
    }
}
