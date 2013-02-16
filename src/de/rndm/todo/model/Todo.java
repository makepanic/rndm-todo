package de.rndm.todo.model;

import android.os.Parcel;
import android.os.Parcelable;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

public class Todo implements Parcelable {
    public static final Parcelable.Creator<Todo> CREATOR = new Parcelable.Creator<Todo>() {
        public Todo createFromParcel(Parcel in) {
            return new Todo(in);
        }

        public Todo[] newArray(int size) {
            return new Todo[size];
        }
    };
    private int id;
    private String title;
    private String description;
    private boolean special;
    private boolean active;
    private boolean sync;
    private Calendar createdAt;
    private Calendar doneUntil;
    private ArrayList<String> contactIds;
    private ArrayList<Contact> contacts;

    public Todo(JSONObject json) {
        final String TODO_ID = "id";
        final String TODO_TITLE = "title";
        final String TODO_DESCRIPTION = "description";
        final String TODO_DONE = "doneuntil";
        final String TODO_SPECIAL = "special";
        final String TODO_ACTIVE = "active";
        final String TODO_CONTACTS = "contacts";

        int id;
        try {
            id = json.has(TODO_ID) ? json.getInt(TODO_ID) : -1;
            String title = json.has(TODO_TITLE) ? json.getString(TODO_TITLE) : "";
            String desc = json.has(TODO_DESCRIPTION) ? json.getString(TODO_DESCRIPTION) : "";
            long doneUntil = json.has(TODO_DONE) ? json.getLong(TODO_DONE) : 0;
            boolean special = json.has(TODO_SPECIAL) && (json.getInt(TODO_SPECIAL) != 0);
            boolean active = json.has(TODO_ACTIVE) && (json.getInt(TODO_ACTIVE) != 0);

            String contactString = json.has(TODO_CONTACTS) ? json.getString(TODO_CONTACTS) : "";

            ArrayList<String> contacts = RememberUtils.stringToList(contactString);

            Calendar cDone = Calendar.getInstance();
            Calendar cCreated = Calendar.getInstance();
            cDone.setTimeInMillis(doneUntil);

            this.id = id;
            this.title = title;
            this.special = special;
            this.createdAt = cCreated;
            this.doneUntil = cDone;
            this.description = desc;
            this.active = active;
            this.sync = true;
            this.contactIds = contacts;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Todo(Parcel in) {
        Calendar calCreated = Calendar.getInstance();
        Calendar calDone = Calendar.getInstance();

        id = in.readInt();
        title = in.readString();
        description = in.readString();

        calCreated.setTimeInMillis(in.readLong());
        calDone.setTimeInMillis(in.readLong());

        createdAt = calCreated;
        doneUntil = calDone;
        special = in.readByte() == 1;
        active = in.readByte() == 1;
        sync = in.readByte() == 1;
        contactIds = RememberUtils.stringToList(in.readString());
        contacts = new ArrayList<Contact>();
        in.readTypedList(contacts, Contact.CREATOR);
    }

    public Todo() {
        this(0, "");
    }

    public Todo(int id, String label) {
        this(id, label, false, null, null, "");
    }

    public Todo(int id, String label, boolean special, Calendar createdAt, Calendar doneUntil, String description) {
        super();
        this.id = id;
        this.title = label;
        this.special = special;
        this.contacts = new ArrayList<Contact>();
        this.contactIds = new ArrayList<String>();

        if (createdAt == null) {
            this.createdAt = Calendar.getInstance();
        } else {
            this.createdAt = createdAt;
        }
        if (doneUntil == null) {
            this.doneUntil = Calendar.getInstance();
        } else {
            this.doneUntil = doneUntil;
        }

        this.description = description;
        this.active = true;
        this.sync = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean hasContact(Contact c) {
        for (Contact contact : contacts) {
            if (contact.equals(c)) {
                return true;
            }
        }
        return false;
    }

    public boolean isOldTimeTodo() {
        long now = Calendar.getInstance().getTimeInMillis();
        return this.doneUntil.getTimeInMillis() < now;
    }

    public boolean hasContact() {
        return !(this.contacts == null || this.contacts.size() == 0);
    }

    public void addContactId(String id) {
        this.contactIds.add(id);
    }

    public void addContact(Contact c) {
        this.contacts.add(c);
        this.addContactId(c.getLookupKey());
    }

    public ArrayList<Contact> getContacts() {
        return this.contacts;
    }

    public void setContacts(ArrayList<Contact> c) {
        this.contacts = c;
        ArrayList<String> lookupKeys = new ArrayList<String>();
        for (Contact contact : this.contacts) {
            lookupKeys.add(contact.getLookupKey());
        }
        this.setContactIds(lookupKeys);
    }

    public ArrayList<String> getContactIds() {
        return this.contactIds;
    }

    public void setContactIds(ArrayList<String> contactIds) {
        this.contactIds = contactIds;
    }

    public boolean isSync() {
        return this.sync;
    }

    public void setSync(boolean sync) {
        this.sync = sync;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isSpecial() {
        return special;
    }

    public void setSpecial(boolean special) {
        this.special = special;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Calendar getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Calendar createdAt) {
        this.createdAt = createdAt;
    }

    public Calendar getDoneUntil() {
        return doneUntil;
    }

    public void setDoneUntil(Calendar doneUntil) {
        this.doneUntil = doneUntil;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeLong(createdAt.getTimeInMillis());
        dest.writeLong(doneUntil.getTimeInMillis());
        dest.writeByte((byte) (special ? 1 : 0));
        dest.writeByte((byte) (active ? 1 : 0));
        dest.writeByte((byte) (sync ? 1 : 0));
        dest.writeString(RememberUtils.listToString(contactIds));
        dest.writeTypedList(contacts);
    }

    @SuppressWarnings("CloneDoesntCallSuperClone")
    public Todo clone() {
        Todo clone = new Todo();
        clone.id = this.id;
        clone.active = this.active;
        clone.createdAt = (Calendar) this.createdAt.clone();
        clone.description = this.description;
        clone.doneUntil = (Calendar) this.doneUntil.clone();
        clone.title = this.title;
        clone.special = this.special;
        clone.sync = this.sync;
        clone.contactIds = RememberUtils.cloneStringList(this.contactIds);
        clone.contacts = RememberUtils.cloneContactList(this.contacts);
        return clone;
    }

    public boolean equals(Todo other) {
        boolean equal = false;
        if (this.id == other.id &&
                this.title.equals(other.title) &&
                this.description.equals(other.description) &&
                this.special == other.special &&
                this.active == other.special &&
                this.createdAt.getTimeInMillis() == other.createdAt.getTimeInMillis() &&
                this.doneUntil.getTimeInMillis() == other.doneUntil.getTimeInMillis() &&
                this.contactIds.equals(other.getContactIds())
                ) {
            equal = true;
        }
        return equal;
    }

    public String toString() {
        String result = this.toJSON().toString();
        if (this.contacts != null && this.contacts.size() > 0) {
            result += this.contacts.toString();
        } else {
            result += " hasNoContacts";
        }
        return result;
    }

    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();

        try {
            obj.put("id", this.id);
            obj.put("title", this.title);
            obj.put("description", this.description);
            obj.put("special", this.special);
            obj.put("active", this.active);
            obj.put("doneUntil", this.doneUntil.getTimeInMillis());
            obj.put("contacts", RememberUtils.listToString(this.contactIds));
        } catch (JSONException e) {
            return new JSONObject();
        }
        return obj;
    }
}
