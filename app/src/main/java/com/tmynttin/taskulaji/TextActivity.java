package com.tmynttin.taskulaji;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.text.Html;
import android.text.Html.ImageGetter;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class TextActivity extends AppCompatActivity implements ImageGetter {
    private static final String TAG = "TextActivity";

    public static final String EXTRA_NEWS_TITLE = "com.tmynttin.taskulaji.extra.EXTRA_NEWS_TITLE";
    public static final String EXTRA_NEWS_CONTENT = "com.tmynttin.taskulaji.extra.EXTRA_NEWS_CONTENT";

    private TextView newsTitle;
    private TextView newsContents;

    private String title;
    private String contents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text);

        Intent intent = getIntent();

        title = intent.getStringExtra(EXTRA_NEWS_TITLE);
        contents = intent.getStringExtra(EXTRA_NEWS_CONTENT);

        newsTitle = (TextView) findViewById(R.id.newsTitle);
        newsContents = (TextView) findViewById(R.id.newsContent);

        newsTitle.setText(title);
        newsContents.setText(Html.fromHtml(contents, this,null));
        newsContents.setMovementMethod(LinkMovementMethod.getInstance());

    }

    // Code snippet from pskink @ https://stackoverflow.com/questions/16179285/html-imagegetter-textview
    @Override
    public Drawable getDrawable(String source) {
        LevelListDrawable d = new LevelListDrawable();
        Drawable empty = getResources().getDrawable(R.mipmap.ic_launcher);
        d.addLevel(0, 0, empty);
        d.setBounds(0, 0, empty.getIntrinsicWidth(), empty.getIntrinsicHeight());

        new LoadImage().execute(source, d);

        return d;
    }

    class LoadImage extends AsyncTask<Object, Void, Bitmap> {

        private LevelListDrawable mDrawable;

        @Override
        protected Bitmap doInBackground(Object... params) {
            String source = (String) params[0];
            mDrawable = (LevelListDrawable) params[1];
            Log.d(TAG, "doInBackground " + source);
            try {
                InputStream is = new URL(source).openStream();
                return BitmapFactory.decodeStream(is);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            Log.d(TAG, "onPostExecute drawable " + mDrawable);
            Log.d(TAG, "onPostExecute bitmap " + bitmap);
            if (bitmap != null) {
                BitmapDrawable d = new BitmapDrawable(bitmap);
                mDrawable.addLevel(1, 1, d);
                mDrawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
                mDrawable.setLevel(1);
                // i don't know yet a better way to refresh TextView
                // mTv.invalidate() doesn't work as expected
                CharSequence t = newsContents.getText();
                newsContents.setText(t);
            }
        }
    }

}