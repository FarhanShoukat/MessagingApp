package com.example.whatsapp;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.github.chrisbanes.photoview.PhotoView;

public class ShowImage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);

        Intent intent = getIntent();
        String path = (String) intent.getSerializableExtra("image");
        PhotoView view = (PhotoView) findViewById(R.id.image);
        view.setImageURI(Uri.parse(path));
    }
}
