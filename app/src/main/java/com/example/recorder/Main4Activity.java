package com.example.recorder;

import androidx.appcompat.app.AppCompatActivity;

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
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.example.recorder.MainActivity.getGlobalRecTitle;

public class Main4Activity extends AppCompatActivity {

    private TextView mDisplayDate;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private DatabaseHelper mDBHelper;
    private SQLiteDatabase mDb;
    private int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);

        getCalendar();

        serviceDB();

        fillOutApps();

        preparations();
    }

    public void save(View v){//
        tagUpdate();
        ContentValues cv = new ContentValues();
        TextView titleField = findViewById(R.id.textView9);
        cv.put("Title",titleField.getText().toString());
        TextView textField = findViewById(R.id.editText2);
        cv.put("Text",textField.getText().toString());
        TextView dateField = findViewById(R.id.editText);
        cv.put("Date",dateField.getText().toString());
        mDb.update("Record", cv, "Id='" + id + "'", null);
        Toast.makeText(Main4Activity.this, "Запись обновлена", Toast.LENGTH_SHORT).show();
    }

    public void cancel(View v){
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }

    public void remove(View v){
        mDb.delete("RecordTag","RecordId = '" + id + "';",null);
        mDb.delete("Record","Id = '" + id + "';",null);
        cancel(v);
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
                        Main4Activity.this,
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

    private void fillOutApps(){
        String title = getGlobalRecTitle();
        TextView titleField = findViewById(R.id.textView9);
        titleField.setText(title);
        Cursor cursor = mDb.rawQuery("SELECT * FROM Record Where Title = '" + title +"';", null);
        cursor.moveToFirst();
        String date = cursor.getString(3);
        String text = cursor.getString(2);
        id = cursor.getInt(0);
        TextView dateField = findViewById(R.id.editText);
        dateField.setText(date);
        TextView textField = findViewById(R.id.editText2);
        textField.setText(text);
        cursor.close();
    }

    private void preparations(){
        MultiAutoCompleteTextView autoCompleteTextView = (MultiAutoCompleteTextView) findViewById(R.id.autocomplete);
        List<String> selectedItems = new ArrayList<>();
        List<String> allItems = new ArrayList<>();
        Cursor cursor = mDb.rawQuery("SELECT Tag.Title FROM Tag Inner Join RecordTag On RecordTag.TagId = Tag.Id Where RecordTag.RecordId = '" + id +"';", null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            selectedItems.add(cursor.getString(0));
            cursor.moveToNext();
        }
        cursor.close();
        String[] str = new String[selectedItems.size()];
        selectedItems.toArray(str);
        StringBuilder stringBuilder = new StringBuilder();
        for(String iter : str){
            stringBuilder.append(iter + ", ");
            System.out.println(iter);
        }


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
        autoCompleteTextView.setText(stringBuilder);
    }

    private Set<TagContainer> getCurrentTagsFromInterface(){
        MultiAutoCompleteTextView autoCompleteTextView = (MultiAutoCompleteTextView) findViewById(R.id.autocomplete);
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

    private List<TagContainer> getCurrentTagsFromDBForRecord(int recordId){
        Cursor cursor = mDb.rawQuery("SELECT RecordTag.TagId, Tag.Title FROM RecordTag Inner Join Tag On Tag.Id = RecordTag.TagId Where RecordTag.RecordId = '" + recordId +"';", null);
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

    private void dropOldRecords() {
        mDb.delete("RecordTag", "RecordId = '" + id + "';", null);
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

    private void tagUpdate(){
        dropOldRecords();
        saveNewRecords();
    }

}
