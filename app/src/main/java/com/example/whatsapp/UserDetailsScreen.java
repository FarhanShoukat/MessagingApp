package com.example.whatsapp;

import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;

public class UserDetailsScreen extends AppCompatActivity {

    private String phone;
    private String name;
    private File image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details_screen);

        Intent intent = getIntent();
        name = (String) intent.getSerializableExtra("name");
        phone = (String)intent.getSerializableExtra("phone");

        TextView textView = (TextView) findViewById(R.id.name);
        textView.setText(name);
        textView = (TextView) findViewById(R.id.phone);
        textView.setText(phone);

        final TextView status = (TextView) findViewById(R.id.status);
        status.setText("");

        FirebaseDatabase.getInstance().getReference("users").child(phone).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    status.setText(dataSnapshot.child("status").getValue(String.class));
                }
                catch (Exception ex) {}
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        ImageView imageView = (ImageView) findViewById(R.id.contact_image);
        ContextWrapper wrapper = new ContextWrapper(getApplicationContext());
        File file = wrapper.getDir("profilePictures",MODE_PRIVATE);
        file = new File(file, phone + ".jpg");
        image = file;
        if(file.exists()) {
            imageView.setImageURI(Uri.parse(file.getAbsolutePath()));
        }

        setMedia();
    }

    private void setMedia() {
        FirebaseDatabase.getInstance().getReference("messages").child(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() + " " + phone).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int i = 0;
                LinearLayout linearLayout = (LinearLayout) findViewById(R.id.media);
                ContextWrapper wrapper = new ContextWrapper(getApplicationContext());
                File file = wrapper.getDir(phone,MODE_PRIVATE);
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    if(ds.child("type").getValue(String.class).equals("image")) {
                        String[] key = ds.getKey().split(" ");
                        File file1 = new File(file, key[2] + " " + key[0] + " " + key[1] + ".jpg");
                        if(file1.exists()) {
                            i++;
                            ImageView imageView = new ImageView(UserDetailsScreen.this);
                            AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(200, 200);
                            imageView.setLayoutParams(layoutParams);
                            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            imageView.setTag(file1.getAbsolutePath());
                            imageView.setImageURI(Uri.parse(file1.getAbsolutePath()));
                            imageView.setPadding(5, 5, 5, 5);
                            imageView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String s = (String) v.getTag();
                                    if(s != null && new File(s).exists()) {
                                        Intent intent = new Intent(UserDetailsScreen.this, ShowImage.class);
                                        intent.putExtra("image", s);
                                        startActivity(intent);
                                    }
                                }
                            });
                            linearLayout.addView(imageView);
                        }
                    }
                }
                if(i == 0) {
                    ((LinearLayout) findViewById(R.id.media_parent)).setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void imageClicked(View view) {
        if(image.exists()) {
            Intent intent = new Intent(this, ShowImage.class);
            intent.putExtra("image", image.getAbsolutePath());
            startActivity(intent);
        }
    }
}
