package com.example.whatsapp;

import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.whatsapp.DBHelper.DATABASE_NAME;

public class Settings extends AppCompatActivity {
    private File image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        TextView edit =(TextView) findViewById(R.id.textView3);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Settings.this,editaccount.class);
                startActivity(intent);
            }

        });

        TextView delete = (TextView) findViewById(R.id.textView5);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(Settings.this)
                        .setTitle("Delete account?")
                        .setMessage("Do you really want to delete your account?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                deleteAccount();
                            }})
                        .setNegativeButton("No", null).show();
            }
        });

        TextView invite =(TextView) findViewById(R.id.textView4);
        invite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance().getReference("inviteLink").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String link = "";
                        try {
                            link = dataSnapshot.getValue(String.class);
                        } catch (Exception ex) {}

                        Intent intent = new AppInviteInvitation.IntentBuilder("Hey check this app")
                                .setMessage("Hey thats a new messaging app. " + link)
                                .setCallToActionText("Share")
                                .build();
                        startActivityForResult(intent, 2);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        final TextView name =(TextView) findViewById(R.id.textView2);
        final TextView status = (TextView) findViewById(R.id.status);
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                name.setText(dataSnapshot.child("name").getValue(String.class));
                status.setText(dataSnapshot.child("status").getValue(String.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        CircleImageView imageView = (CircleImageView) findViewById(R.id.imageView);
        ContextWrapper wrapper = new ContextWrapper(getApplicationContext());
        File file = wrapper.getDir("profilePictures",MODE_PRIVATE);
        file = new File(file, FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() + ".jpg");
        image = file;
        if(file.exists()) {
            imageView.setImageURI(Uri.parse(file.getAbsolutePath()));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void imageClicked(View view) {
        if(image.exists()) {
            Intent intent = new Intent(this, ShowImage.class);
            intent.putExtra("image", image.getAbsolutePath());
            startActivity(intent);
        }
    }

    public void deleteAccount() {
        String user = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(user);
        databaseReference.child("name").setValue(null);
        databaseReference.child("status").setValue(null);

        databaseReference = FirebaseDatabase.getInstance().getReference("messages");
        DBHelper dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT DISTINCT Friend FROM Message", null);
        while (cursor.moveToNext()) {
            String friend = cursor.getString(cursor.getColumnIndex("Friend"));
            databaseReference.child(user + " " + friend).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot ds : dataSnapshot.getChildren()) {
                        ds.child("type").getRef().setValue(null);
                        ds.child("message").getRef().setValue(null);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        cursor.close();
        FirebaseAuth.getInstance().signOut();
        try {
            deleteDatabase(DATABASE_NAME);
        }
        catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
        ContextWrapper wrapper = new ContextWrapper(getApplicationContext());
        File file = wrapper.getDir("profilePictures",MODE_PRIVATE);
        if (file.isDirectory())
        {
            String[] children = file.list();
            for (String child : children)
            {
                new File(file, child).delete();
            }
        }
        stopService(new Intent(this, SyncMessagesService.class));
        finishAffinity();
    }
}
