package com.example.poems;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import javax.xml.transform.Result;

/*
This is the detail page of the poem with the title, author, content, explanation
 */

public class PoemDetailActivity extends AppCompatActivity {

    public static final String POEM_ID = "poemId";
    public static final String POEM_INDEX = "poemIndex";
    public static final String POEM_ID_LIST = "poemIdList";
    public static final String POEM_IS_RECITED = "poemIsRecited";
    // private Poem poem;
    private Cursor cursor;
//    private SQLiteDatabase db;
    private ArrayList<Integer> poemIdList;
    private int poemId;
    private int poemTotal;
    private int poemIndex;
    private boolean poemIsRecited;
    private Button recitedButton;
    private ImageView recitedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poem_detail);
        poemId = (Integer) getIntent().getExtras().get(POEM_ID);
        getPoemDetailById(poemId);
        poemIndex = (Integer) getIntent().getExtras().get(POEM_INDEX);
        poemIdList = getIntent().getIntegerArrayListExtra(POEM_ID_LIST);
//        poemIsRecited = (Boolean) getIntent().getExtras().get(POEM_IS_RECITED);
        if (poemIndex == 0) {
            Button prevButton = findViewById(R.id.poem_prev);
            prevButton.setVisibility(View.INVISIBLE);
        }
        poemTotal = poemIdList.size();
        if (poemIndex == poemTotal - 1) {
            Button nextButton = findViewById(R.id.poem_next);
            nextButton.setVisibility(View.INVISIBLE);
        }
        if (poemIsRecited) {
            Button recitedButton = findViewById(R.id.poem_recited);
            recitedButton.setVisibility(View.INVISIBLE);
            ImageView recitedImage = findViewById(R.id.img_recited);
            recitedImage.setVisibility(View.VISIBLE);
        } else {
            recitedButton = findViewById(R.id.poem_recited);
            recitedButton.setVisibility(View.VISIBLE);
            recitedImage = findViewById(R.id.img_recited);
            recitedImage.setVisibility(View.INVISIBLE);
        }
       

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
        new UpdatePoemTask().execute(poemId);
//        updatePoem();
    }

    private void getPoemDetailById(int id) {

        try{
            SQLiteOpenHelper databaseHelper = new PoemDatabaseHelper(this);
            SQLiteDatabase db = databaseHelper.getReadableDatabase();
            cursor = db.query("POEM", new String[]{"TITLE", "AUTHOR", "CONTENT", "DESCRIPTION", "IS_PASS"},"_id = ?", new String[]{Integer.toString(poemId)}, null, null, null);
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
                poemIsRecited = (cursor.getInt(4) == 1);
            }
            cursor.close();
            db.close();
        } catch (SQLiteException e) {
            Toast toast = Toast.makeText(this, "Database unavailable", Toast.LENGTH_SHORT);
            toast.show();
        }

    }

    private void updatePoem() {

        SQLiteOpenHelper poemDatabaseHelper = new PoemDatabaseHelper(this);
        SQLiteDatabase db = poemDatabaseHelper.getReadableDatabase();
        ContentValues poemValues = new ContentValues();
        poemValues.put("IS_PASS", true);
        try {
            db.update("Poem", poemValues, "_id = ?", new String[]{Integer.toString(poemId)});
            db.close();
        } catch (SQLiteException e) {
            Toast toast = Toast.makeText(this, "Database unavailable", Toast.LENGTH_SHORT);
            toast.show();
        }

    }

//    public void onRestart() {
//        super.onRestart();
//    }

    private class UpdatePoemTask extends AsyncTask<Integer, Void, Boolean> {

        ContentValues poemValues;

        protected void onPreExecute() {
            poemValues = new ContentValues();
            poemValues.put("IS_PASS", true);
        }

        protected Boolean doInBackground(Integer... poemId) {
            int id = poemId[0];
            SQLiteOpenHelper poemDatabaseHelper = new PoemDatabaseHelper(PoemDetailActivity.this);
            try {
                SQLiteDatabase db = poemDatabaseHelper.getWritableDatabase();
                db.update("Poem", poemValues, "_id = ?", new String[]{Integer.toString(id)});
                db.close();
                return true;
            } catch (SQLiteException e) {
                return false;
            }
        }

        protected void onPostExecute(Boolean success) {
            if(!success) {
                Toast toast = Toast.makeText(PoemDetailActivity.this, "Database unavailable", Toast.LENGTH_SHORT);
                toast.show();
            } else {
                recitedButton.setVisibility(View.INVISIBLE);
                recitedImage.setVisibility(View.VISIBLE);
            }
        }
    }
//    private void updatePoemTask() {
//
//    }

}



