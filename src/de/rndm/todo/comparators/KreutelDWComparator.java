package de.rndm.todo.comparators;

import android.util.Log;
import de.rndm.todo.model.Todo;

import java.util.Comparator;

public class KreutelDWComparator implements Comparator<Todo> {

	@Override
	public int compare(Todo lhs, Todo rhs) {
		/*
        if(lhs.isActive() == rhs.isActive()){
			return compareDate(lhs, rhs);
		}
		else if(lhs.isActive() && !rhs.isActive()){
			return -1;
		}else
			return 1;
	    */
        if(lhs.isActive() && !rhs.isActive()){
            return -1;
        }else if(!lhs.isActive() && rhs.isActive()){
            return 1;
        }else{
            if(lhs.isActive() == rhs.isActive()){
                int dateComparison = new DateComparator().compare(lhs, rhs);
                if(dateComparison == 0){
                    //wichtigkeit prüfen
                    return new ImportanceComparator().compare(lhs, rhs);
                }else{
                    return dateComparison;
                }

            }else{
                Log.i("compare", "sollte nie eintreffen");
                return 0;
            }
        }
	}
}
