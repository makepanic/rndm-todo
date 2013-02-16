package de.rndm.todo.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import de.rndm.todo.R;
import de.rndm.todo.comparators.DateComparator;
import de.rndm.todo.comparators.KreutelDWComparator;
import de.rndm.todo.comparators.KreutelWDComparator;
import de.rndm.todo.comparators.SpecialComparator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

public class RememberUtils {
    public static int getStateResource(Todo todo) {
        int resource;
        if (todo.isActive()) {
            if (todo.isSpecial()) {
                resource = R.drawable.dude_special;
            } else {
                resource = R.drawable.dude;
            }
        } else {
            if (todo.isSpecial()) {
                resource = R.drawable.dude_disabled_special;
            } else {
                resource = R.drawable.dude_disabled;
            }
        }
        return resource;
    }

    public static String makeRemainingLabel(long checkTime) {
        String label, prefix;
        long now = Calendar.getInstance().getTimeInMillis();
        long diff = checkTime - now;
        diff = (long) Math.floor(diff / 1000); //in Sekunden umwandeln
        long diffAbs = Math.abs(diff);

        int toMin = 60;
        int toHour = toMin * 60;
        int toDay = toHour * 24;
        int toMonth = toDay * 30;
        int toYear = toMonth * 12;
        int diffMod;

        if (diffAbs / toYear > 0) {
            //In Jahren ausgeben
            diffMod = (int) (diff / toYear);
            label = Math.abs(diffMod) + " Jahr" + (Math.abs(diffMod) > 1 ? "en" : "");
        } else if (diffAbs / toMonth > 0) {
            //In Monaten ausgeben
            diffMod = (int) (diff / toMonth);
            label = Math.abs(diffMod) + " Monat" + (Math.abs(diffMod) > 1 ? "en" : "");
        } else if (diffAbs / toDay > 0) {
            //in Tagen ausgeben
            diffMod = (int) (diff / toDay);
            label = Math.abs(diffMod) + " Tag" + (Math.abs(diffMod) > 1 ? "en" : "");
        } else if (diffAbs / toHour > 0) {
            //in Stunden ausgeben
            diffMod = (int) (diff / toHour);
            label = Math.abs(diffMod) + " Stunde" + (Math.abs(diffMod) > 1 ? "n" : "");
        } else if (diffAbs / toMin > 0) {
            //in Minuten ausgeben
            diffMod = (int) (diff / toMin);
            label = Math.abs(diffMod) + " Minute" + (Math.abs(diffMod) > 1 ? "n" : "");
        } else {
            //in Sekunden ausgeben
            diffMod = (int) (diff);
            label = Math.abs(diffMod) + " Sekunde" + (Math.abs(diffMod) > 1 ? "n" : "");
        }
        prefix = diffMod < 0 ? "vor " : "in ";
        label = prefix + label;

        return label;
    }

    public static ArrayList<Todo> sortListByOrder(ArrayList<Todo> todos, SortOrder order) {
        switch (order) {
            case DATE:
                Collections.sort(todos, new DateComparator());
                break;
            case SPECIAL:
                Collections.sort(todos, new SpecialComparator());
                break;
            case KREUTELDW:
                Collections.sort(todos, new KreutelDWComparator());
                break;
            case KREUTELWD:
                Collections.sort(todos, new KreutelWDComparator());
                break;
        }
        return todos;
    }

    public static int getTodoIndexInArrayList(Todo todo, ArrayList<Todo> todos) {
        int index;
        for (index = 0; index < todos.size(); index++) {
            if (todo.getId() == todos.get(index).getId()) {
                break;
            }
        }
        return index;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public static Bitmap scaleDownBitmap(Bitmap photo, int newHeight, Context context) {

        final float densityMultiplier = context.getResources().getDisplayMetrics().density;

        int h = (int) (newHeight * densityMultiplier);
        int w = (int) (h * photo.getWidth() / ((double) photo.getHeight()));

        photo = Bitmap.createScaledBitmap(photo, w, h, true);

        return photo;
    }

    public static String listToString(ArrayList<String> input) {
        return RememberUtils.listToString(input, ",");
    }

    public static String listToString(ArrayList<String> input, String delimiter) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < input.size(); i++) {
            result.append(input.get(i));
            if (i != input.size() - 1) {
                result.append(delimiter);
            }
        }
        return result.toString();
    }

    public static ArrayList<String> stringToList(String input) {
        return RememberUtils.stringToList(input, ",");
    }

    public static ArrayList<String> stringToList(String input, String delimiter) {
        ArrayList<String> result = new ArrayList<String>();
        if (!input.equals("")) {
            String[] inputSplit = input.split(delimiter);
            for (String anInputSplit : inputSplit) {
                result.add(String.valueOf(anInputSplit));
            }
        }
        return result;
    }

    public static ArrayList<String> cloneStringList(ArrayList<String> LongList) {
        ArrayList<String> clonedList = new ArrayList<String>(LongList.size());
        for (String l : LongList) {
            clonedList.add(l);
        }
        return clonedList;
    }

    public static ArrayList<Contact> cloneContactList(ArrayList<Contact> LongList) {
        ArrayList<Contact> clonedList = new ArrayList<Contact>(LongList.size());
        for (Contact l : LongList) {
            clonedList.add(l);
        }
        return clonedList;
    }

    public static int getResourceIdFromRemaining(long remaining) {
        remaining /= 1000; //dont want milliseconds
        if (remaining < 0) {
            return 0;
        }
        if (remaining < 60) {
            //Sekunden
            return R.drawable.selector_todo_second;
        }
        if (remaining < 60 * 60) {
            //Minuten
            return R.drawable.selector_todo_minute;
        }
        if (remaining < 60 * 60 * 24) {
            //Stunden
            return R.drawable.selector_todo_hour;
        }
        if (remaining < 60 * 60 * 24 * 30) {
            //Monat ca
            return R.drawable.selector_todo_day;
        }
        if (remaining < 60 * 60 * 24 * 30 * 12) {
            //Jahr ca
            return R.drawable.selector_todo_month;
        }
        return R.drawable.selector_todo_year;
    }
}
