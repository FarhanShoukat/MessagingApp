package com.example.whatsapp;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static android.R.attr.phoneNumber;

public class RegisterPhoneNumber extends AppCompatActivity {

    private String[] locales;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_phone_number);

        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            FirebaseDatabase.getInstance().getReference("users").keepSynced(true);
            FirebaseDatabase.getInstance().getReference("inviteLink").keepSynced(true);
        }
        catch (Exception ex) {}

        //FirebaseDatabase.getInstance().getReference("inviteLink").setValue("abc");

        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() != null) {
            startActivity(new Intent(this, ChatActivity.class));
            finish();
        }

        createCountryDropDownMenu();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){}
                else finish();
        }
    }

    private void createCountryDropDownMenu() {
        ArrayList<String> countries= getCountryNames();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, countries);

        Spinner countrySpinner = (Spinner) findViewById(R.id.country);
        countrySpinner.setAdapter(adapter);

        countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String CountryID= locales[position];
                String[] rl = getResources().getStringArray(R.array.CountryCodes);
                for(String x : rl){
                    String[] g = x.split(",");
                    if(g[1].trim().equals(CountryID.trim())){
                        TextView textView = (TextView) findViewById(R.id.countryCode);
                        textView.setText("+" + g[0]);
                        return;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void nextButton(View view) {
        TextView editText1 = (TextView) findViewById(R.id.countryCode);
        EditText editText2 = (EditText) findViewById(R.id.phone);

        Intent intent = new Intent(this, VerifyPhoneNumberActivity.class);
        intent.putExtra("phone", editText1.getText().toString() + editText2.getText().toString());
        startActivity(intent);
    }

    public ArrayList<String> getCountryNames() {

        locales = Locale.getISOCountries();
        ArrayList<String> countries = new ArrayList<>();

        for (String countryCode : locales) {

            Locale obj = new Locale("", countryCode);
            countries.add(obj.getDisplayCountry());
        }
        return countries;
    }

    public static class HelperClass {
        public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
            final int height = options.outHeight;
            final int width = options.outWidth;
            int inSampleSize = 1;

            if (height > reqHeight || width > reqWidth) {
                final int halfHeight = height / 2;
                final int halfWidth = width / 2;

                while ((halfHeight / inSampleSize) >= reqHeight
                        && (halfWidth / inSampleSize) >= reqWidth) {
                    inSampleSize *= 2;
                }
            }
            return inSampleSize;
        }

        public static Bitmap decodeBitmapFromFile(File file, int reqWidth, int reqHeight) {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            try {
                BitmapFactory.decodeFile(file.getAbsolutePath(), options);

                // Calculate inSampleSize
                options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

                // Decode bitmap with inSampleSize set
                options.inJustDecodeBounds = false;
                return BitmapFactory.decodeFile(file.getAbsolutePath(), options);
            }
            catch (Exception ex) {
                return null;
            }
        }
        }
}
