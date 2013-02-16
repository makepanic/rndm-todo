package de.rndm.todo.model.accessor;

import java.util.ArrayList;

import de.rndm.todo.model.Todo;


public interface ITodoCRUDAccessor {
	public ArrayList<Todo> readAllTodos();

    public Todo getLastTodo();
	
	public Todo createTodo(Todo todo);

	public boolean deleteTodo(long todoId);

	public Todo updateTodo(Todo todo);

    public void updateTodos(ArrayList<Todo> todos);
}