package com.example.whatsapp;

import android.net.Uri;

/**
 * Created by farha on 07-Nov-17.
 */

public class PersonCell {
    private Uri photo;
    private String name;
    private String lastMessage;
    private String date;
    private String phone;

    public PersonCell(Uri photo, String name, String lastMessage, String date, String phone) {
        this.photo = photo;
        this.name = name;
        this.lastMessage = lastMessage;
        this.date = date;
        this.phone = phone;
    }

    public Uri getPhoto() {
        return photo;
    }

    public String getName() {
        return name;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public String getDate() {
        return date;
    }

    public String getPhone() {
        return phone;
    }
}
