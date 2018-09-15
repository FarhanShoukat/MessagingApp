package com.example.whatsapp;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private static ChatAdapter chatAdapter;
    private static ContactAdapter contactAdapter;
    public static ArrayList<String> names = new ArrayList<>();
    public static ArrayList<String> phones = new ArrayList<>();
    private static Context applicationContext;
    private static StorageReference storageRef;

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        applicationContext = getApplicationContext();
        loadContactsFromPhone();
        storageRef = FirebaseStorage.getInstance().getReference().child("profilePictures");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        EditText text = (EditText) findViewById(R.id.search);
        text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(tabLayout.getSelectedTabPosition() == 0) {
                    chatAdapter.getFilter().filter(s.toString());
                }
                else {
                    contactAdapter.getFilter().filter(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    @Override
    public void onBackPressed() {
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.search_bar);
        if(linearLayout.getVisibility() == View.GONE)
            finishAffinity();
        else
            resetSearch(linearLayout);
    }

    public void backPressed(View view) {
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.search_bar);
        resetSearch(linearLayout);
    }

    public void resetSearch(LinearLayout linearLayout) {
        linearLayout.setVisibility(View.GONE);
        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        EditText text = (EditText) findViewById(R.id.search);
        TabLayout tabs = (TabLayout) findViewById(R.id.tabs);
        tabs.setVisibility(View.VISIBLE);
        text.setText("");
        if(tabLayout.getSelectedTabPosition() == 0) {
            chatAdapter.getFilter().filter("");
        }
        else {
            contactAdapter.getFilter().filter("");
        }
    }

    private void loadContactsFromPhone() {
        ContentResolver cr = getContentResolver();
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        while (cursor.moveToNext()) {
            String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            //
            //  Get all phone numbers.
            //
            Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);

            while (phones.moveToNext()) {
                String number = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).replaceAll("\\s+","");
                String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                if(!ChatActivity.phones.contains(number)) {
                    ChatActivity.names.add(name);
                    ChatActivity.phones.add(number);
                }
            }
            phones.close();
        }
        cursor.close();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.home_settings) {
            startActivity(new Intent(this, Settings.class));
            return true;
        }
        else if(id == R.id.home_search) {
            LinearLayout linearLayout = (LinearLayout) findViewById(R.id.search_bar);
            linearLayout.setVisibility(View.VISIBLE);
            TabLayout tabs = (TabLayout) findViewById(R.id.tabs);
            tabs.setVisibility(View.GONE);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static class PlaceholderFragment extends Fragment {
        private static View view1;
        private static View view2;
        private static final String ARG_SECTION_NUMBER = "section_number";

        private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                intent.getSerializableExtra("message");
                createFirstList();
            }
        };

        public PlaceholderFragment() {
        }

        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onResume() {
            super.onResume();
            createFirstList();
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(mMessageReceiver, new IntentFilter("serviceMessage"));
        }

        @Override
        public void onPause() {
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mMessageReceiver);
            super.onPause();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            if(getArguments().getInt(ARG_SECTION_NUMBER) == 1) {
                view1 = inflater.inflate(R.layout.fragment_chat, container, false);
                FloatingActionButton fab = (FloatingActionButton) view1.findViewById(R.id.fab2);
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(), ContactActivity.class);
                        startActivity(intent);
                    }
                });
                createFirstList();
                return view1;
            }
            else {
                view2 = inflater.inflate(R.layout.fragment_contacts, container, false);
                createSecondList();
                syncContactsWithFirebase();
                FloatingActionButton fab = (FloatingActionButton) view2.findViewById(R.id.fab);
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI);
                        startActivity(intent);
                        /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();*/
                    }
                });
                return view2;
            }
        }

        private void createFirstList() {
            ListView listView = (ListView) view1.findViewById(R.id.list);
            ArrayList<PersonCell> cells = new ArrayList<>();
            ContextWrapper wrapper = new ContextWrapper(applicationContext);
            File file = wrapper.getDir("profilePictures",MODE_PRIVATE);
            DBHelper helper = new DBHelper(getContext());
            SQLiteDatabase db = helper.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM Message ORDER BY Date DESC", null);
            int i;
            while (cursor.moveToNext()) {
                String friend = cursor.getString(cursor.getColumnIndex("Friend"));
                String name = "";
                if((i = phones.indexOf(friend)) != -1) {
                    name = names.get(i);
                }
                else name = friend;
                String date = cursor.getString(cursor.getColumnIndex("Date"));
                String cDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                if(cDate.equals(date.split(" ")[0])) {
                    String[] d = date.split(" ")[1].split(":");
                    date = d[0] + ":" + d[1];
                }
                else {
                    String[] d = date.split(" ")[0].split("-");
                    date = d[2] + "/" + d[1] + "/" + d[0];
                }
                String message = cursor.getString(cursor.getColumnIndex("Message"));
                if(message.equals("")) message = "Photo";
                else if(message.contains("http://maps.google.com/maps/api/staticmap?center=")) message = "Location";
                File file1 = new File(file, friend + ".jpg");
                if(file1.exists()) {
                    cells.add(new PersonCell(Uri.parse(file1.getAbsolutePath()), name, message, date, friend));
                }
                else {
                    cells.add(new PersonCell(null, name, message, date, friend));
                }
            }
            chatAdapter = new ChatAdapter(getContext(), cells);
            listView.setAdapter(chatAdapter);
        }

        private void createSecondList() {
            ListView listView = (ListView) view2.findViewById(R.id.list);
            DBHelper helper = new DBHelper(getContext());
            SQLiteDatabase db = helper.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM Friend", null);
            ArrayList<ContactCell> cells = new ArrayList<>();
            ContextWrapper wrapper = new ContextWrapper(applicationContext);
            File file = wrapper.getDir("profilePictures",MODE_PRIVATE);
            int i;
            while (cursor.moveToNext()) {
                String number = cursor.getString(cursor.getColumnIndex("Number"));
                if((i = phones.indexOf(number)) != -1) {
                    File file1 = new File(file, number + ".jpg");
                    String status = cursor.getString(cursor.getColumnIndex("Status"));
                    String name = names.get(i);
                    if(file1.exists())
                        cells.add(new ContactCell(Uri.parse(file1.getAbsolutePath()), name, status, number));
                    else
                        cells.add(new ContactCell(null, name, status, number));
                }
            }
            contactAdapter = new ContactAdapter(getContext(), cells);
            listView.setAdapter(contactAdapter);
        }

        private void syncContactsWithFirebase() {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("users");
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    DBHelper dbHelper = new DBHelper(getContext());
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    db.execSQL("DELETE FROM Friend");
                    ContextWrapper wrapper = new ContextWrapper(applicationContext);
                    File file = wrapper.getDir("profilePictures",MODE_PRIVATE);
                    for(DataSnapshot ds : dataSnapshot.getChildren()) {
                        String number = ds.getKey();
                        String status = ds.child("status").getValue(String.class);

                        ContentValues values = new ContentValues();
                        values.put("Number", number);
                        values.put("Status", status);
                        db.insertWithOnConflict("Friend", null, values, SQLiteDatabase.CONFLICT_REPLACE);

                        if(!number.equals(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()) && phones.contains(number)) {
                            final StorageReference reference = storageRef.child(number + ".jpg");
                            final File file1 = new File(file, number + ".jpg");
                            if(file1.exists()) {
                                final long localSize = file1.length();
                                reference.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                                    @Override
                                    public void onSuccess(StorageMetadata storageMetadata) {
                                        if(localSize != storageMetadata.getSizeBytes()) {
                                            reference.getFile(file1).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                                @Override
                                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                            else {
                                reference.getFile(file1).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    }
                    createSecondList();
                    getContext().startService(new Intent(getContext(), SyncMessagesService.class));
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }
    }

    static class ViewHolder {
        CircleImageView photoImageView;
        LinearLayout layout;
        TextView nameTextView;
        TextView lastMessageTextView;
        TextView dateTextView;
    }
}
