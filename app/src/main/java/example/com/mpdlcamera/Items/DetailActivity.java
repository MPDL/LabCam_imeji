package example.com.mpdlcamera.Items;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;

import example.com.mpdlcamera.R;
import example.com.mpdlcamera.Settings.SettingsActivity;
import uk.co.senab.photoview.PhotoViewAttacher;


public class DetailActivity extends Activity {
    private static final String LOG_TAG = DetailActivity.class.getSimpleName();

    private Activity activity = this;
    private View rootView;
    private String itemPath;
    private PhotoViewAttacher mAttacher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);


        rootView = getWindow().getDecorView().findViewById(android.R.id.content);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            itemPath = extras.getString("itemPath");
            ImageView imageView = (ImageView) rootView.findViewById(R.id.detail_image);

            mAttacher = new PhotoViewAttacher(imageView);
            //mAttacher.setScaleType(ImageView.ScaleType.CENTER_INSIDE);  //scale and show the whole photo based on Longth
            //mAttacher.setScaleType(ImageView.ScaleType.FIT_CENTER);  //scale and show the whole photo based on Width
            //mAttacher.setScaleType(ImageView.ScaleType.CENTER);     // no scale, cut the photo to fit
            //mAttacher.setScaleType(ImageView.ScaleType.CENTER_CROP); // no scale , cut by center
            //mAttacher.setScaleType(ImageView.ScaleType.FIT_XY);  //no scale, show the original photo

            WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            Display display = windowManager.getDefaultDisplay();

            Point size = new Point();
            display.getSize(size);

//            Log.v(size.x / 2 + " ", size.y / 2 + "");

//                DataItem item = new Select().
//                        from(DataItem.class).
//                        where("filename = ?", itemName).executeSingle();
//                Log.v(LOG_TAG, item.getWebResolutionUrlUrl());

            boolean isLocalImage = extras.getBoolean("isLocalImage",false);
            if(isLocalImage){
                Uri uri = Uri.fromFile(new File(itemPath));
                Log.e(LOG_TAG, itemPath);
                Picasso.with(activity)
                        .load(uri)
                        .resize(size.x, size.y)
                        .centerInside()
                                //.centerCrop()
                                //.placeholder(R.drawable.progress_animation)
                        .into(imageView);
            }else {
                   Picasso.with(activity)
                           .load(itemPath)
                           .resize(size.x, size.y)
                           .centerInside()
                           .into(imageView);
            }


            mAttacher.update();

        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent showSettingIntent = new Intent(this, SettingsActivity.class);
            startActivity(showSettingIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}