package de.rndm.todo.comparators;

import de.rndm.todo.model.Todo;

import java.util.Comparator;

/**
 * Created with IntelliJ IDEA.
 * User: momo
 * Date: 03.02.13
 * Time: 00:32
 * To change this template use File | Settings | File Templates.
 */
public class ImportanceComparator implements Comparator<Todo>{
    @Override
    public int compare(Todo lhs, Todo rhs) {
		if(lhs.isSpecial() == rhs.isSpecial()){
            //beide special
            return 0;
        }else{
            //nicht beide special
            if(lhs.isSpecial() && !rhs.isSpecial()){
                return -1;
            }else{
                return 1;
            }
        }
    }
}
