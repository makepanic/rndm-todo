package de.rndm.todo.comparators;

import android.util.Log;
import de.rndm.todo.model.Todo;

import java.util.Comparator;

public class KreutelWDComparator implements Comparator<Todo> {

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
                int importanceComparision = new ImportanceComparator().compare(lhs, rhs);
                if(importanceComparision == 0){
                    //wichtigkeit prüfen
                    return new DateComparator().compare(lhs, rhs);
                }else{
                    return importanceComparision;
                }

            }else{
                Log.i("compare", "sollte nie eintreffen");
                return 0;
            }
        }
	}
}
