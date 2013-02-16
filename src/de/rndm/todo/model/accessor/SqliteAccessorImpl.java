package de.rndm.todo.model.accessor;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import de.rndm.todo.model.RememberUtils;
import de.rndm.todo.model.Todo;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created with IntelliJ IDEA.
 * User: mkp
 * Date: 29.12.12
 * Time: 12:12
 * @29c3
 */
public class SqliteAccessorImpl extends SQLiteOpenHelper implements ITodoCRUDAccessor{

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "rememberMe";
    private static final String TABLE_TODOS= "todos";

    private static final String KEY_ID = "id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_DESC = "description";
    private static final String KEY_SPECIAL = "special";
    private static final String KEY_ACTIVE = "active";
    private static final String KEY_CONTACTS = "contacts";
    private static final String KEY_CREATEDAT = "created_at";
    private static final String KEY_DONEUNTIL = "done_until";

    public SqliteAccessorImpl(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TODOS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_TODOS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_TITLE + " VARCHAR,"
                + KEY_DESC + " TEXT,"
                + KEY_SPECIAL + " INTEGER," + KEY_ACTIVE + " INTEGER,"
                + KEY_CREATEDAT + " LONG," + KEY_DONEUNTIL + " LONG,"
                + KEY_CONTACTS + " VARCHAR"
                + ")";
        db.execSQL(CREATE_TODOS_TABLE);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TODOS);
        onCreate(db);
    }

    public void resetTodos(){

        String dropSql = "DROP TABLE IF EXISTS " + TABLE_TODOS;
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(dropSql);
        onCreate(db);
    }

    public ArrayList<Todo> getTodosByContact(String lookupKey){
        ArrayList<Todo> todos = new ArrayList<Todo>();

        String selectQuery = "SELECT * FROM " + TABLE_TODOS + " WHERE " + KEY_CONTACTS + " LIKE ? ";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{
                "%" + lookupKey + "%"
        });
        if(cursor.moveToFirst()){
            do {
                todos.add(this.getTodo(cursor.getInt(0)));
            } while (cursor.moveToNext());
        }
        return todos;
    }
    public Todo getTodo(long id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TODOS, new String[]{
                KEY_ID, KEY_TITLE, KEY_DESC, KEY_SPECIAL, KEY_ACTIVE,
                KEY_CREATEDAT, KEY_DONEUNTIL, KEY_CONTACTS
        }, KEY_ID + " = ?", new String[]{
                String.valueOf(id)
        }, null, null, null);

        if(cursor != null && cursor.moveToFirst()){
            Calendar cCreated = Calendar.getInstance();
            Calendar cDone = (Calendar) cCreated.clone();
            cCreated.setTimeInMillis(cursor.getLong(5));
            cDone.setTimeInMillis(cursor.getLong(6));

            Todo todo = new Todo();
            todo.setId(cursor.getInt(0));
            todo.setTitle(cursor.getString(1));
            todo.setActive(cursor.getInt(4) != 0);
            todo.setSpecial(cursor.getInt(3) != 0);
            todo.setDescription(cursor.getString(2));
            todo.setCreatedAt(cCreated);
            todo.setDoneUntil(cDone);
            todo.setContactIds(RememberUtils.stringToList(cursor.getString(7)));
            return todo;
        }
        return new Todo();
    }

    @Override
    public ArrayList<Todo> readAllTodos() {

        ArrayList<Todo> todoList = new ArrayList<Todo>();

        String selectQuery = "SELECT * FROM " + TABLE_TODOS;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if(cursor.moveToFirst()){

            do {

                Todo todo = new Todo();
                Calendar cCreated = Calendar.getInstance();
                Calendar cDone = (Calendar) cCreated.clone();
                cCreated.setTimeInMillis(cursor.getLong(5));
                cDone.setTimeInMillis(cursor.getLong(6));
                todo.setId(cursor.getInt(0));
                todo.setTitle(cursor.getString(1));
                todo.setDescription(cursor.getString(2));
                todo.setActive(cursor.getInt(4) != 0);
                todo.setSpecial(cursor.getInt(3) != 0);
                todo.setCreatedAt(cCreated);
                todo.setDoneUntil(cDone);
                todo.setContactIds(RememberUtils.stringToList(cursor.getString(7)));
                todoList.add(todo);
            } while (cursor.moveToNext());
        }

        return todoList;
    }

    @Override
    public Todo getLastTodo() {

        Todo lastTodo = new Todo();
        String selectQuery = "SELECT * FROM " + TABLE_TODOS;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if(cursor.moveToLast()){

            Calendar cCreated = Calendar.getInstance();
            Calendar cDone = (Calendar) cCreated.clone();
            cCreated.setTimeInMillis(cursor.getLong(5));
            cDone.setTimeInMillis(cursor.getLong(6));
            lastTodo.setId(cursor.getInt(0));
            lastTodo.setTitle(cursor.getString(1));
            lastTodo.setDescription(cursor.getString(2));
            lastTodo.setActive(cursor.getInt(4) != 0);
            lastTodo.setSpecial(cursor.getInt(3) != 0);
            lastTodo.setCreatedAt(cCreated);
            lastTodo.setDoneUntil(cDone);
            lastTodo.setContactIds(RememberUtils.stringToList(cursor.getString(7)));
        }else{
            lastTodo.setId(-1);
        }

        return lastTodo;
    }

    @Override
    public Todo createTodo(Todo todo) {
        SQLiteDatabase db = this.getReadableDatabase();

        ContentValues values = new ContentValues();
        //values.put(KEY_ID, null);
        values.put(KEY_TITLE, todo.getTitle().equals("") ? "Neues Todo" : todo.getTitle());
        values.put(KEY_DESC, todo.getDescription());
        values.put(KEY_SPECIAL, todo.isSpecial());
        values.put(KEY_ACTIVE, todo.isActive());
        values.put(KEY_CREATEDAT, todo.getCreatedAt().getTimeInMillis());
        values.put(KEY_DONEUNTIL, todo.getDoneUntil().getTimeInMillis());
        values.put(KEY_CONTACTS, RememberUtils.listToString(todo.getContactIds()));
        db.insert(TABLE_TODOS, null, values);
        db.close();

        return this.getLastTodo();
    }

    @Override
    public boolean deleteTodo(long todoId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TODOS, KEY_ID + " = ?",
                new String[]{
                        "" + todoId
                });
        db.close();
        return true;
    }

    @Override
    public Todo updateTodo(Todo todo) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ID, todo.getId());
        values.put(KEY_TITLE, todo.getTitle());
        values.put(KEY_DESC, todo.getDescription());
        values.put(KEY_SPECIAL, todo.isSpecial());
        values.put(KEY_ACTIVE, todo.isActive());
        values.put(KEY_CREATEDAT, todo.getCreatedAt().getTimeInMillis());
        values.put(KEY_DONEUNTIL, todo.getDoneUntil().getTimeInMillis());
        values.put(KEY_CONTACTS, RememberUtils.listToString(todo.getContactIds()));
        int rowsAffected = db.update(TABLE_TODOS, values, KEY_ID + " = ?",
                new String[]{
                        String.valueOf(todo.getId())
                });
        if(rowsAffected == 1){
            return todo;
        }
        return null;
    }

    @Override
    public void updateTodos(ArrayList<Todo> todos) {
        if(todos != null && todos.size() > 0){
            for(Todo t : todos){
                this.updateTodo(t);
            }
        }
    }


}
