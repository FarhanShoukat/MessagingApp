package com.example.whatsapp;

import android.app.ProgressDialog;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetProfileData extends AppCompatActivity {
    private EditText name;
    private CircleImageView imageView;
    private DatabaseReference mDatabase;
    String phone;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_profile_data);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        storageRef = FirebaseStorage.getInstance().getReference();
        Intent intent = getIntent();
        phone = (String) intent.getSerializableExtra("phone");
        name = (EditText) findViewById(R.id.name);
        imageView = (CircleImageView) findViewById(R.id.photo);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallery = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(gallery, 1);
            }
        });
    }

    public void nextButton(View view) {
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        if(drawable == null) {
            Toast.makeText(this, "Must attach a Profile Photo", Toast.LENGTH_SHORT).show();
            return;
        }
        final Bitmap photo = drawable.getBitmap();
        final String nam = name.getText().toString();
        if(nam.length() == 0) {
            Toast.makeText(this, "Must input Name", Toast.LENGTH_SHORT).show();
            return;
        }

        StorageReference reference = storageRef.child("profilePictures/" + phone + ".jpg");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            photo.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            final ProgressDialog dialog = ProgressDialog.show(this, "Please wait", "Uploading Image...", true);
            UploadTask uploadTask = reference.putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(SetProfileData.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    ContextWrapper wrapper = new ContextWrapper(getApplicationContext());
                    File file = wrapper.getDir("profilePictures",MODE_PRIVATE);
                    file = new File(file, FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() + ".jpg");
                    OutputStream stream = null;
                    try {
                        stream = new FileOutputStream(file);
                        photo.compress(Bitmap.CompressFormat.JPEG,100,stream);
                    }
                    catch (Exception ex) {
                        Toast.makeText(SetProfileData.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    finally {
                        try {
                            dialog.dismiss();
                            stream.flush();
                            stream.close();
                        }catch (Exception ex) {}
                    }
                    Map<String, String> map = new HashMap<>();
                    map.put("name", nam);
                    map.put("status", "Available");
                    DatabaseReference reference1 = mDatabase.child("users").child(phone);

                    reference1.child("name").setValue(null);
                    reference1.child("status").setValue(null);
                    mDatabase.child("users").child(phone).setValue(map);
                    startActivity(new Intent(SetProfileData.this, ChatActivity.class));
                    finish();
                }
            });
        }
        catch (Exception ex) {
            Toast.makeText(SetProfileData.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {
            if(requestCode == 1) {
                Uri imageUri = data.getData();
                imageView.setImageURI(imageUri);
            }
        }
    }
}
