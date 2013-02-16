package de.rndm.todo.comparators;

import java.util.Comparator;

import de.rndm.todo.model.Todo;

public class SpecialComparator implements Comparator<Todo>{

	@Override
	public int compare(Todo lhs, Todo rhs) {
		if(lhs.isActive() == rhs.isActive()){
			//beide gleich aktiv
			return compareSpecial(lhs, rhs);
		}
		else if(lhs.isActive() && !rhs.isActive()){
			return -1;
		}else
			return 1;
	}
	private int compareSpecial(Todo lhs, Todo rhs){
		if(lhs.isSpecial() && !rhs.isSpecial()){
			return -1;
		}else if(!lhs.isSpecial() && rhs.isSpecial()){
			return 1;
		}
		return 0;
	}

}
