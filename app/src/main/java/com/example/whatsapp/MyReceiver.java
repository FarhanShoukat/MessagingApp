package com.example.whatsapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;

/**
 * Created by farha on 30-Nov-17.
 */

public class MyReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            context.startService(new Intent(context, SyncMessagesService.class));
        }
    }
}
