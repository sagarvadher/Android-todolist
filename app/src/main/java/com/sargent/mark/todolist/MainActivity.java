package com.sargent.mark.todolist;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuAdapter;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.sargent.mark.todolist.data.Contract;
import com.sargent.mark.todolist.data.DBHelper;

import java.text.SimpleDateFormat;

public class MainActivity extends AppCompatActivity
      implements AddToDoFragment.OnDialogCloseListener,
      UpdateToDoFragment.OnUpdateDialogCloseListener,
      AdapterView.OnItemSelectedListener {

   private RecyclerView rv;

   private FloatingActionButton button;

   private DBHelper helper;

   private Cursor cursor;

   private SQLiteDatabase db;

   ToDoListAdapter adapter;

   private final String TAG = "mainactivity";

   @Override
   protected void onCreate(@Nullable Bundle savedInstanceState) {

      super.onCreate(savedInstanceState);

      setContentView(R.layout.activity_main);

      Log.d(TAG, "oncreate called in main activity");

      button = (FloatingActionButton) findViewById(R.id.addToDo);

      button.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {

            FragmentManager fm = getSupportFragmentManager();

            AddToDoFragment frag = new AddToDoFragment();

            frag.show(fm, "addtodofragment");
         }
      });

      rv = (RecyclerView) findViewById(R.id.recyclerView);

      rv.setLayoutManager(new LinearLayoutManager(this));

   }

   @Override protected void onStop() {

      super.onStop();

      if (db != null) {

         db.close();
      }

      if (cursor != null) {

         cursor.close();
      }
   }

   @Override protected void onStart() {
      super.onStart();

      helper = new DBHelper(this);

      db = helper.getWritableDatabase();

      cursor = getAllItems(db);

      adapter = new ToDoListAdapter(cursor,
            new ToDoListAdapter.ItemClickListener() {

               @Override
               public void onItemClick(int pos, String description, String duedate, String category, long id) {

                  Log.d(TAG, "item click id: " + id);

                  String[] dateInfo = duedate.split("-");

                  int year = Integer.parseInt(dateInfo[0].replaceAll("\\s", ""));

                  int month = Integer.parseInt(dateInfo[1].replaceAll("\\s", ""));

                  int day = Integer.parseInt(dateInfo[2].replaceAll("\\s", ""));

                  FragmentManager fm = getSupportFragmentManager();

                  UpdateToDoFragment frag = UpdateToDoFragment.newInstance(year, month, day, description, category, id);

                  frag.show(fm, "updatetodofragment");
               }

            });

      rv.setAdapter(adapter);

      new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

         @Override
         public boolean onMove(RecyclerView recyclerView,
               RecyclerView.ViewHolder viewHolder,
               RecyclerView.ViewHolder target) {
            return false;
         }

         @Override
         public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {

            long id = (long) viewHolder.itemView.getTag();

            Log.d(TAG, "passing id: " + id);

            removeToDo(db, id);

            adapter.swapCursor(getAllItems(db));
         }
      }).attachToRecyclerView(rv);
   }

   @Override
   public void closeDialog(int year, int month, int day, String description, String category) {

      addToDo(db, description, formatDate(year, month, day), category);

      cursor = getAllItems(db);

      adapter.swapCursor(cursor);
   }

   public String formatDate(int year, int month, int day) {

      SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE, dd MMM yyyy");

      return String.format("%02d-%02d-%04d", year, month + 1, day);
   }

   private Cursor getAllItems(SQLiteDatabase db) {

      return db.query(Contract.TABLE_TODO.TABLE_NAME, null, null, null, null, null, Contract.TABLE_TODO.COLUMN_NAME_DUE_DATE);
   }

   private long addToDo(SQLiteDatabase db, String description, String duedate, String category) {

      ContentValues cv = new ContentValues();

      cv.put(Contract.TABLE_TODO.COLUMN_NAME_DESCRIPTION, description);

      cv.put(Contract.TABLE_TODO.COLUMN_NAME_DUE_DATE, duedate);

      cv.put(Contract.TABLE_TODO.COLUMN_NAME_CATEGORY, category);
      //ToDO modifications: new field needs to be added in the database

      return db.insert(Contract.TABLE_TODO.TABLE_NAME, null, cv);
   }

   private boolean removeToDo(SQLiteDatabase db, long id) {

      Log.d(TAG, "deleting id: " + id);

      return db.delete(Contract.TABLE_TODO.TABLE_NAME, Contract.TABLE_TODO._ID + "=" + id, null) > 0;
   }

   private int updateToDo(SQLiteDatabase db, int year, int month, int day, String description, String category, long id) {

      String duedate = formatDate(year, month - 1, day);

      ContentValues cv = new ContentValues();

      cv.put(Contract.TABLE_TODO.COLUMN_NAME_DESCRIPTION, description);

      cv.put(Contract.TABLE_TODO.COLUMN_NAME_DUE_DATE, duedate);

      cv.put(Contract.TABLE_TODO.COLUMN_NAME_CATEGORY, category);
      //ToDO modifications:new field needs to be updated in the database

      return db.update(Contract.TABLE_TODO.TABLE_NAME, cv, Contract.TABLE_TODO._ID + "=" + id, null);
   }

   @Override public void closeUpdateDialog(int year, int month, int day, String description, String category, long id) {

      updateToDo(db, year, month, day, description, category, id);

      adapter.swapCursor(getAllItems(db));
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      getMenuInflater().inflate(R.menu.menu, menu);

      ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.menu_item_todo_category_array, android.R.layout.simple_spinner_item);

      adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

      MenuItem item = menu.findItem(R.id.categoryMenuItem);

      Spinner spinner = (Spinner) MenuItemCompat.getActionView(item);

      spinner.setOnItemSelectedListener(this);

      spinner.setAdapter(adapter);

      return true;
      //ToDO modifications: menu options created
   }

   public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

      String selectedCategory = parent.getItemAtPosition(pos).toString();

      if ("All".equalsIgnoreCase(selectedCategory)) {

         adapter.swapCursor(getAllItems(db));

      } else {

         adapter.swapCursor(getItemsForCategory(db, selectedCategory));
      }
      //ToDo modifications: obtain the menu item selected

   }

   public void onNothingSelected(AdapterView<?> parent) {
   }

   private Cursor getItemsForCategory(SQLiteDatabase db, String category) {

      return db.query(Contract.TABLE_TODO.TABLE_NAME, null, Contract.TABLE_TODO.COLUMN_NAME_CATEGORY + "='" + category + "'", null, null, null, Contract.TABLE_TODO.COLUMN_NAME_DUE_DATE);
      //ToDo modifications: get the todo items from the category

   }
}
