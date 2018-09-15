package com.example.whatsapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import static com.example.whatsapp.MessageActivity.GALLERY;


/**
 * Created by sharjeel on 10/28/2017.
 */

public class editaccount extends AppCompatActivity{
    DatabaseReference reference;
    private static ImageView imageView;

    private String name = "";
    private String status = "";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editaccount);
        imageView = new ImageView(this);
        reference = FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());

        TextView updateName =(TextView) findViewById(R.id.updateName);
        updateName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialog("Name");
            }
        });

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                name = dataSnapshot.child("name").getValue(String.class);
                status = dataSnapshot.child("status").getValue(String.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        TextView updatePicture =(TextView) findViewById(R.id.updatePic);
        updatePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                if (cm.getActiveNetworkInfo() != null) {
                    showChangePicDialog();
                }
                else {
                    Toast.makeText(editaccount.this, "No connection available", Toast.LENGTH_LONG).show();
                }
            }
        });

        TextView updateStatus =(TextView) findViewById(R.id.updateStatus);
        updateStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialog("Status");
            }
        });
    }

    public void showAlertDialog(final String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        final EditText input = new EditText(this);
        input.setHint("New " + title);
        if(title.equals("Name")) input.setText(name);
        else input.setText(status);
        builder.setView(input);
        builder.setPositiveButton("Set", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(title.equals("Name")) {
                    changeName(input.getText().toString());
                }
                else {
                    changeStatus(input.getText().toString());
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    public void changeName(String title) {
        reference.child("name").setValue(null);
        reference.child("name").setValue(title);
        Toast.makeText(this, "Name changed", Toast.LENGTH_SHORT).show();
    }

    public void changeStatus(String status) {
        reference.child("status").setValue(null);
        reference.child("status").setValue(status);
        Toast.makeText(this, "Status changed", Toast.LENGTH_SHORT).show();
    }

    public void showChangePicDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Profile picture");

        ContextWrapper wrapper = new ContextWrapper(getApplicationContext());
        File file = wrapper.getDir("profilePictures",MODE_PRIVATE);
        file = new File(file, FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() + ".jpg");

        if(file.exists()) imageView.setImageURI(Uri.parse(file.getAbsolutePath()));
        else imageView.setImageResource(R.drawable.account);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallery = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(gallery, GALLERY);
            }
        });

        builder.setView(imageView);
        builder.setPositiveButton("Set", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                changeProfilePic();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    public void changeProfilePic() {
        final Bitmap photo = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
        StorageReference reference = FirebaseStorage.getInstance().getReference("profilePictures").child(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() + ".jpg");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            photo.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            final ProgressDialog dialog = ProgressDialog.show(this, "Please wait", "Uploading Image...", true);
            UploadTask uploadTask = reference.putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(editaccount.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(editaccount.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    finally {
                        try {
                            dialog.dismiss();
                            Toast.makeText(editaccount.this, "Profile picture changed", Toast.LENGTH_SHORT).show();
                            stream.flush();
                            stream.close();
                        }catch (Exception ex) {}
                    }
                }
            });
        }
        catch (Exception ex) {
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == GALLERY) {
                Uri imageUri = data.getData();
                imageView.setImageURI(imageUri);
            }
        }
    }
}


