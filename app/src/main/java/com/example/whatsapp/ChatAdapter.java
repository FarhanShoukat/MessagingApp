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
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by farha on 07-Nov-17.
 */

public class ChatAdapter extends ArrayAdapter<PersonCell> implements Filterable{
    private ArrayList<PersonCell> items;
    private ArrayList<PersonCell> filteredItems;
    private LayoutInflater inflater;
    private Filter filter;

    public ChatAdapter(Context context, ArrayList<PersonCell> items) {
        super(context, 0, items);
        this.items = items;
        filteredItems = items;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Nullable
    @Override
    public PersonCell getItem(int position) {
        return filteredItems.get(position);
    }

    @Override
    public int getCount() {
        return filteredItems.size();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final PersonCell cell = getItem(position);
        ChatActivity.ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.person_cell, null);
            holder = new ChatActivity.ViewHolder();
            holder.photoImageView = (CircleImageView) convertView.findViewById(R.id.photo);
            holder.layout = (LinearLayout) convertView.findViewById(R.id.layout);
            holder.nameTextView = (TextView) convertView.findViewById(R.id.person);
            holder.lastMessageTextView = (TextView) convertView.findViewById(R.id.lastmessage);
            holder.dateTextView = (TextView) convertView.findViewById(R.id.date);
            convertView.setTag(holder);
        }
        else {
            holder = (ChatActivity.ViewHolder) convertView.getTag();
        }

        if(cell.getPhoto() == null) {
            holder.photoImageView.setImageResource(R.drawable.account);
        }
        else {
            //holder.photoImageView.setImageURI(cell.getPhoto());
            Bitmap bitmap = RegisterPhoneNumber.HelperClass.decodeBitmapFromFile(new File(cell.getPhoto().toString()), 50, 50);
            holder.photoImageView.setImageBitmap(bitmap);
        }
        holder.nameTextView.setText(cell.getName());
        holder.lastMessageTextView.setText(cell.getLastMessage());
        holder.dateTextView.setText(cell.getDate());

        holder.photoImageView.setTag(cell);
        holder.layout.setTag(cell);
        holder.dateTextView.setTag(cell);

        holder.photoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PersonCell personCell = (PersonCell) v.getTag();
                Intent intent = new Intent(getContext(), UserDetailsScreen.class);
                intent.putExtra("name", personCell.getName());
                intent.putExtra("phone", personCell.getPhone());
                getContext().startActivity(intent);
            }
        });

        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PersonCell personCell = (PersonCell) v.getTag();
                Intent intent = new Intent(getContext(), MessageActivity.class);
                intent.putExtra("name", personCell.getName());
                intent.putExtra("phone", personCell.getPhone());
                getContext().startActivity(intent);
            }
        });

        holder.dateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PersonCell personCell = (PersonCell) v.getTag();
                Intent intent = new Intent(getContext(), MessageActivity.class);
                intent.putExtra("name", personCell.getName());
                intent.putExtra("phone", personCell.getPhone());
                getContext().startActivity(intent);
            }
        });

        return convertView;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        if(filter == null) {
            filter = new PersonFilter();
        }
        return filter;
    }

    private class PersonFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            if(constraint != null && constraint.length() > 0) {
                ArrayList<PersonCell> filteredList = new ArrayList<>();
                for(PersonCell cell : items) {
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
            filteredItems = (ArrayList<PersonCell>) results.values;
            notifyDataSetChanged();
        }
    }
}
