package com.example.whatsapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by farha on 16-Nov-17.
 */

public class MessageAdapter extends ArrayAdapter<Message> implements Filterable {
    private Context context;
    private ArrayList<Message> messages;
    private ArrayList<Message> filteredMessages;
    private Filter filter;

    public MessageAdapter(Context context, ArrayList<Message> messages) {
        super(context, 0, messages);
        this.context = context;
        this.messages = messages;
        this.filteredMessages = messages;
    }

    @Nullable
    @Override
    public Message getItem(int position) {
        return filteredMessages.get(position);
    }

    @Override
    public int getCount() {
        return filteredMessages.size();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Message message = getItem(position);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if(message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message;
            if(textMessage.getSender().equals(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())) {
                convertView = inflater.inflate(R.layout.sent_text, null);
                TextView sentText = (TextView) convertView.findViewById(R.id.sent_text);
                sentText.setText(textMessage.getMessage());
                if(textMessage.getMessage().contains("http://maps.google.com/maps/api/staticmap?center=")) {
                    sentText.setMovementMethod(LinkMovementMethod.getInstance());
                }
            }
            else {
                convertView = inflater.inflate(R.layout.received_text, null);
                TextView receivedText = (TextView) convertView.findViewById(R.id.received_text);
                receivedText.setText(textMessage.getMessage());
                if(textMessage.getMessage().contains("http://maps.google.com/maps/api/staticmap?center=")) {
                    receivedText.setMovementMethod(LinkMovementMethod.getInstance());
                }
            }
        }
        else {
            ImageMessage imageMessage = (ImageMessage) message;
            if(imageMessage.getSender().equals(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())) {
                convertView = inflater.inflate(R.layout.sent_image, null);
                ImageView sentImage = (ImageView) convertView.findViewById(R.id.sent_image);
                try {
                    //sentImage.setImageURI(imageMessage.getImageUri());
                    sentImage.setTag(imageMessage.getImageUri().toString());
                    Bitmap bitmap = RegisterPhoneNumber.HelperClass.decodeBitmapFromFile(new File(imageMessage.getImageUri().toString()), 150, 150);
                    sentImage.setImageBitmap(bitmap);
                }
                catch (Exception ex) {}
                sentImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String tag = (String) v.getTag();
                        if(tag != null && new File(tag).exists()) {
                            Intent intent = new Intent(getContext(), ShowImage.class);
                            intent.putExtra("image", tag);
                            getContext().startActivity(intent);
                        }
                    }
                });
            }
            else {
                convertView = inflater.inflate(R.layout.received_image, null);
                ImageView receivedImage = (ImageView) convertView.findViewById(R.id.received_image);
                try {
                    //receivedImage.setImageURI(imageMessage.getImageUri());
                    receivedImage.setTag(imageMessage.getImageUri().toString());
                    Bitmap bitmap = RegisterPhoneNumber.HelperClass.decodeBitmapFromFile(new File(imageMessage.getImageUri().toString()), 150, 150);
                    receivedImage.setImageBitmap(bitmap);
                }
                catch (Exception ex) {}
                receivedImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String tag = (String) v.getTag();
                        if( tag != null && new File(tag).exists()) {
                            Intent intent = new Intent(getContext(), ShowImage.class);
                            intent.putExtra("image", tag);
                            getContext().startActivity(intent);
                        }
                    }
                });
            }
        }

        return convertView;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        if(filter == null) {
            filter = new MessageFilter();
        }
        return filter;
    }

    private class MessageFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            if(constraint != null && constraint.length() > 0) {
                ArrayList<Message> filteredList = new ArrayList<>();
                for(Message message : messages) {
                    if(message instanceof TextMessage && ((TextMessage) message).getMessage().toLowerCase().contains(constraint.toString().toLowerCase())) {
                        if(!((TextMessage) message).getMessage().contains("http://maps.google.com/maps/api/staticmap?center="))
                            filteredList.add(message);
                    }
                }
                results.count = filteredList.size();
                results.values = filteredList;
            }
            else {
                results.count = messages.size();
                results.values = messages;
            }
            return  results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredMessages = (ArrayList<Message>) results.values;
            notifyDataSetChanged();
        }
    }
}
