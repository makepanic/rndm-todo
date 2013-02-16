package de.rndm.todo.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.*;
import de.rndm.todo.R;
import de.rndm.todo.model.*;
import de.rndm.todo.model.accessor.ITodoCRUDAccessor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class DashboardListAdapter extends ArrayAdapter<Todo> {

    private Context context;
    private boolean enabled;
    private ArrayList<Todo> objects = null;
    private int layoutResourceId;
    private SimpleDateFormat formatDate = new SimpleDateFormat("dd.MM.yyy", Locale.GERMAN);
    private ITodoCRUDAccessor accessor;
    private ViewGroup parent;

    public static class ViewHolder {
        public ImageView list_img;
        public ImageView list_contact;
        public FrameLayout list_contact_holder;
        public TextView list_contact_count;
        public TextView list_txt;
        public TextView list_remaining;
        public TextView list_date;
        public LinearLayout list_container;
    }

    public DashboardListAdapter(Context context, int textViewResourceId, ArrayList<Todo> objects, ITodoCRUDAccessor accessor) {
        super(context, textViewResourceId, objects);
        this.objects = objects;
        this.accessor = accessor;
        this.layoutResourceId = textViewResourceId;
        this.context = context;
        this.enabled = true;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return this.enabled;
    }

    @Override
    public boolean isEnabled(int position) {
        return this.enabled;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder;
        if (this.parent == null) this.parent = parent;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.list_img = (ImageView) row.findViewById(R.id.list_img);
            holder.list_txt = (TextView) row.findViewById(R.id.list_txt);
            holder.list_remaining = (TextView) row.findViewById(R.id.list_remaining);
            holder.list_date = (TextView) row.findViewById(R.id.list_date);
            holder.list_contact = (ImageView) row.findViewById(R.id.image_contact);
            holder.list_contact_holder = (FrameLayout) row.findViewById(R.id.list_contact_holder);
            holder.list_contact_count = (TextView) row.findViewById(R.id.label_contact_count);
            holder.list_container = (LinearLayout) row.findViewById(R.id.list_item);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }
        final Todo todo = objects.get(position);
        holder.list_img.setImageResource(RememberUtils.getStateResource(todo));
        holder.list_txt.setText(todo.getTitle());
        holder.list_date.setText(formatDate.format(todo.getDoneUntil().getTime()));

        holder.list_img.setOnLongClickListener(new OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                if (enabled && !todo.isOldTimeTodo()) {
                    todo.setSpecial(!todo.isSpecial());
                    todo.setSync(false);
                    updateTodo(todo);
                    ImageView iv = (ImageView) v.findViewById(R.id.list_img);
                    iv.setImageResource(RememberUtils.getStateResource(todo));
                }
                return true;
            }
        });
        holder.list_img.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (enabled && !todo.isOldTimeTodo()) {
                    todo.setActive(!todo.isActive());
                    todo.setSync(false);
                    updateTodo(todo);
                    ImageView iv = (ImageView) v.findViewById(R.id.list_img);
                    iv.setImageResource(RememberUtils.getStateResource(todo));
                    DashboardListAdapter.this.notifyDataSetChanged();
                }
            }
        });
        if (todo.hasContact()) {
            ArrayList<Contact> contacts = todo.getContacts();
            holder.list_contact_holder.setVisibility(View.VISIBLE);
            holder.list_contact.setImageBitmap(contacts.get(0).getBitmap());
            if (contacts.size() > 1) {
                holder.list_contact_count.setVisibility(View.VISIBLE);
                holder.list_contact_count.setText("" + (contacts.size()));
            } else {
                holder.list_contact_count.setVisibility(View.INVISIBLE);
            }
        } else {
            holder.list_contact_holder.setVisibility(View.INVISIBLE);
        }
        Long now = Calendar.getInstance().getTimeInMillis();

        int colorId = RememberUtils.getResourceIdFromRemaining(todo.getDoneUntil().getTimeInMillis() - now);
        if (colorId == 0) {
            row.setBackgroundColor(Color.TRANSPARENT);
        } else {
            row.setBackgroundResource(colorId);
        }
        if (!todo.isActive() || todo.isOldTimeTodo()) {
            try {
                row.setAlpha(.5f);
            } catch (NoSuchMethodError e) {
                row.getBackground().setAlpha(128);
            }
        } else {
            try {
                row.setAlpha(1);
            } catch (NoSuchMethodError e) {
                row.getBackground().setAlpha(255);
            }
        }

        long remaining = todo.getDoneUntil().getTimeInMillis();


        holder.list_remaining.setText(RememberUtils.makeRemainingLabel(remaining));

        return row;
    }

    private void updateTodo(Todo todo) {
        new AsyncTask<Todo, Void, Todo>() {

            @Override
            protected void onPreExecute() {
            }

            @Override
            protected Todo doInBackground(Todo... todo) {
                return accessor.updateTodo(todo[0]);
            }

            @Override
            protected void onPostExecute(Todo result) {
            }

        }.execute(todo);
    }

}
