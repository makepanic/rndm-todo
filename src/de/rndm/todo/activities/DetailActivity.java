package de.rndm.todo.activities;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import de.rndm.todo.R;
import de.rndm.todo.adapter.DetailContactAdapter;
import de.rndm.todo.model.Contact;
import de.rndm.todo.custom.ExpandableHeightGridView;
import de.rndm.todo.model.Todo;
import de.rndm.todo.model.accessor.ContactsAccessorImpl;
import de.rndm.todo.model.accessor.IContactsAccessor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class DetailActivity extends Activity {

    static final int DATE_DIALOG_ID = 1;
    static final int TIME_DIALOG_ID = 2;
    static final int CONTACT_DIALOG_ID = 3;
    private int listnum;
    private Intent i;
    private TextView description;
    private EditText headline;
    private CheckBox isDone;
    private Button buttonSubmit, buttonDateDialog, buttonTimeDialog, buttonContact;
    private ImageButton buttonReset, buttonDelete;
    private Todo oldTodo, newTodo;
    private SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm", Locale.GERMAN);
    private SimpleDateFormat formatDate = new SimpleDateFormat("d.MM.yyy", Locale.GERMAN);
    private Toast toast;
    private DetailContactAdapter adapter;
    private ArrayList<Contact> contacts = new ArrayList<Contact>();
    private ExpandableHeightGridView contactList;
    private IContactsAccessor accessor;
    private boolean readonly = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_detail);

        i = getIntent();
        readonly = i.getBooleanExtra(getString(R.string.parcel_readonly), false);

        adapter = new DetailContactAdapter(this, R.layout.listview_detail_item_row, contacts);
        adapter.setRemoveable(true);


        oldTodo = (Todo) i.getParcelableExtra(getString(R.string.parcel_todo));

        newTodo = oldTodo.clone();

        accessor = new ContactsAccessorImpl(this.getContentResolver(), getResources().getDrawable(R.drawable.ic_contact_picture), getApplicationContext());

        listnum = i.getIntExtra(getString(R.string.parcel_num), 0);

        contactList = (ExpandableHeightGridView) findViewById(R.id.list_contacts);
        contactList.setExpanded(true);
        contactList.setAdapter(adapter);
        contactList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Contact contact = contacts.get(position);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, contact.getLookupKey());
                intent.setData(uri);
                DetailActivity.this.startActivity(intent);

                /*Log.i("onItemClick", "startActivity");
                Intent i = new Intent(DetailActivity.this, ContactActivity.class);
                //i.putExtra(getApplicationContext().getString(R.string.parcel_todo), todos.get(arg2));
                i.putExtra(getString(R.string.parcel_contact), contacts.get(position));
                i.putExtra("rndm.contact.detail", "wasd");
                startActivity(i);    */
            }
        });

        description = (TextView) findViewById(R.id.detail_description);

        headline = (EditText) findViewById(R.id.detail_item_edit);

        buttonSubmit = (Button) findViewById(R.id.detail_submit);
        buttonDateDialog = (Button) findViewById(R.id.detail_date_button);
        buttonTimeDialog = (Button) findViewById(R.id.detail_time_button);
        buttonContact = (Button) findViewById(R.id.button_add_contact);

        isDone = (CheckBox) findViewById(R.id.detail_active);

        buttonReset = (ImageButton) findViewById(R.id.detail_revert);
        buttonDelete = (ImageButton) findViewById(R.id.detail_delete);

        populateFields(newTodo);

        buttonContact.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                        Contacts.CONTENT_URI);
                startActivityForResult(contactPickerIntent, CONTACT_DIALOG_ID);

            }
        });
        buttonReset.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                toast = Toast.makeText(getApplicationContext(), R.string.success_todo_revert, Toast.LENGTH_LONG);
                toast.show();
                newTodo = oldTodo.clone();
                populateFields(oldTodo);
                //finish();
                //startActivity(getIntent());
            }
        });
        buttonDelete.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = new Intent(DetailActivity.this, DashboardActivity.class);
                i.putExtra(getString(R.string.parcel_todo), newTodo);
                i.putExtra(getString(R.string.parcel_update), false);
                i.putExtra(getString(R.string.parcel_delete), true);
                setResult(RESULT_OK, i);
                finish();
            }
        });
        buttonSubmit.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (readonly) {
                    finish();
                }
                newTodo.setTitle(headline.getText().toString());
                newTodo.setDescription(description.getText().toString());
                newTodo.setActive(!isDone.isChecked());
                newTodo.setContacts(contacts);

                if (newTodo.equals(oldTodo)) {
                    newTodo.setSync(true);
                } else {
                    newTodo.setSync(false);
                }

                Intent i = new Intent(DetailActivity.this, DashboardActivity.class);
                i.putExtra(getString(R.string.parcel_todo), newTodo);
                i.putExtra(getString(R.string.parcel_num), listnum);
                i.putExtra(getString(R.string.parcel_update), true);
                i.putExtra(getString(R.string.parcel_delete), false);
                setResult(RESULT_OK, i);
                finish();
            }
        });

        buttonTimeDialog.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                showDialog(TIME_DIALOG_ID);
            }
        });
        buttonDateDialog.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DATE_DIALOG_ID);
            }
        });

        if (readonly) {
            //adapter.setEnabled(false);
            //contactList.setEnabled(false);
            contactList.setEnabled(false);
            adapter.setRemoveable(false);
            description.setEnabled(false);
            isDone.setClickable(false);
            headline.setKeyListener(null);
            buttonDateDialog.setClickable(false);
            buttonTimeDialog.setClickable(false);
            buttonContact.setVisibility(View.GONE);
            buttonDelete.setVisibility(View.GONE);
            buttonReset.setVisibility(View.GONE);
            buttonSubmit.setText(getString(R.string.button_back));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_detail, menu);
        return true;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CONTACT_DIALOG_ID:
                    // handle contact results
                    Uri result = data.getData();
                    Contact c = accessor.readContactFromUri(result);
                    if (!newTodo.hasContact(c)) {
                        newTodo.addContact(c);
                        this.loadContacts();
                    }
                    break;
            }
        } else {
            // gracefully handle failure
            Log.w("result", "Warning: activity result not ok");
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID:
                return new DatePickerDialog(this,
                        new OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                                  int dayOfMonth) {
                                Calendar c = newTodo.getDoneUntil();
                                c.set(Calendar.YEAR, year);
                                c.set(Calendar.MONTH, monthOfYear);
                                c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                buttonDateDialog.setText(formatDate.format(c.getTime()));
                                newTodo.setDoneUntil(c);
                            }
                        },
                        newTodo.getDoneUntil().get(Calendar.YEAR),
                        newTodo.getDoneUntil().get(Calendar.MONTH),
                        newTodo.getDoneUntil().get(Calendar.DAY_OF_MONTH));
            case TIME_DIALOG_ID:
                return new TimePickerDialog(this,
                        new OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                Calendar c = newTodo.getDoneUntil();
                                c.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                c.set(Calendar.MINUTE, minute);
                                buttonTimeDialog.setText(formatTime.format(c.getTime()));
                                newTodo.setDoneUntil(c);
                            }
                        },
                        newTodo.getDoneUntil().get(Calendar.HOUR_OF_DAY),
                        newTodo.getDoneUntil().get(Calendar.MINUTE),
                        true);

        }
        return null;
    }

    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case DATE_DIALOG_ID:
                ((DatePickerDialog) dialog).updateDate(
                        newTodo.getDoneUntil().get(Calendar.YEAR),
                        newTodo.getDoneUntil().get(Calendar.MONTH),
                        newTodo.getDoneUntil().get(Calendar.DAY_OF_MONTH)
                );
                break;
            case TIME_DIALOG_ID:
                ((TimePickerDialog) dialog).updateTime(
                        newTodo.getDoneUntil().get(Calendar.HOUR_OF_DAY),
                        newTodo.getDoneUntil().get(Calendar.MINUTE)
                );
                break;
        }
    }

    private void populateFields(Todo todo) {
        isDone.setChecked(!todo.isActive());
        headline.setText(todo.getTitle());
        description.setText(todo.getDescription());

        Calendar doneUntil = todo.getDoneUntil();
        buttonDateDialog.setText(formatDate.format(doneUntil.getTime()));
        buttonTimeDialog.setText(formatTime.format(doneUntil.getTime()));
        loadContacts();
    }

    private void loadContacts() {
        contacts.clear();
        contacts.addAll(newTodo.getContacts());
        adapter.notifyDataSetChanged();
    }
}
