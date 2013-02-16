package de.rndm.todo.model;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created with IntelliJ IDEA.
 * User: mkp
 * Date: 23.12.12
 * Time: 14:04
 */
public class AppPreferences {
    private static final String APP_SHARED_PREFS = "de.rndm.rememberme_prefs"; //  Name of the file -.xml
    private SharedPreferences appSharedPrefs;
    private SharedPreferences.Editor prefsEditor;

    public AppPreferences(Context context)
    {
        this.appSharedPrefs = context.getSharedPreferences(APP_SHARED_PREFS, Activity.MODE_PRIVATE);
        this.prefsEditor = appSharedPrefs.edit();
    }

    public String get(String key, String defValue){
        String value = "";

        if(key != null && !key.equals("")){
            value = appSharedPrefs.getString(key, "");
        }

        return value;

    }
    public String get(String key){
        return get(key, "");
    }

    public void set(String key, String value){
        if(key != null && value != null && !key.equals("") && !value.equals("")){
            prefsEditor.putString(key, value);
            prefsEditor.commit();
        }
    }
}