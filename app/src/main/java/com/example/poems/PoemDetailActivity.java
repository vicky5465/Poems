package com.example.poems;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.PlaybackPreparer;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.ArrayList;

import javax.xml.transform.Result;

/*
This is the detail page of the poem with the title, author, content, explanation
 */

public class PoemDetailActivity extends AppCompatActivity
        implements View.OnClickListener, PlaybackPreparer, PlayerControlView.VisibilityListener{

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
    private PlayerView playerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poem_detail);
        poemId = (Integer) getIntent().getExtras().get(POEM_ID);
        System.out.println("---------" + poemId);
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

        playerView = findViewById(R.id.player_view);
        playerView.setVisibility(View.INVISIBLE);
        // for poems with audio
        if (1 == poemId) {
//            playerView = findViewById(R.id.player_view);
            playerView.setVisibility(View.VISIBLE);
            playerView.setControllerVisibilityListener(this);
            Uri mp4VideoUri = Uri.parse("asset:///jingyesi.m4a");
            DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this,
                    Util.getUserAgent(this, "Poems"));
            // This is the MediaSource representing the media to be played.
            MediaSource videoSource =
                    new ProgressiveMediaSource.Factory(dataSourceFactory)
                            .createMediaSource(mp4VideoUri);


            SimpleExoPlayer player = new SimpleExoPlayer.Builder(this).build();
            playerView.setPlayer(player);
            playerView.setPlaybackPreparer(this);
            // Prepare the player with the source.
            player.prepare(videoSource);
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

    @Override
    public void preparePlayback() {

    }

    @Override
    public void onVisibilityChange(int visibility) {

    }

    @Override
    public void onClick(View view) {
    }


    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
//        releasePlayer();
//        releaseAdsLoader();
//        clearStartPosition();
        setIntent(intent);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
//            initializePlayer();
            if (playerView != null) {
                playerView.onResume();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Util.SDK_INT <= 23 ) {
//            initializePlayer();
            if (playerView != null) {
                playerView.onResume();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            if (playerView != null) {
                playerView.onPause();
            }
//            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            if (playerView != null) {
                playerView.onPause();
            }
//            releasePlayer();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        releaseAdsLoader();
    }
}



