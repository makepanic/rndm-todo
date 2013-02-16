package de.rndm.todo.model.accessor;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import de.rndm.todo.model.Contact;
import de.rndm.todo.model.RememberUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class ContactsAccessorImpl implements IContactsAccessor {

    private final Bitmap avatarBitmap;
    private ContentResolver resolver;
    private Drawable avatar;
    private Context context;

    public ContactsAccessorImpl(ContentResolver resolver, Drawable defaultAvatar, Context context) {
        this.resolver = resolver;
        this.avatar = defaultAvatar;
        this.avatarBitmap = RememberUtils.drawableToBitmap(avatar);
        this.context = context;
    }

    @Override
    public ArrayList<Contact> readAllContactsFromLookupList(ArrayList<String> idList) {

        ArrayList<Contact> contacts = new ArrayList<Contact>();
        for (String anIdList : idList) {
            contacts.add(this.readContact(anIdList));
        }
        return contacts;
    }

    @Override
    public Contact readContact(long contactId) {

        Contact contact = new Contact();

        Uri requestUri = Uri.withAppendedPath(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, String.valueOf(28));

        Cursor cursor = resolver.query(requestUri, null, null, null, null);

        if (cursor.moveToFirst()) {
            String name = cursor.getString(
                    cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME)
            );
            contact.setName(name);
            Uri person = ContentUris.withAppendedId(
                    ContactsContract.Contacts.CONTENT_URI, contactId);

            contact.setURI(Uri.withAppendedPath(person, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY));
        }

        cursor.close();
        return contact;
    }

    private ArrayList<String> lookupPhoneNumbers(String lookupKey) {

        ArrayList<String> phoneNumbers = new ArrayList<String>();

        Cursor cursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY + " = ?", new String[]{lookupKey}, null);
        try {
            while (cursor.moveToNext()) {
                String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                phoneNumbers.add(phoneNumber);
            }
        } finally {
            cursor.close();
        }

        return phoneNumbers;
    }

    @Override
    public Contact readContact(String lookupKey) {

        Contact contact = new Contact();
        contact.setLookupKey(lookupKey);
        Uri lookupUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
        Uri res = ContactsContract.Contacts.lookupContact(resolver, lookupUri);

        Cursor cursor = resolver.query(res, null, null, null, null);

        if (cursor.moveToFirst()) {
            String name = cursor.getString(
                    cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME)
            );
            contact.setName(name);
            long contactId = Long.valueOf(
                    cursor.getString(cursor
                            .getColumnIndex(ContactsContract.Contacts._ID))
            );
            contact.setId(contactId);

            ArrayList<String> numbers = lookupPhoneNumbers(contact.getLookupKey());
            if (numbers.size() > 0) {
                for (String number : numbers) {
                    contact.addPhoneNumber(number);
                }
            }

            Cursor emails = resolver.query(
                    ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = "
                            + contact.getId(), null, null);

            if (emails.moveToFirst()) {
                while (emails.moveToNext()) {
                    String emailAddress = emails
                            .getString(emails
                                    .getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA1));

                    contact.addEmailAddress(emailAddress);
                }
            }
            emails.close();

            Uri person = ContentUris.withAppendedId(
                    ContactsContract.Contacts.CONTENT_URI, contactId);

            contact.setURI(Uri.withAppendedPath(person, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY));

            Bitmap bitmap = null;
            try {
                bitmap = RememberUtils.scaleDownBitmap(MediaStore.Images.Media.getBitmap(resolver, contact.getURI()), 32, context);
            } catch (FileNotFoundException e) {
                bitmap = avatarBitmap;
            } catch (IOException e) {
                e.printStackTrace();
            }
            contact.setBitmap(bitmap);
        }

        cursor.close();
        return contact;
    }

    @Override
    public Contact readContactFromUri(Uri uri) {
        Contact contact = new Contact();
        Cursor cursor = resolver.query(uri, null, null, null, null);
        cursor.moveToFirst();

        // set the contact name
        contact.setName(cursor.getString(cursor
                .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));

        contact.setLookupKey(cursor.getString(cursor
                .getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY))
        );
        contact.setId(
                cursor.getString(cursor
                        .getColumnIndex(ContactsContract.Contacts._ID))
        );

        Uri person = ContentUris.withAppendedId(
                ContactsContract.Contacts.CONTENT_URI, contact.getId());

        contact.setURI(Uri.withAppendedPath(person, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY));

        Bitmap bitmap = null;
        try {
            bitmap = RememberUtils.scaleDownBitmap(MediaStore.Images.Media.getBitmap(resolver, contact.getURI()), 32, context);
        } catch (FileNotFoundException e) {
            bitmap = avatarBitmap;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        contact.setBitmap(bitmap);

        cursor.close();
        return contact;
    }
}
