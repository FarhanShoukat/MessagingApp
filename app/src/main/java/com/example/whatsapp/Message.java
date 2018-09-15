package com.example.whatsapp;

import android.net.Uri;

import java.util.Date;

/**
 * Created by farha on 16-Nov-17.
 */

public abstract class Message {
    private String date;
    private String sender;

    public Message(String sender) {
        this.sender = sender;
    }

    public String  getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }
}

class TextMessage extends Message {
    private String message;

    public TextMessage(String sender, String message) {
        super(sender);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

class ImageMessage extends Message {
    private Uri imageUri;

    public ImageMessage(String sender, Uri imageUri) {
        super(sender);
        this.imageUri = imageUri;
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
    }
}
