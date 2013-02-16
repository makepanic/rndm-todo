package de.rndm.todo.comparators;

import android.util.Log;
import de.rndm.todo.model.Todo;

import java.util.Calendar;
import java.util.Comparator;

public class DateComparator implements Comparator<Todo> {

    @Override
    public int compare(Todo lhs, Todo rhs) {
        long now = Calendar.getInstance().getTimeInMillis();
        long lhsTime = lhs.getDoneUntil().getTimeInMillis() - now;
        long rhsTime = rhs.getDoneUntil().getTimeInMillis() - now;

        //time < 0 = vergangenes event

        //check ob beides gleich
        if (lhsTime == rhsTime)
            return 0;

        //schauen ob abgelaufen oder nicht
        if (lhsTime < 0 && rhsTime > 0)
            return 1;
        else if (lhsTime > 0 && rhsTime < 0)
            return -1;

        //beide in der Vergangenheit
        if (lhsTime < 0 && rhsTime < 0) {
            //lhs ist weiter zurück
            if (lhsTime < rhsTime)
                return 1;
            else
                return -1;
        }

        if (lhsTime > 0 && rhsTime > 0) {
            if (lhsTime < rhsTime) {
                //lhs näher am jetzt
                return -1;
            } else {
                return 1;
            }
        }

        Log.i("comparator", "sollte nicht hier sein");
        return -1;
    }
}
