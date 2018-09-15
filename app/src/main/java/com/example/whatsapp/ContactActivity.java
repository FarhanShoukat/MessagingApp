package com.example.whatsapp;

import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;

public class ContactActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        setToolbar();

        ListView listView = (ListView) findViewById(R.id.list);
        DBHelper helper = new DBHelper(this);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Friend", null);
        ArrayList<ContactCell> cells = new ArrayList<>();
        ContextWrapper wrapper = new ContextWrapper(getApplicationContext());
        File file = wrapper.getDir("profilePictures",MODE_PRIVATE);
        int i;
        while (cursor.moveToNext()) {
            String number = cursor.getString(cursor.getColumnIndex("Number"));
            if((i = ChatActivity.phones.indexOf(number)) != -1) {
                File file1 = new File(file, number + ".jpg");
                String status = cursor.getString(cursor.getColumnIndex("Status"));
                String name = ChatActivity.names.get(i);
                if(file1.exists())
                    cells.add(new ContactCell(Uri.parse(file1.getAbsolutePath()), name, status, number));
                else
                    cells.add(new ContactCell(null, name, status, number));
            }
        }
        final ContactAdapter2 contactAdapter = new ContactAdapter2(this, cells);
        listView.setAdapter(contactAdapter);

        EditText text = (EditText) findViewById(R.id.search);
        text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                contactAdapter.getFilter().filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void setToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }
}
