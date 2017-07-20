package com.sargent.mark.todolist;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.Calendar;

/**
 * Created by mark on 7/4/17.
 */

public class AddToDoFragment extends DialogFragment {

   private EditText toDo;
   private DatePicker dp;
   private Button add;


   private Spinner categorySpinner;
   //ToDo modifications: Spinner component added with name categorySpinner


   private ArrayAdapter<CharSequence> spinnerAdapter;
   private final String TAG = "addtodofragment";

   public AddToDoFragment() {
   }



   public interface OnDialogCloseListener {
      void closeDialog(int year, int month, int day, String description, String category);
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

      View view = inflater.inflate(R.layout.fragment_to_do_adder, container, false);

      toDo = (EditText) view.findViewById(R.id.toDo);

      dp = (DatePicker) view.findViewById(R.id.datePicker);

      add = (Button) view.findViewById(R.id.add);

      categorySpinner = (Spinner) view.findViewById(R.id.categorySpinner);

      spinnerAdapter = ArrayAdapter.createFromResource(this.getContext(), R.array.todo_category_array, android.R.layout.simple_spinner_item);

      spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

      categorySpinner.setAdapter(spinnerAdapter);

      final Calendar c = Calendar.getInstance();

      int year = c.get(Calendar.YEAR);

      int month = c.get(Calendar.MONTH);

      int day = c.get(Calendar.DAY_OF_MONTH);

      dp.updateDate(year, month, day);

      add.setOnClickListener(new View.OnClickListener() {
         @Override public void onClick(View v) {
            OnDialogCloseListener activity = (OnDialogCloseListener) getActivity();

            activity.closeDialog(dp.getYear(), dp.getMonth(), dp.getDayOfMonth(), toDo.getText().toString(), categorySpinner.getSelectedItem().toString());
            //ToDo modifications: passed the selection category from the menu into the listener

            AddToDoFragment.this.dismiss();

         }
      });

      return view;
   }
}
