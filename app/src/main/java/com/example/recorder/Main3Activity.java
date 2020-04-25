package com.example.recorder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class Main3Activity extends AppCompatActivity {

    private DatabaseHelper mDBHelper;
    private SQLiteDatabase mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        serviceDB();
    }

    public void createTag(View v) {
        TextView tw = findViewById(R.id.textView2);
        Cursor cursor = mDb.rawQuery("Select COUNT(*) From Tag Where Title = '" + tw.getText().toString() + "';", null);
        cursor.moveToFirst();
        int counter = cursor.getInt(0);
        cursor.close();
        if(counter > 0){
            Toast.makeText(Main3Activity.this, "Уже есть такой тег", Toast.LENGTH_SHORT).show();
        }
        else{
            ContentValues cv = new ContentValues();
            cv.put("Title", tw.getText().toString());
            mDb.insert("Tag", "Title", cv);
            cancel(v);
        }
    }

    public void cancel(View v) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void serviceDB() {
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
}
