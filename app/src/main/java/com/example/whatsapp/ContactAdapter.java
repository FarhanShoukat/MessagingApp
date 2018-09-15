package com.example.whatsapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by farha on 09-Nov-17.
 */

public class ContactAdapter extends ArrayAdapter<ContactCell> {
    private ArrayList<ContactCell> items;
    private ArrayList<ContactCell> filteredItems;
    private LayoutInflater inflater;
    private Filter filter;

    public ContactAdapter(Context context, ArrayList<ContactCell> items) {
        super(context, 0, items);
        this.items = items;
        this.filteredItems = items;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Nullable
    @Override
    public ContactCell getItem(int position) {
        return filteredItems.get(position);
    }

    @Override
    public int getCount() {
        return filteredItems.size();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ContactCell cell = getItem(position);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.contact_cell, null);
        }

        CircleImageView photoImageView = (CircleImageView) convertView.findViewById(R.id.photo);
        LinearLayout layout = (LinearLayout) convertView.findViewById(R.id.layout);
        TextView nameTextView = (TextView) convertView.findViewById(R.id.person);
        TextView statusTextView = (TextView) convertView.findViewById(R.id.status);

        if(cell.getPhoto() == null) {
            photoImageView.setImageResource(R.drawable.account);
        } else {
            //photoImageView.setImageURI(cell.getPhoto());
            Bitmap bitmap = RegisterPhoneNumber.HelperClass.decodeBitmapFromFile(new File(cell.getPhoto().toString()), 100, 100);
            photoImageView.setImageBitmap(bitmap);
        }
        nameTextView.setText(cell.getName());
        statusTextView.setText(cell.getStatus());

        layout.setTag(cell);

        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContactCell contactCell = (ContactCell) v.getTag();
                Intent intent = new Intent(getContext(), UserDetailsScreen.class);
                intent.putExtra("name", contactCell.getName());
                intent.putExtra("phone", contactCell.getPhone());
                getContext().startActivity(intent);
            }
        });

        return convertView;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        if(filter == null) {
            filter = new ContactFilter();
        }
        return filter;
    }

    private class ContactFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            if(constraint != null && constraint.length() > 0) {
                ArrayList<ContactCell> filteredList = new ArrayList<>();
                for(ContactCell cell : items) {
                    if(cell.getName().toLowerCase().contains(constraint.toString().toLowerCase())) {
                        filteredList.add(cell);
                    }
                }
                results.count = filteredList.size();
                results.values = filteredList;
            }
            else {
                results.count = items.size();
                results.values = items;
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredItems = (ArrayList<ContactCell>) results.values;
            notifyDataSetChanged();
        }
    }
}