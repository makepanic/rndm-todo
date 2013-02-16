package de.rndm.todo.model;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * some class to represent a contact
 */
public class Contact implements Parcelable {

    public static final Parcelable.Creator<Contact> CREATOR = new Parcelable.Creator<Contact>() {
        @Override
        public Contact createFromParcel(Parcel in) {
            return new Contact(in);
        }

        @Override
        public Contact[] newArray(int size) {
            return new Contact[size];
        }
    };
    private long id;
    private String name;
    private Uri thumbnailUri;
    private Bitmap thumbnail;
    private String lookupKey;
    private ArrayList<String> lookupKeys = new ArrayList<String>();
    private ArrayList<String> emails = new ArrayList<String>();
    private ArrayList<String> phoneNumbers = new ArrayList<String>();

    public Contact() {
        this.id = (long) 0;
        this.name = "";
        this.lookupKey = "";
        this.thumbnail = null;
    }

    public Contact(Parcel in) {
        this.id = in.readLong();
        this.name = in.readString();
        this.lookupKey = in.readString();
        this.thumbnail = (Bitmap) in.readParcelable(getClass().getClassLoader());
        this.lookupKeys = RememberUtils.stringToList(in.readString());
        this.emails = RememberUtils.stringToList(in.readString());
        this.phoneNumbers = RememberUtils.stringToList(in.readString());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeString(lookupKey);
        dest.writeParcelable(thumbnail, flags);
        dest.writeString(RememberUtils.listToString(lookupKeys));
        dest.writeString(RememberUtils.listToString(emails));
        dest.writeString(RememberUtils.listToString(phoneNumbers));
    }

    public void addEmailAddress(String email) {
        this.emails.add(email);
    }

    public void addPhoneNumber(String phone) {
        this.phoneNumbers.add(phone);
    }

    public List<String> getEmails() {
        return this.emails;
    }

    public List<String> getPhoneNumbers() {
        return this.phoneNumbers;
    }

    public boolean hasPhoneNumbers() {
        return this.phoneNumbers.size() > 0;
    }

    public long getId() {
        return id;
    }

    public void setId(String id) {
        this.id = Long.valueOf(id);
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLookupKey() {
        return this.lookupKey;
    }

    public void setLookupKey(String id) {
        this.lookupKey = id;
    }

    public List<String> getLookupKeys() {
        return this.lookupKeys;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Bitmap getBitmap() {
        return this.thumbnail;
    }

    public void setBitmap(Bitmap bitmap) {
        this.thumbnail = bitmap;
    }

    public Uri getURI() {
        return this.thumbnailUri;
    }

    public void setURI(Uri uri) {
        this.thumbnailUri = uri;
    }

    public boolean equals(Object other) {
        return other.getClass() == this.getClass() && this.getId() == ((Contact) other).getId();
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("id = ").append(id);
        builder.append("; lookupKey = ").append(lookupKey);
        builder.append("; lookupKeys = ").append(lookupKeys.toString());
        builder.append("; name = ").append(name);
        builder.append("; email = ").append(emails.toString());
        builder.append("; numbers = ").append(phoneNumbers.toString());
        builder.append("; hasBitmap = ").append(thumbnail == null ? "false" : "true");

        return builder.toString();
    }

}
