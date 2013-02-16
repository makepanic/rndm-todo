package de.rndm.todo.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.view.Window;
import android.widget.*;
import de.rndm.todo.R;
import de.rndm.todo.adapter.DashboardListAdapter;
import de.rndm.todo.adapter.DetailContactAdapter;
import de.rndm.todo.model.*;
import de.rndm.todo.model.accessor.ContactsAccessorImpl;
import de.rndm.todo.model.accessor.ITodoCRUDAccessor;
import de.rndm.todo.model.accessor.SqliteAccessorImpl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created with IntelliJ IDEA.
 * User: mkp
 * Date: 29.12.12
 * Time: 12:53
 */
public class ContactActivity extends Activity {
    private Intent i;
    private SqliteAccessorImpl sqlite;
    private Contact contact;
    private DetailContactAdapter contactAdapter;
    private DashboardListAdapter todoAdapter;
    private ArrayList<Contact> contacts = new ArrayList<Contact>();
    private ArrayList<Todo> todos;
    private ITodoCRUDAccessor todoAccessor;
    private LinearLayout openContact;
    private TextView contactName;
    private ImageButton shareThis, sendThis;
    private SimpleDateFormat sdf;

    private ListView contactList, todoList;
    private ContactsAccessorImpl contactAccessor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_contact_detail);

        sdf = new SimpleDateFormat("d.MM.yyy - HH:mm", Locale.GERMAN);

        i = getIntent();

        contactAccessor = new ContactsAccessorImpl(this.getContentResolver(), getResources().getDrawable(R.drawable.ic_contact_picture), getApplicationContext());

        openContact = (LinearLayout) findViewById(R.id.open_contact);
        openContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, contact.getLookupKey());
                intent.setData(uri);
                ContactActivity.this.startActivity(intent);
            }
        });

        contact = (Contact) i.getParcelableExtra(getString(R.string.parcel_contact));
        contacts.add(contact);

        contactAdapter = new DetailContactAdapter(this, R.layout.listview_detail_item_row_dark, contacts);
        contactAdapter.setRemoveable(false);

        contactList = (ListView)findViewById(R.id.list_contacts);
        contactList.setAdapter(contactAdapter);
        contactList.setDividerHeight(0);
        contactList.setCacheColorHint(0);
        contactAdapter.setEnabled(false);

        sqlite = new SqliteAccessorImpl(getApplicationContext());

        todos = new ArrayList<Todo>();
        todoAccessor = new SqliteAccessorImpl(getApplicationContext());

        todoAdapter = new DashboardListAdapter(this, R.layout.listview_todo_item_row, todos, todoAccessor);

        todoList = (ListView)findViewById(R.id.list_todos);
        todoList.setAdapter(todoAdapter);
        todoList.setEnabled(true);
        todoAdapter.setEnabled(true);
        todoList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                Intent i = new Intent(ContactActivity.this, DetailActivity.class);
                i.putExtra(getString(R.string.parcel_readonly), true);
                Todo t = todos.get(arg2);

                i.putExtra(getString(R.string.parcel_todo), t);
                i.putExtra(getString(R.string.parcel_num), arg2);
                startActivityForResult(i, 1);
            }
        });

        shareThis = (ImageButton) findViewById(R.id.button_share_contact);
        sendThis = (ImageButton) findViewById(R.id.button_send_contact);

        if(!contact.hasPhoneNumbers()){
            sendThis.setVisibility(View.GONE);
        }

        sendThis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<String> phoneList = contact.getPhoneNumbers();
                if(phoneList.size() > 0){
                    Uri uri = Uri.parse("smsto:" + phoneList.get(0));
                    Intent it = new Intent(Intent.ACTION_SENDTO, uri);
                    it.putExtra("sms_body", buildMessageBody());
                    startActivity(it);
                }
            }
        });

        shareThis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean plural = todos.size() > 1;
                String messageTitle;
                String shareTitle;

                shareTitle = "Termin" + (plural ? "e": "") + " mit Kontakt teilen";
                messageTitle = "Sie wurden mit " + (plural ? "mehreren Terminen" : "einem Termin") + " verknüpft";

                List<String> emailList = contact.getEmails();
                List<String> phoneList = contact.getPhoneNumbers();

                String[] emails = emailList.toArray(new String[emailList.size()]);

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, messageTitle);
                shareIntent.putExtra(Intent.EXTRA_TEXT, buildMessageBody());
                shareIntent.putExtra(Intent.EXTRA_EMAIL, emails);
                if(phoneList.size() > 0){
                    //shareIntent.setData(Uri.parse("sms:" + phoneList.get(0)));
                    shareIntent.putExtra(Intent.EXTRA_PHONE_NUMBER, phoneList.get(0));
                }
                startActivity(Intent.createChooser(shareIntent, shareTitle));
            }
        });

        contactName = (TextView) findViewById(R.id.label_contact_big);

        this.loadTodos();
    }

    private String buildMessageBody(){
        StringBuilder body = new StringBuilder();

        body.append("Hi,\nJemand hat Sie mit einem Termin verknüpft und möchte, dass Sie davon erfahren.\n\n");
        for(Todo t : todos){
            body.append("Termin:\n");
            body.append("\tTitel: ").append(t.getTitle()).append("\n");
            body.append("\tDatum: ").append(sdf.format(t.getDoneUntil().getTime())).append("\n");
            if(t.getDescription().length() > 0)
                body.append("\tBeschreibung:\n\t").append(t.getDescription()).append("\n");
        }

        return body.toString();
    }

    private void loadTodos(){
        new AsyncTask<Void, Void, ArrayList<Todo>>() {

            @Override
            protected ArrayList<Todo> doInBackground(Void... params) {

                ArrayList<Todo> todos = sqlite.getTodosByContact(contact.getLookupKey());

                for (Todo t : todos) {
                    ArrayList<Contact> contacts = contactAccessor.readAllContactsFromLookupList(t.getContactIds());
                    t.setContacts(contacts);
                }
                return todos;
            }
            @Override
            protected void onPostExecute(ArrayList<Todo> result) {
                if(result.size() > 0){
                    shareThis.setVisibility(View.VISIBLE);
                }else{
                    shareThis.setVisibility(View.INVISIBLE);
                }
                todos.clear();
                todos.addAll(result);
                todos = RememberUtils.sortListByOrder(todos, SortOrder.DATE);
                contactName.setText("Todos " + todos.size());
                todoAdapter.notifyDataSetChanged();
            }
        }.execute();
    }
}
