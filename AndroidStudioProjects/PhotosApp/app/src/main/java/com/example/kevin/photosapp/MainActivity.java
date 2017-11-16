package com.example.kevin.photosapp;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;



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
        // so correction should be RGB(+0, +10, +47)

        drawPhoto(photo);

    }

    public void drawPhoto(Bitmap newPhoto) {

        final Bitmap touchedPhoto = newPhoto;

        Log.v("DRAWING", "Drawing...");
        ImageView iv = new ImageView(this);
        iv.setImageBitmap(touchedPhoto);
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        llPhotoContainer.addView(iv);


        iv.setOnTouchListener(new ImageView.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                int action = event.getAction();

                switch (action) {

                    case MotionEvent.ACTION_UP:

                        int x = (int) event.getX();
                        int y = (int) event.getY();
                        int pixel = touchedPhoto.getPixel(x, y);

                        int[] rgbValues = new int[3];
                        rgbValues[0] = Color.red(pixel);
                        rgbValues[1] = Color.green(pixel);
                        rgbValues[2] = Color.blue(pixel);

                        int[] correctionMatrix = calcColorCorrectionMatrix(rgbValues, maxChannelIndex(rgbValues));

                        Toast.makeText(getApplicationContext(), "RGB values: " + rgbValues[0] + ", " + rgbValues[1] + ", " + rgbValues[2], Toast.LENGTH_LONG).show();
                        Toast.makeText(getApplicationContext(), "correction values: " + correctionMatrix[0] + ", " + correctionMatrix[1] + ", " + correctionMatrix[2], Toast.LENGTH_LONG).show();
                        break;

                    default:
                        break;
                }

                return true;

            }
        });
    }

    // color correction works by balancing RGB values on a white pixel
    // looks at highest channel value increases other channels to match
    // according to PhotoDirector, "white" square is RGB(214, 204, 167)
    // so correction should be RGB(+0, +10, +47)
    public int[] calcColorCorrectionMatrix(int[] rgbValues, int maxChannelIndex) {

        int[] correctionMatrix = new int[3];

        for(int i = 0; i < rgbValues.length; i++) {
            correctionMatrix[i] = rgbValues[maxChannelIndex] - rgbValues[i];
        }

        return correctionMatrix;

    }

    // returns the index of the channel with the highest value
    // 0 = red, 1 = green, 2 = blue
    public int maxChannelIndex(int[] rgbValues) {

        int index = -1;
        int colorValue = -1;

        for(int i = 0; i < rgbValues.length; i++) {
            if(rgbValues[i] > colorValue) {
                index = i;
                colorValue = rgbValues[i];
            }
        }

        return index;
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
