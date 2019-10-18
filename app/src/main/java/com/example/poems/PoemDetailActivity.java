package com.example.poems;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/*
This is the detail page of the poem with the title, author, content, explanation
 */

public class PoemDetailActivity extends AppCompatActivity {

    public static final String POEM_ID = "poemId";
    public static final String POEM_INDEX = "poemIndex";
    public static final String POEM_ID_LIST = "poemIdList";
    // private Poem poem;
    private Cursor cursor;
    private SQLiteDatabase db;
    private ArrayList<Integer> poemIdList;
    private int poemId;
    private int poemTotal;
    private int poemIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poem_detail);
        poemId = (Integer) getIntent().getExtras().get(POEM_ID);

        poemIndex = (Integer) getIntent().getExtras().get(POEM_INDEX);
        poemIdList = getIntent().getIntegerArrayListExtra(POEM_ID_LIST);

        if(poemIndex == 0) {
            Button prevButton = findViewById(R.id.poem_prev);
            prevButton.setVisibility(View.INVISIBLE);
        }
        poemTotal = poemIdList.size();
        if(poemIndex == poemTotal - 1) {
            Button nextButton = findViewById(R.id.poem_next);
            nextButton.setVisibility(View.INVISIBLE);
        }
       
       getPoemDetailById(poemId);
    }

    public void getPreviousOne(View view){
        int poemNewIndex = poemIndex-1;
        poemId = poemIdList.get(poemNewIndex);
        Intent intent = getIntent();
        intent.putExtra(POEM_ID, poemId);
        intent.putExtra(PoemDetailActivity.POEM_INDEX, poemNewIndex);
        intent.putIntegerArrayListExtra(PoemDetailActivity.POEM_ID_LIST, poemIdList);
        startActivity(intent);

    }

    public void getNextOne(View view){
        int poemNewIndex = poemIndex + 1;
        poemId = poemIdList.get(poemNewIndex);
        Intent intent = getIntent();
        intent.putExtra(POEM_ID, poemId);
        intent.putExtra(PoemDetailActivity.POEM_INDEX, poemNewIndex);
        intent.putIntegerArrayListExtra(PoemDetailActivity.POEM_ID_LIST, poemIdList);
        startActivity(intent);
    }

    public void addToRecited(View view){

    }

    private void getPoemDetailById(int id) {

        try{
            SQLiteOpenHelper databaseHelper = new PoemDatabaseHelper(this);
            db = databaseHelper.getReadableDatabase();
            cursor = db.query("POEM", new String[]{"TITLE", "AUTHOR", "CONTENT", "DESCRIPTION"},"_id = ?", new String[]{Integer.toString(poemId)}, null, null, null);
            if (cursor.moveToFirst()) {
                String title = cursor.getString(0);
                String author = cursor.getString(1);
                String content = cursor.getString(2);
                String description = cursor.getString(3);
                TextView titleView = findViewById(R.id.titleDetail);
                titleView.setText(title);
                TextView authorView = findViewById(R.id.authorDetail);
                authorView.setText(author);
                TextView contentView = findViewById(R.id.contentDetail);
                contentView.setText(content);
                TextView descView = findViewById(R.id.contentDesc);
                descView.setText(description);
                descView.setMovementMethod(new ScrollingMovementMethod());
            }
            cursor.close();
            db.close();
        } catch (SQLiteException e) {
            Toast toast = Toast.makeText(this, "Database unavailable", Toast.LENGTH_SHORT);
            toast.show();
        }

    }
}
