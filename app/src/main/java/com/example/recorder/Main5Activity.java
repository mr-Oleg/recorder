package com.example.recorder;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main5Activity extends AppCompatActivity {

    private TextView mDisplayDate;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private DatabaseHelper mDBHelper;
    private SQLiteDatabase mDb;
    private int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main5);

        getCalendar();

        serviceDB();

        preparations();
    }

    public void save(View v){
        TextView title = findViewById(R.id.textView9);
        TextView text = findViewById(R.id.editText2);
        TextView date = findViewById(R.id.editText);
        ContentValues cv = new ContentValues();
        cv.put("Title",title.getText().toString());
        cv.put("Text",text.getText().toString());
        cv.put("Date",date.getText().toString());
        id = (int)mDb.insert("Record",null,cv);
        saveNewRecords();
        cancel(v);
    }

    public void cancel(View v){
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }

    private void saveNewRecords(){
        Set<TagContainer> currentTagsFromInterface = getCurrentTagsFromInterface();
        for(TagContainer tag : currentTagsFromInterface){
            ContentValues cv = new ContentValues();
            cv.put("RecordId",id);
            cv.put("TagId",tag.getId());
            mDb.insert("RecordTag",null,cv);
        }
    }

    private Set<TagContainer> getCurrentTagsFromInterface(){
        MultiAutoCompleteTextView autoCompleteTextView = (MultiAutoCompleteTextView) findViewById(R.id.autocomplete1);
        String tagString = autoCompleteTextView.getText().toString();
        String[] tags = tagString.split(",");
        for(int iter = 0; iter < tags.length; iter++){
            tags[iter] = tags[iter].trim();
        }
        List<TagContainer> allTags = getAllTags();
        Set<TagContainer> selectedTags = new HashSet<>();
        for(TagContainer tag : allTags){
            for(String interfaceTag : tags){
                if(interfaceTag.equals(tag.getTitle())){
                    selectedTags.add(tag);
                }
            }
        }
        return selectedTags;
    }

    private List<TagContainer> getAllTags(){
        Cursor cursor = mDb.rawQuery("SELECT * FROM Tag;", null);
        cursor.moveToFirst();
        List<TagContainer> container = new ArrayList<>();
        while(!cursor.isAfterLast()){
            TagContainer tag = new TagContainer();
            tag.setId(cursor.getInt(0));
            tag.setTitle(cursor.getString(1));
            container.add(tag);
            cursor.moveToNext();
        }
        cursor.close();
        return container;
    }

    private void tagUpdate(){
        saveNewRecords();
    }

    private void getCalendar(){
        mDisplayDate = findViewById(R.id.editText);
        mDisplayDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        Main5Activity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListener,
                        year,month,day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                String date = year + "-" + month + "-" + day;
                mDisplayDate.setText(date);
            }
        };
    }

    private void serviceDB(){
        mDBHelper = new DatabaseHelper(this);
        try {
            mDBHelper.updateDataBase();
        } catch (IOException mIOException) {
            throw new Error("UnableToUpdateDatabase");
        }

        try {
            mDb = mDBHelper.getWritableDatabase();
        } catch (SQLException mSQLException) {
            throw mSQLException;
        }
    }

    private void preparations(){
        MultiAutoCompleteTextView autoCompleteTextView = (MultiAutoCompleteTextView) findViewById(R.id.autocomplete1);
        List<String> allItems = new ArrayList<>();
        Cursor cursor1 = mDb.rawQuery("SELECT Title FROM Tag;", null);
        cursor1.moveToFirst();
        while (!cursor1.isAfterLast()) {
            allItems.add(cursor1.getString(0));
            cursor1.moveToNext();
        }
        cursor1.close();
        String[] all = new String[allItems.size()];
        allItems.toArray(all);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, all );
        autoCompleteTextView.setAdapter(adapter);
        autoCompleteTextView.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
    }
}
