package de.rndm.todo.model.accessor;

import java.util.ArrayList;

import android.net.Uri;
import de.rndm.todo.model.Contact;

public interface IContactsAccessor {

    public ArrayList<Contact> readAllContactsFromLookupList(ArrayList<String> idList);

    public Contact readContact(long contactId);

    public Contact readContact(String lookupKey);

    public Contact readContactFromUri(Uri uri);

}
