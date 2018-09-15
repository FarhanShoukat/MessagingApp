package com.example.whatsapp;

/**
 * Created by farha on 03-Oct-17.
 */

public class AttachmentCell {
    int imageId;
    String name;

    public AttachmentCell(int imageId, String name) {
        this.imageId = imageId;
        this.name = name;
    }

    public int getImageId() {
        return imageId;
    }

    public String getName() {
        return name;
    }
}
