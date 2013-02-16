package de.rndm.todo.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import de.rndm.todo.R;
import de.rndm.todo.adapter.DashboardListAdapter;
import de.rndm.todo.custom.FadeoutAnimation;
import de.rndm.todo.model.*;
import de.rndm.todo.model.accessor.ContactsAccessorImpl;
import de.rndm.todo.model.accessor.SqliteAccessorImpl;

import java.util.ArrayList;
import java.util.Date;

public class DashboardActivity extends Activity {

    private final String logKey = "DashboardActivity";
    private final int TIME_REFRESH_RATE = 1000 * 60;
    private ArrayList<Todo> todos = new ArrayList<Todo>();
    private ListView listview;
    private DashboardListAdapter adapter;
    private SortOrder order = SortOrder.DATE;
    private ImageView newButton;
    private ProgressBar loadIndicator;
    private TextView todoCount;
    private Handler guiHandler;
    private SqliteAccessorImpl todoAccessor;
    private ContactsAccessorImpl contactAccessor;
    private Date adapterUpdated = new Date();

    private FadeoutAnimation fadeoutAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_dashboard);

        guiHandler = new Handler();
        todoAccessor = new SqliteAccessorImpl(getApplicationContext());
        contactAccessor = new ContactsAccessorImpl(this.getContentResolver(), getResources().getDrawable(R.drawable.ic_contact_picture), getApplicationContext());

        adapter = new DashboardListAdapter(this, R.layout.listview_todo_item_row, todos, todoAccessor);

        listview = (ListView) findViewById(R.id.list_todos);
        listview.setDividerHeight(0);
        listview.setDrawingCacheEnabled(true);
        listview.setAlwaysDrawnWithCacheEnabled(true);
        listview.setPersistentDrawingCache(ViewGroup.PERSISTENT_SCROLLING_CACHE);

        todoCount = (TextView) findViewById(R.id.label_count_todos);
        newButton = (ImageView) findViewById(R.id.button_new);
        loadIndicator = (ProgressBar) findViewById(R.id.button_reload);
        fadeoutAnimation = new FadeoutAnimation(getApplicationContext(), R.anim.fade_out, guiHandler, loadIndicator);

        newButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                new AsyncTask<Todo, Void, Todo>() {
                    @Override
                    protected Todo doInBackground(Todo... todo) {
                        return todoAccessor.createTodo(todo[0]);
                    }

                    @Override
                    protected void onPostExecute(Todo todo) {
                        if (todo != null) {
                            Intent i = new Intent(DashboardActivity.this, DetailActivity.class);
                            todo.setActive(true);
                            i.putExtra(getString(R.string.parcel_todo), todo);
                            i.putExtra(getString(R.string.parcel_num), todos.size() - 1);
                            startActivityForResult(i, 1);

                            todos.add(todo);
                            updateTodoCount();
                            adapter.notifyDataSetChanged();
                        }
                    }
                }.execute(new Todo());
            }
        });

        listview.setAdapter(adapter);
        listview.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                checkUpdate();
                return false;
            }
        });

        listview.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {

                Intent i = new Intent(DashboardActivity.this, DetailActivity.class);
                i.putExtra(getString(R.string.parcel_todo), todos.get(arg2));
                i.putExtra(getString(R.string.parcel_num), arg2);
                startActivityForResult(i, 1);
            }
        });

        loadAll();
    }

    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_dashboard, menu);
        return true;
    }

    /*
     * update the adapter after a given time, if there is a touch event
     * useful to update the remaining time kinda responsive without using a timer
     */
    private void checkUpdate() {
        Date checkDate = new Date();
        if (checkDate.getTime() > adapterUpdated.getTime() + TIME_REFRESH_RATE) {
            sortRefresh();
            adapterUpdated = new Date();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {

                Log.i(logKey, "RESULT_OK");

                Todo receivedTodo = (Todo) data.getParcelableExtra(getString(R.string.parcel_todo));
                if (data.getBooleanExtra(getString(R.string.parcel_delete), false)) {

                    //Fall result gelöscht
                    Log.i(logKey, "parcel_delete");

                    new AsyncTask<Todo, Void, Todo>() {
                        @Override
                        protected void onPreExecute() {
                            adapter.setEnabled(false);
                            fadeoutAnimation.reset();
                            guiHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    //wenn result delete gestartet wird
                                }
                            });
                            super.onPreExecute();
                        }

                        @Override
                        protected Todo doInBackground(Todo... todo) {
                            return todoAccessor.deleteTodo(todo[0].getId()) ? todo[0] : null;
                        }

                        @Override
                        protected void onPostExecute(Todo todo) {
                            adapter.setEnabled(true);

                            fadeoutAnimation.start();

                            if (todo != null) {
                                guiHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        //wenn erfolgreich gelöscht, dann
                                    }
                                });

                                todos.remove(RememberUtils.getTodoIndexInArrayList(todo, todos));
                                updateTodoCount();
                                sortRefresh();

                            } else {
                                guiHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        //wenn fehlerhaft gelöscht, dann
                                    }
                                });
                            }
                        }
                    }.execute(receivedTodo);

                } else if (data.getBooleanExtra(getString(R.string.parcel_update), false)) {

                    //Fall result updated
                    Log.i(logKey, "parcel_update");

                    new AsyncTask<Todo, Void, Todo>() {
                        @Override
                        protected void onPreExecute() {
                            adapter.setEnabled(false);
                            fadeoutAnimation.reset();
                            guiHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    //wenn result update gestartet wird
                                }
                            });
                            super.onPreExecute();
                        }

                        @Override
                        protected Todo doInBackground(Todo... todo) {
                            return todoAccessor.updateTodo(todo[0]);
                        }

                        @Override
                        protected void onPostExecute(Todo todo) {
                            adapter.setEnabled(true);
                            fadeoutAnimation.start();

                            if (todo != null) {
                                guiHandler.post(new Runnable() {  // Handler benutzen, der mitк UI-Thread verbunden ist
                                    @Override
                                    public void run() {
                                        //wenn result update erfolgreich beendet wird
                                    }
                                });
                                todos.set(RememberUtils.getTodoIndexInArrayList(todo, todos), todo);
                                sortRefresh();
                            } else {
                                guiHandler.post(new Runnable() {  // Handler benutzen, der mitк UI-Thread verbunden ist
                                    @Override
                                    public void run() {
                                        //wenn result update fehlerhaft beendet wird
                                    }
                                });
                            }
                        }
                    }.execute(receivedTodo);
                }
            }
        }

        if (resultCode == RESULT_CANCELED) {
            Log.i(logKey, "RESULT_CANCELED");
        }
    }

    private void sortRefresh() {
        todos = RememberUtils.sortListByOrder(todos, order);
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_list_reload:
                todos.clear();
                adapter.setEnabled(true);
                loadAll();
                break;
            case R.id.menu_sort_date_importance:
                order = SortOrder.DATE;
                todos = RememberUtils.sortListByOrder(todos, order);
                break;
            case R.id.menu_sort_importance_date:
                order = SortOrder.SPECIAL;
                todos = RememberUtils.sortListByOrder(todos, order);
                break;
            case R.id.menu_sort_kreutel_dw:
                order = SortOrder.KREUTELDW;
                todos = RememberUtils.sortListByOrder(todos, order);
                break;
            case R.id.menu_sort_kreutel_wd:
                order = SortOrder.KREUTELWD;
                todos = RememberUtils.sortListByOrder(todos, order);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        adapter.notifyDataSetChanged();
        return true;
    }

    private void updateTodoCount() {
        todoCount.setText(todos.size() + " " + getApplicationContext().getString(R.string.label_todos));
    }

    private void loadAll() {
        new AsyncTask<Void, Void, ArrayList<Todo>>() {
            @Override
            protected void onPreExecute() {
                adapter.setEnabled(false);

                //wenn loadAll gestarted wird
                fadeoutAnimation.reset();

                super.onPreExecute();
            }

            @Override
            protected ArrayList<Todo> doInBackground(Void... params) {
                ArrayList<Todo> todos = todoAccessor.readAllTodos();

                for (Todo t : todos) {
                    ArrayList<Contact> contacts = contactAccessor.readAllContactsFromLookupList(t.getContactIds());
                    t.setContacts(contacts);
                }
                return todos;
            }

            @Override
            protected void onPostExecute(ArrayList<Todo> result) {

                adapter.setEnabled(true);

                fadeoutAnimation.start();

                todos.addAll(result);
                updateTodoCount();
                sortRefresh();

            }

        }.execute();
    }
}
