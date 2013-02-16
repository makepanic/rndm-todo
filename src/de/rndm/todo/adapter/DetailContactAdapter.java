package de.rndm.todo.adapter;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import de.rndm.todo.R;
import de.rndm.todo.model.Contact;

import java.util.ArrayList;

public class DetailContactAdapter extends ArrayAdapter<Contact> {

    private Context context;
    private boolean enabled, removeable;
    private ArrayList<Contact> objects = null;
    private int layoutResourceId;

    public static class ViewHolder {
        public ImageView list_img;
        public TextView list_txt;
        public ImageView remove_contact;
    }

    public DetailContactAdapter(Context context, int textViewResourceId, ArrayList<Contact> objects) {
        super(context, textViewResourceId, objects);
        this.objects = objects;
        this.layoutResourceId = textViewResourceId;
        this.context = context;
        this.enabled = true;
    }

    public void setRemoveable(boolean removeable) {
        this.removeable = removeable;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean areAllItemsEnabled() {
        // TODO Auto-generated method stub
        return this.enabled;
    }

    @Override
    public boolean isEnabled(int position) {
        // TODO Auto-generated method stub
        return this.enabled;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.list_img = (ImageView) row.findViewById(R.id.list_img);
            holder.list_txt = (TextView) row.findViewById(R.id.list_txt);
            holder.remove_contact = (ImageView) row.findViewById(R.id.remove_contact);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }
        //final Contact contact = objects.get(position);
        Contact contact = objects.get(position);
        if (contact.getBitmap() == null) {
            Log.i("contact.getBitmap", "isNull");
        }
        holder.list_img.setImageBitmap(contact.getBitmap());
        holder.list_txt.setText(contact.getName() == null ? "undefined" : contact.getName());
        if (removeable) {
            holder.remove_contact.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    objects.remove(objects.get(position));
                    DetailContactAdapter.this.notifyDataSetChanged();
                }
            });
        } else {
            holder.remove_contact.setVisibility(View.GONE);
        }

        return row;
    }

}
