package com.example.recorder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.example.recorder.MainActivity.getGlobalTagName;

public class Main2Activity extends AppCompatActivity {

    private DatabaseHelper mDBHelper;
    private SQLiteDatabase mDb;
    private String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        serviceDB();
        title = getGlobalTagName();
        EditText et = findViewById(R.id.textView8);
        et.setText(title);
        createFeed();
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

    private void createFeed() {
        int colorCounter = 0;
        Cursor cursor = mDb.rawQuery("SELECT Record.Id,Record.Date,Record.Text,Record.Title From Tag Join RecordTag On RecordTag.TagId = Tag.Id Join Record On Record.Id = RecordTag.RecordId Where Tag.Title = '" + title + "';", null);
        cursor.moveToFirst();
        int counter = cursor.getCount();
        while (!cursor.isAfterLast()) {
            String title = cursor.getString(3);
            String text = cursor.getString(2);
            String date = cursor.getString(1);
            int id = cursor.getInt(0);
            Cursor cursor1 = mDb.rawQuery("SELECT * From Tag INNER JOIN RecordTag On RecordTag.TagId = Tag.Id Where RecordTag.RecordId = " + id + ";", null);
            cursor1.moveToFirst();
            List<String> tags = new ArrayList<>();
            while (!cursor1.isAfterLast()) {
                tags.add(cursor1.getString(1));
                cursor1.moveToNext();
            }
            TextView textView1 = new TextView(this);
            TextView textView2 = new TextView(this);
            TextView textView3 = new TextView(this);
            TextView textView4 = new TextView(this);
            textView1.setText(date);
            textView2.setText(title);
            textView3.setText(text);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Теги: ");
            for (String iter : tags) {
                stringBuilder.append(" " + iter + " ");
            }
            textView4.setText(stringBuilder.toString());
            textView1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            textView2.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            textView3.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            textView4.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            textView1.setPadding(10, 40, 0, 0);
            textView1.setTextSize(18);
            textView1.setTextColor((Color.parseColor("#FFFFFF")));
            textView2.setTextColor((Color.parseColor("#FFFFFF")));
            textView3.setTextColor((Color.parseColor("#FFFFFF")));
            textView4.setTextColor((Color.parseColor("#FFFFFF")));
            textView1.setTextAlignment(TextView.TEXT_ALIGNMENT_VIEW_START);
            textView2.setPadding(0, 1, 0, 15);
            textView2.setTextSize(18);
            textView2.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
            textView2.setTypeface(null, Typeface.BOLD);
            textView3.setPadding(15, 0, 0, 0);
            textView4.setPadding(15, 0, 0, 15);
            LinearLayout linearLayout = new LinearLayout(this);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.addView(textView1);
            linearLayout.addView(textView2);
            linearLayout.addView(textView3);
            linearLayout.addView(textView4);
            if (colorCounter % 2 == 0) {
                linearLayout.setBackgroundColor((Color.parseColor("#09094A")));
            } else {
                linearLayout.setBackgroundColor((Color.parseColor("#FF5C00")));
            }
            LinearLayout wrap = (LinearLayout) findViewById(R.id.wrapLayout1);
            wrap.addView(linearLayout);
            cursor1.close();
            tags = null;
            cursor.moveToNext();
            colorCounter++;
        }
        cursor.close();
    }

    public void onClickCancel(View v) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void onClickRemove(View v) {
        Cursor cursor = mDb.rawQuery("Select Id From Tag Where Title = '" + title + "';", null);
        cursor.moveToFirst();
        int id = cursor.getInt(0);
        cursor.close();
        mDb.delete("RecordTag","TagId = '" + id + "';",null);
        mDb.delete("Tag","Id = '" + id + "';",null);
        onClickCancel(v);
    }

    public void onClickSave(View v) {
        TextView tw = findViewById(R.id.textView8);
        Cursor cursor = mDb.rawQuery("Select COUNT(*) From Tag Where Title = '" + tw.getText().toString() + "';", null);
        cursor.moveToFirst();
        int counter = cursor.getInt(0);
        cursor.close();
        if (counter >= 1) {
            Toast.makeText(Main2Activity.this, "Уже есть такой тег", Toast.LENGTH_SHORT).show();
        } else {
            ContentValues cv = new ContentValues();
            cv.put("Title", tw.getText().toString());
            mDb.update("Tag", cv, "Title='" + title + "'", null);
            Toast.makeText(Main2Activity.this, "Тег обновлен", Toast.LENGTH_SHORT).show();
        }
    }
}
