package com.example.whatsapp;

import android.net.Uri;

/**
 * Created by farha on 09-Nov-17.
 */

public class ContactCell {
    private Uri photo;
    private String name;
    private String status;
    private String phone;

    public ContactCell(Uri photo, String name, String status, String phone) {
        this.photo = photo;
        this.name = name;
        this.status = status;
        this.phone = phone;
    }

    public Uri getPhoto() {
        return photo;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhoto(Uri photo) {
        this.photo = photo;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
