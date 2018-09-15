package com.example.whatsapp;

import android.*;
import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageActivity extends AppCompatActivity {

    private ListView messageList;
    private LinearLayout attachmentGrid;
    private EditText messageTextView;
    public static final int CAMERA = 2;
    public static final int GALLERY = 3;

    private String phone;
    private String name;

    private DatabaseReference reference1;
    private DatabaseReference reference2;
    private StorageReference storageRef;

    private MessageAdapter messageAdapter;

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            intent.getSerializableExtra("message");
            showMessages();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Intent intent = getIntent();
        name = (String) intent.getSerializableExtra("name");
        phone = (String)intent.getSerializableExtra("phone");
        if(name == null) {
            name = phone;
        }
        CircleImageView imageView = (CircleImageView) findViewById(R.id.imageView);
        ContextWrapper wrapper = new ContextWrapper(getApplicationContext());
        File file = wrapper.getDir("profilePictures",MODE_PRIVATE);
        file = new File(file, phone + ".jpg");
        if(file.exists()) {
            ((CircleImageView) findViewById(R.id.friend_image)).setImageURI(Uri.parse(file.getAbsolutePath()));
        }

        setToolbar();

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("messages");
        reference1 = mDatabase.child(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() + " " + phone);
        reference2 = mDatabase.child(phone + " " + FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
        storageRef = FirebaseStorage.getInstance().getReference(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() + " " + phone);

        messageList = (ListView) findViewById(R.id.message_list);
        showMessages();

        attachmentGrid = (LinearLayout) findViewById(R.id.attachment_grid);

        messageTextView = (EditText) findViewById(R.id.message_text);
        messageTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attachmentGrid.setVisibility(View.GONE);
            }
        });

        EditText editText = (EditText) findViewById(R.id.search_bar);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                messageAdapter.getFilter().filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        SyncMessagesService.currentlyOpened = phone;
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("serviceMessage"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        SyncMessagesService.currentlyOpened = "";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SyncMessagesService.currentlyOpened = "";
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.message_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.message_clearChat) {
            DBHelper helper = new DBHelper(this);
            SQLiteDatabase db = helper.getWritableDatabase();
            db.execSQL("DELETE FROM Message WHERE Friend='" + phone + "'");
            db.execSQL("DELETE FROM MyMessage WHERE Friend='" + phone + "'");
            reference1.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot ds : dataSnapshot.getChildren()) {
                        ds.child("type").getRef().setValue(null);
                        ds.child("message").getRef().setValue(null);
                    }
                    showMessages();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            ContextWrapper wrapper = new ContextWrapper(getApplicationContext());
            File file = wrapper.getDir(phone,MODE_PRIVATE);
            if (file.isDirectory())
            {
                String[] children = file.list();
                for (String child : children)
                {
                    new File(file, child).delete();
                }
            }
        }
        else if(id == R.id.message_search) {
            findViewById(R.id.tool_bar_parent).setBackground(getResources().getDrawable(R.color.white));
            findViewById(R.id.tool_bar_child).setVisibility(View.GONE);
            findViewById(R.id.search_bar).setVisibility(View.VISIBLE);
        }
        return super.onOptionsItemSelected(item);
    }

    private void resetSearch() {
        findViewById(R.id.tool_bar_parent).setBackground(getResources().getDrawable(R.color.teal));
        findViewById(R.id.tool_bar_child).setVisibility(View.VISIBLE);
        EditText editText = (EditText) findViewById(R.id.search_bar);
        editText.setVisibility(View.GONE);
        editText.setText("");
        messageAdapter.getFilter().filter(null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm.getActiveNetworkInfo() != null) {
                if (requestCode == CAMERA) {
                    sendImage((Bitmap) data.getExtras().get("data"));
                }
                else if (requestCode == GALLERY) {
                    Uri imageUri = data.getData();
                    ImageView imageView = (ImageView) findViewById(R.id.image_view);
                    imageView.setImageURI(imageUri);
                    Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
                    sendImage(bitmap);
                }
            }
            else {
                Toast.makeText(this, "No connection available", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void sendImage(@NonNull Bitmap bitmap) {
        ContextWrapper wrapper = new ContextWrapper(getApplicationContext());
        File file = wrapper.getDir(phone,MODE_PRIVATE);
        Date date = new Date();
        final String n = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date) + " " + FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
        final String n1 = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() + " " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
                File file1 = new File(file, n1 + ".jpg");

        try {
            OutputStream stream = null;
            stream = new FileOutputStream(file1);
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream);
            stream.flush();
            stream.close();

            StorageReference reference = storageRef.child(n1 + ".jpg");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();
            UploadTask uploadTask = reference.putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(MessageActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Map<String, String> map = new HashMap<>();
                    map.put("type", "image");
                    reference1.child(n).setValue(map);
                    reference2.child(n).setValue(map);
                }
            });
        }
        catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();

        }
    }





    public void showMessages() {
        reference1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Message> messages = new ArrayList<>();
                ContextWrapper wrapper = new ContextWrapper(getApplicationContext());
                File file = wrapper.getDir(phone,MODE_PRIVATE);
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    String[] key = ds.getKey().split(" ");
                    String sender = key[2];
                    String date = key[0] + " " + key[1];
                    String type = ds.child("type").getValue(String.class);
                    if(type.equals("text")) {
                        String message = ds.child("message").getValue(String.class);
                        TextMessage textMessage = new TextMessage(sender, message);
                        textMessage.setDate(date);
                        messages.add(textMessage);
                        System.out.println(sender + " " + date + " " + message);
                    }
                    else if(type.equals("image")) {
                        File file1 = new File(file, sender + " " + date + ".jpg");
                        ImageMessage imageMessage = null;
                        if(file1.exists()) {
                            imageMessage = new ImageMessage(sender, Uri.parse(file1.getAbsolutePath()));
                            imageMessage.setDate(date);
                            messages.add(imageMessage);
                        }
                    }
                }
                MessageAdapter adapter = new MessageAdapter(MessageActivity.this, messages);
                messageAdapter = adapter;
                messageList.setAdapter(adapter);
                messageList.setSelection(messages.size() - 1);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        LinearLayout layout = (LinearLayout) findViewById(R.id.tool_bar_child);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MessageActivity.this, UserDetailsScreen.class);
                intent.putExtra("phone", phone);
                intent.putExtra("name", name);
                startActivity(intent);
            }
        });

        TextView textView = (TextView) findViewById(R.id.name);
        textView.setText(name);

        ImageButton button = (ImageButton) findViewById(R.id.back_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessageActivity.this.onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(findViewById(R.id.search_bar).getVisibility() != View.GONE)
            resetSearch();
        else
            super.onBackPressed();
    }

    //onclick listeners

    public void sendButton(View view) {
        attachmentGrid.setVisibility(View.GONE);
        String message = messageTextView.getText().toString();
        if(!message.equals("")) {
            Map<String, String> map = new HashMap<>();
            map.put("type", "text");
            map.put("message", message);

            String n = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " " + FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
            reference1.child(n).setValue(map);
            reference2.child(n).setValue(map);

            messageTextView.setText("");
        }
    }

    public void attachButton(View view) {
        if(attachmentGrid.getVisibility() == View.VISIBLE) {
            attachmentGrid.setVisibility(View.GONE);
        }
        else {
            attachmentGrid.setVisibility(View.VISIBLE);
        }
    }

    public void cameraButton(View view) {
        attachmentGrid.setVisibility(View.GONE);
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA);
    }

    public void galleryButton(View view) {
        attachmentGrid.setVisibility(View.GONE);
        Intent gallery = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, GALLERY);
    }

    public void locationButton(View view) {
        attachmentGrid.setVisibility(View.GONE);
        //Toast.makeText(this, "Location", Toast.LENGTH_SHORT).show();
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    1);
        }
        else shareLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) shareLocation();
                else Toast.makeText(this, "You need to give permission to share location", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareLocation() {
        //Toast.makeText(this, "shared", Toast.LENGTH_SHORT).show();
        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.

                                // Logic to handle location object
                            try {
                                String lat = Double.toString(location.getLatitude());
                                String lng = Double.toString(location.getLongitude());
                                String url = "http://maps.google.com/maps/api/staticmap?center=" + lat + "," + lng + "&zoom=15&size=200x200&sensor=false";
                                Map<String, String> map = new HashMap<>();
                                map.put("type", "text");
                                map.put("message", url);

                                String n = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " " + FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
                                reference1.child(n).setValue(map);
                                reference2.child(n).setValue(map);
                            }
                            catch (Exception ex) {
                                Toast.makeText(MessageActivity.this, "Failed to share location. Please check if Google Play Services can access your location", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
        catch (SecurityException ex) {}
    }
}