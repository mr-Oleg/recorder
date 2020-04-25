package com.example.recorder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper mDBHelper;
    private SQLiteDatabase mDb;
    private static String globalTagName;
    private static String globalRecTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preparations();

        serviceDB();

        createFeed();

        createTagList();

    }

    private void preparations(){
        setTitle("TabHost");

        TabHost tabHost = (TabHost) findViewById(R.id.tabHost);

        tabHost.setup();

        TabHost.TabSpec tabSpec = tabHost.newTabSpec("tag1");

        tabSpec.setContent(R.id.tab1);
        tabSpec.setIndicator("Заметки");
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("tag2");
        tabSpec.setContent(R.id.tab2);
        tabSpec.setIndicator("Теги");
        tabHost.addTab(tabSpec);

        tabHost.setCurrentTab(0);
    }

    private void createFeed(){
        Cursor cursor = mDb.rawQuery("SELECT * FROM Record", null);
        cursor.moveToFirst();
        int counter = cursor.getCount();
        int colorCounter = 0;
        while (!cursor.isAfterLast()) {
            String title = cursor.getString(1);
            String text = cursor.getString(2);
            String date = cursor.getString(3);
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

            SpannableString ss=new SpannableString(title);
            ss.setSpan(new UnderlineSpan(),0,title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            textView1.setText(date);
            textView2.setText(ss);

            final String ACTION = "android.intent.action.EDITOR";
            View.OnClickListener tes = new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    Intent intent = new Intent(ACTION);
                    startActivity(intent);
                    globalRecTitle = ((TextView)v).getText().toString();
                }
            };
            textView2.setOnClickListener(tes);

            textView3.setText(text);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Теги: ");
            for(String iter : tags){
                stringBuilder.append(" " + iter + " ");
            }
            textView4.setText(stringBuilder.toString());
            textView1.setTextColor((Color.parseColor("#FFFFFF")));
            textView2.setTextColor((Color.parseColor("#FFFFFF")));
            textView3.setTextColor((Color.parseColor("#FFFFFF")));
            textView4.setTextColor((Color.parseColor("#FFFFFF")));
            textView1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            textView2.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            textView3.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            textView4.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            textView1.setPadding(10,0,0,0);
            textView1.setTextSize(18);
            textView1.setTextAlignment(TextView.TEXT_ALIGNMENT_VIEW_START);
            textView2.setPadding(0,15,0,15);
            textView2.setTextSize(18);
            textView2.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
            textView2.setTypeface(null, Typeface.BOLD);
            textView3.setPadding(15,0,0,0);
            textView4.setPadding(15,0,0,15);
            LinearLayout linearLayout = new LinearLayout(this);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            if(colorCounter % 2 == 0) {
                linearLayout.setBackgroundColor((Color.parseColor("#09094A")));
            }
            else{
                linearLayout.setBackgroundColor((Color.parseColor("#FF5C00")));
            }
            linearLayout.addView(textView1);
            linearLayout.addView(textView2);
            linearLayout.addView(textView3);
            linearLayout.addView(textView4);
            LinearLayout wrap = (LinearLayout)findViewById(R.id.wrapLayout);
            wrap.addView(linearLayout);
            cursor1.close();
            tags = null;
            cursor.moveToNext();
            colorCounter++;
        }
        cursor.close();
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

    private void createTagList(){
        Cursor cursor = mDb.rawQuery("SELECT * FROM Tag", null);
        cursor.moveToFirst();
        int colorCounter = 0;
        while (!cursor.isAfterLast()) {
            int id = cursor.getInt(0);
            String title = cursor.getString(1);
            Cursor cursor1 = mDb.rawQuery("SELECT COUNT(*) From RecordTag Where TagId = " + id + ";", null);
            cursor1.moveToFirst();
            int count = 0;
            while (!cursor1.isAfterLast()) {
                count = cursor1.getInt(0);
                cursor1.moveToNext();
            }
            cursor1.close();
            cursor.moveToNext();

            TextView textView5 = new TextView(this);
            TextView textView6 = new TextView(this);
            textView5.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            textView6.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            SpannableString ss=new SpannableString(title);
            ss.setSpan(new UnderlineSpan(),0,title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            textView6.setText(ss);//
            textView6.setTextColor((Color.parseColor("#FFFFFF")));
            textView6.setTextSize(20);
            textView5.setText(Integer.toString(count));
            textView5.setTextColor((Color.parseColor("#FFFFFF")));
            textView5.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
            textView6.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
            LinearLayout.LayoutParams lParams1 = (LinearLayout.LayoutParams)textView5.getLayoutParams();
            LinearLayout.LayoutParams lParams2 = (LinearLayout.LayoutParams)textView6.getLayoutParams();
            lParams1.weight = 1;
            lParams2.weight = 1;
            textView5.setLayoutParams(lParams1);
            textView6.setLayoutParams(lParams2);
            LinearLayout linearLayout1 = new LinearLayout(this);
            if(colorCounter % 2 == 0) {
                linearLayout1.setBackgroundColor((Color.parseColor("#09094A")));
            }
            else{
                linearLayout1.setBackgroundColor((Color.parseColor("#FF5C00")));
            }
            linearLayout1.setOrientation(LinearLayout.HORIZONTAL);
            linearLayout1.addView(textView6);
            linearLayout1.addView(textView5);
            LinearLayout wrap1 = findViewById(R.id.wrapLayout1);
            wrap1.addView(linearLayout1);
            final String ACTION = "android.intent.action.VIEW";
            View.OnClickListener test = new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    Intent intent = new Intent(ACTION);
                    startActivity(intent);
                    globalTagName = ((TextView)v).getText().toString();
                }
            };
            textView6.setOnClickListener(test);
            colorCounter++;
        }
        cursor.close();
    }

    public void createNewTag(View v){
        Intent intent = new Intent(this, Main3Activity.class);
        startActivity(intent);
    }

    public void createNewTemplate(View v){
        Intent intent = new Intent(this, Main5Activity.class);
        startActivity(intent);
    }

    public static String getGlobalRecTitle() {
        return globalRecTitle;
    }

    public static String getGlobalTagName() {
        return globalTagName;
    }
}
