package com.example.kevin.photosapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.zomato.photofilters.geometry.Point;
import com.zomato.photofilters.SampleFilters;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.imageprocessors.SubFilter;
import com.zomato.photofilters.imageprocessors.subfilters.ToneCurveSubfilter;

import java.io.IOException;

import siclo.com.ezphotopicker.api.EZPhotoPick;
import siclo.com.ezphotopicker.api.EZPhotoPickStorage;
import siclo.com.ezphotopicker.api.models.EZPhotoPickConfig;
import siclo.com.ezphotopicker.api.models.PhotoSource;


public class MainActivity extends AppCompatActivity {

    LinearLayout llPhotoContainer;

    static {
        System.loadLibrary("NativeImageProcessor");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */

        llPhotoContainer = (LinearLayout) findViewById(R.id.photo_container);

        findViewById(R.id.bt_gallery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EZPhotoPickConfig config = new EZPhotoPickConfig();
                config.photoSource = PhotoSource.GALLERY;
                config.isAllowMultipleSelect = false;
                config.exportingSize = 1000;
                EZPhotoPick.startPhotoPickActivity(MainActivity.this, config);
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != RESULT_OK) {
            return;
        }

        if (requestCode == EZPhotoPick.PHOTO_PICK_GALLERY_REQUEST_CODE || requestCode == EZPhotoPick.PHOTO_PICK_CAMERA_REQUEST_CODE) {

            Log.v("GOOD REQUEST CODE", "Good request code");
            Bitmap photo = null;
            try {
                photo = new EZPhotoPickStorage(this).loadLatestStoredPhotoBitmap();
                Log.v("GRABBING PHOTO", "Grabbing photo...");
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(photo != null) {
                Log.v("FILTERING", "Filtering...");
                filter(photo);
            }
        } else {
            Log.v("BAD REQUEST CODE", "Bad request code");
        }
    }

    public void filter(Bitmap photo) {
        // according to PhotoDirector, "white square" is RGB(214, 204, 167)
        // so correction should be RGB(41, 51, 88)

        Filter myFilter = new Filter();

        Point[] rgbKnots = new Point[2];
        rgbKnots[0] = new Point(0,0);
        rgbKnots[1] = new Point(255,255);

        Point[] red = new Point[2];
        red[0] = new Point(0, 255);
        red[1] = new Point(255, 255);
        Log.v("RED", "Filtering...");

        Point[] green = new Point[2];
        green[0] = new Point(0, 255);
        green[1] = new Point(255, 255);
        Log.v("GREEN", "Filtering...");

        Point[] blue = new Point[2];
        blue[0] = new Point(0, 255);
        blue[1] = new Point(255, 255);
        Log.v("BLUE", "Filtering...");


        myFilter.addSubFilter(new ToneCurveSubfilter(rgbKnots, red, green, blue));
        Bitmap processedPhoto = myFilter.processFilter(photo);

        drawPhoto(processedPhoto);
    }

    public void drawPhoto(Bitmap newPhoto) {
        Log.v("DRAWING", "Drawing...");
        ImageView iv = new ImageView(this);
        iv.setImageBitmap(newPhoto);
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        llPhotoContainer.addView(iv);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
