package com.example.kevin.photosapp;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Matrix;
import android.graphics.RectF;
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
    static Bitmap correctedPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        if (requestCode == EZPhotoPick.PHOTO_PICK_GALLERY_REQUEST_CODE) {

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
                filterAndDrawPhoto(photo);
            }
        } else {
            Log.v("BAD REQUEST CODE", "Bad request code");
        }
    }


    public void filterAndDrawPhoto(final Bitmap newPhoto) {

        final Bitmap touchedPhoto = newPhoto;

        Log.v("DRAWING", "Drawing...");
        //final ImageView iv = new ImageView(this);
        final MagnifyingGlass iv = new MagnifyingGlass(this);
        iv.init(touchedPhoto);
        iv.setImageBitmap(touchedPhoto);
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        llPhotoContainer.addView(iv);


        iv.setOnTouchListener(new ImageView.OnTouchListener() {
                                  @Override
                                  public boolean onTouch(View v, MotionEvent event) {
                                      iv.onTouchEvent(event);
                                      return true;
                                  }
                              });




        /*
        iv.setOnTouchListener(new ImageView.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                int action = event.getAction();
                int x = (int) event.getX();
                int y = (int) event.getY();
                int pixel = touchedPhoto.getPixel(x, y);

                switch (action) {

                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        break;

                    case MotionEvent.ACTION_UP:

                        int[] rgbValues = new int[3];
                        rgbValues[0] = Color.red(pixel);
                        rgbValues[1] = Color.green(pixel);
                        rgbValues[2] = Color.blue(pixel);

                        int[] correctionMatrix = calcColorCorrectionMatrix(rgbValues, maxChannelIndex(rgbValues));

                        Bitmap correctedPhoto = colorCorrect(touchedPhoto.copy(Bitmap.Config.ARGB_8888, true), correctionMatrix);

                        Toast.makeText(getApplicationContext(), "RGB values: " + rgbValues[0] + ", " + rgbValues[1] + ", " + rgbValues[2], Toast.LENGTH_LONG).show();
                        Toast.makeText(getApplicationContext(), "correction values: " + correctionMatrix[0] + ", " + correctionMatrix[1] + ", " + correctionMatrix[2], Toast.LENGTH_LONG).show();

                        iv.setImageBitmap(correctedPhoto);
                        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);





                        // if user clicks reset, restores touchedPhoto

                        break;

                    default:
                        break;
                }
                return true;
            }
        });*/

    }

    // takes in a photo and a correction matrix
    // changes each pixel according to the correction matrix

    // this URL wasn't used, but might be an interesting thing to look at
    // https://github.com/pushd/colorpal
    public Bitmap colorCorrect(Bitmap photo, int[] correctionMatrix) {

        // copies(?) the original photo
        correctedPhoto = photo;

        // array for storing the corrected RGB values for a given pixel
        int[] correctedRGB = new int[3];

        // iterating through each row and column of the photo...
        for(int y = 0; y < photo.getHeight(); y++) {
            for(int x = 0; x < photo.getWidth(); x++) {

                int pixel = photo.getPixel(x, y);

                // correctedRGB[i] = color of the original photo + correction factor for that channel
                correctedRGB[0] = Color.red(pixel) + correctionMatrix[0];
                correctedRGB[1] = Color.green(pixel) + correctionMatrix[1];
                correctedRGB[2] = Color.blue(pixel) + correctionMatrix[2];

                // consolidates RGB values into a color integer
                int correctPixelColor = Color.rgb(correctedRGB[0], correctedRGB[1], correctedRGB[2]);

                // "writes" the adjusted pixel color to the correctedPhoto
                correctedPhoto.setPixel(x, y, correctPixelColor);
            }
        }

        return correctedPhoto;
    }

    // white balance works by balancing RGB channels on a white pixel
    // looks at highest channel value increases other channels to match
    // according to PhotoDirector, "white" square is RGB(214, 204, 167)
    // so correction should be RGB(+0, +10, +47)
    public int[] calcColorCorrectionMatrix(int[] rgbValues, int maxChannelIndex) {

        /* from PhotoDirector app

         "pure" color patches from bad photo are:
         white: (214, 204, 167)
         grey: (97, 85, 55)
         red: (158, 83, 33)
         green: (86, 124, 66)
         blue: (75, 80, 131)

         after touching the white square

         "pure" color patches from corrected photo are:
         white: (214, 214, 214)
         grey: (98, 90, 74)
         red: (158, 86, 44)
         green: (85, 132, 87)
         blue: (82, 85, 170)

         after touching the 18% grey square (from bad photo)
         white: (206, 221, 250)
         grey: (93, 93, 92)
         red: (150, 88, 55)
         green: (90, 136, 109)
         blue: (90 , 90, 213)

         Conclusion: PhotoDirector actually only does white balance, not color correction

        */


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
