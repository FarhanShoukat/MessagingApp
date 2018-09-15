package com.example.whatsapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class SyncMessagesService extends Service {
    public static String currentlyOpened = "";
    private ArrayList<String> friends = new ArrayList<>();

    public SyncMessagesService() {
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            FirebaseDatabase.getInstance().getReference("users").keepSynced(true);
            FirebaseDatabase.getInstance().getReference("inviteLink").keepSynced(true);
        }
        catch (Exception ex) {}
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        syncFunction();
        return START_STICKY;
    }

    public void syncFunction() {
        final DBHelper helper = new DBHelper(this);
        final SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT Number FROM Friend", null);
        final String user = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("messages");
        while (cursor.moveToNext()) {
            final String friend = cursor.getString(cursor.getColumnIndex("Number"));
            boolean contains = false;
            for(String string : friends) {
                if(string.equals(friend)) {
                    contains = true;
                    break;
                }
            }
            if(!friend.equals(user) && !contains) {
                friends.add(friend);
                DatabaseReference reference = databaseReference.child(user + " " + friend);
                reference.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                        String[] a = dataSnapshot.getKey().split(" ");
                        String sender = a[2];
                        String date = a[0] + " " + a[1];
                        Cursor cursor1 = db.rawQuery("SELECT Friend FROM MyMessage WHERE Sender='" + sender + "' AND Date='" + date + "'", null);

                        if(!cursor1.moveToNext()) {
                            String type = dataSnapshot.child("type").getValue(String.class);

                            DBHelper dbHelper = new DBHelper(SyncMessagesService.this);
                            final SQLiteDatabase database = dbHelper.getWritableDatabase();

                            final ContentValues values = new ContentValues();
                            values.put("Friend", friend);
                            values.put("Sender", sender);
                            values.put("Date", date);
                            values.put("Type", type);
                            if(type.equals("text")) values.put("Message", dataSnapshot.child("message").getValue(String.class));
                            else if(type.equals("image")) values.put("Message", "");


                            if(!user.equals(sender) && type.equals("image")) {
                                StorageReference reference = FirebaseStorage.getInstance().getReference(friend + " " + user).child(friend + " " + date + ".jpg");
                                ContextWrapper wrapper = new ContextWrapper(getApplicationContext());
                                File file = wrapper.getDir(friend,MODE_PRIVATE);
                                file = new File(file, sender + " " + date + ".jpg");
                                if(!file.exists()) {
                                    reference.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                            database.insertWithOnConflict("Message", null, values, SQLiteDatabase.CONFLICT_REPLACE);
                                            database.insertWithOnConflict("MyMessage", null, values, SQLiteDatabase.CONFLICT_REPLACE);
                                            broadcastMessage();
                                            if (!currentlyOpened.equals(friend)) {
                                                NotificationCompat.Builder mBuilder =
                                                        new NotificationCompat.Builder(SyncMessagesService.this)
                                                                .setSmallIcon(R.drawable.icon_round)
                                                                .setContentTitle(friend)
                                                                .setContentText("Photo");
                                                Intent intent = new Intent(SyncMessagesService.this, ChatActivity.class);
                                                PendingIntent pendingIntent = PendingIntent.getActivity(SyncMessagesService.this, 0, intent, 0);
                                                mBuilder.setContentIntent(pendingIntent);
                                                mBuilder.setDefaults(Notification.DEFAULT_SOUND);
                                                NotificationManager mNotificationManager =
                                                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                                mNotificationManager.notify(1, mBuilder.build());
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(SyncMessagesService.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                            else {
                                database.insertWithOnConflict("Message", null, values, SQLiteDatabase.CONFLICT_REPLACE);
                                database.insertWithOnConflict("MyMessage", null, values, SQLiteDatabase.CONFLICT_REPLACE);
                                broadcastMessage();
                                if(!user.equals(sender) && !currentlyOpened.equals(friend)) {
                                    String message = dataSnapshot.child("message").getValue(String.class);
                                    if(message.contains("http://maps.google.com/maps/api/staticmap?center=")) message = "Location";
                                    NotificationCompat.Builder mBuilder =
                                            new NotificationCompat.Builder(SyncMessagesService.this)
                                                    .setSmallIcon(R.drawable.icon_round)
                                                    .setContentTitle(friend)
                                                    .setContentText(message);
                                    Intent intent = new Intent(SyncMessagesService.this, ChatActivity.class);
                                    PendingIntent pendingIntent = PendingIntent.getActivity(SyncMessagesService.this, 0, intent, 0);
                                    mBuilder.setContentIntent(pendingIntent);
                                    mBuilder.setDefaults(Notification.DEFAULT_SOUND);
                                    //mBuilder.setPriority(Notification.PRIORITY_MAX);
                                    NotificationManager mNotificationManager =
                                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                    mNotificationManager.notify(001, mBuilder.build());
                                }
                            }
                        }
                        cursor1.close();
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }
        cursor.close();
    }

    public void broadcastMessage() {
        Intent intent = new Intent("serviceMessage");
        intent.putExtra("message", "");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
