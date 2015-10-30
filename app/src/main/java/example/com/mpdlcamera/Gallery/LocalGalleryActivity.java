package example.com.mpdlcamera.Gallery;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedHashSet;

import example.com.mpdlcamera.Folder.MainActivity;
import example.com.mpdlcamera.Model.Gallery;
import example.com.mpdlcamera.R;
import example.com.mpdlcamera.Settings.SettingsActivity;

/**
 * Created by kiran on 22.10.15.
 */
public class LocalGalleryActivity extends AppCompatActivity {

    Toolbar toolbar;
    private View rootView;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle drawerToggle;
    CollapsingToolbarLayout collapsingToolbarLayout;
    private NavigationView navigation;
    GridView gridView;
    SharedPreferences preferences;

    GalleryListAdapter adapter;

    private Activity activity = this;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.local_gallery);

        rootView = getWindow().getDecorView().findViewById(android.R.id.content);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        gridView = (GridView) findViewById(R.id.gallery_gridView);
        setSupportActionBar(toolbar);

        TextView titleView = (TextView) findViewById(R.id.title);


        titleView.setText("Local Gallery");
      //  titleView.setText("Local Gallery");

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        rootView = getWindow().getDecorView().findViewById(android.R.id.content);




        String[] albums = new String[]{MediaStore.Images.Media.BUCKET_DISPLAY_NAME};
        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        preferences = PreferenceManager.getDefaultSharedPreferences(this);


        final ArrayList<Gallery> folders = new ArrayList<Gallery>();
        final ArrayList<String> folders1 = new ArrayList<String>();

        Cursor cur = getContentResolver().query(images, albums, null, null, null);

        if (cur.moveToFirst()) {

            int albumLocation = cur.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);

            do {
                Gallery album = new Gallery();
                album.setGalleryName(cur.getString(albumLocation));
                String curr = cur.getString(albumLocation);
                if(!folders1.contains(curr)) {
                    folders.add(album);
                    folders1.add(curr);
                }

                Log.i("ListingImages", " album=" + album);
            } while (cur.moveToNext());
        }

        ArrayList<Gallery> imageFolders = new ArrayList<Gallery>();
        imageFolders = new ArrayList<Gallery>(new LinkedHashSet<Gallery>(folders));


        adapter = new GalleryListAdapter(activity, imageFolders );

        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Gallery gallery = (Gallery) adapter.getItem(position);

                String status = preferences.getString(gallery.getGalleryName(),"");

                if(status.equalsIgnoreCase("On")) {

                    Intent activatedGalleryIntent = new Intent(activity, ActivatedGalleryActivity.class);
                    activatedGalleryIntent.putExtra("galleryName", gallery.getGalleryName());
                    activatedGalleryIntent.putExtra("galleryPath", gallery.getGalleryPath());

                    startActivity(activatedGalleryIntent);

                }
                else {
                    Intent galleryImagesIntent = new Intent(activity, LocalImageActivity.class);
                    galleryImagesIntent.putExtra("galleryTitle", gallery.getGalleryPath());

                    startActivity(galleryImagesIntent);
                }
            }
        });

    }
}
