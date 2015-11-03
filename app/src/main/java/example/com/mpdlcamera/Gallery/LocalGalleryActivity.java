package example.com.mpdlcamera.Gallery;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedHashSet;

import example.com.mpdlcamera.Auth.LoginActivity;
import example.com.mpdlcamera.Folder.MainActivity;
import example.com.mpdlcamera.Folder.UploadService;
import example.com.mpdlcamera.Model.Gallery;
import example.com.mpdlcamera.R;
import example.com.mpdlcamera.Settings.SettingsActivity;
import example.com.mpdlcamera.Upload.UploadResultReceiver;

/**
 * Created by kiran on 22.10.15.
 */

public class LocalGalleryActivity extends AppCompatActivity implements UploadResultReceiver.Receiver {

    Toolbar toolbar;
    private View rootView;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle drawerToggle;
    CollapsingToolbarLayout collapsingToolbarLayout;
    private NavigationView navigation;
    GridView gridView;
    SharedPreferences preferences;
    private SharedPreferences mPrefs;


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

        UploadResultReceiver mReceiver = new UploadResultReceiver(new Handler());
        mReceiver.setReceiver(this);
        Intent intent = new Intent(this, UploadService.class);
        intent.putExtra("receiver", mReceiver);
        this.startService(intent);

    }


    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case 0:

                setProgressBarIndeterminateVisibility(true);
                break;
            case 1:
                /* Hide progress & extract result from bundle */
                setProgressBarIndeterminateVisibility(false);

                //  String[] results = resultData.getStringArray("result");

                /* Update ListView with result */
                //ArrayAdapter arrayAdapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_2, results);
                //listView.setAdapter(arrayAdapter);
              //  Toast.makeText(this, "Files are synced", Toast.LENGTH_LONG).show();



                mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

                SharedPreferences.Editor e = mPrefs.edit();
                e.putString("UStatus","true");
                e.commit();
                adapter.notifyDataSetChanged();

//                Intent showLocalImageIntent = new Intent(activity, LocalGalleryActivity.class);
//                startActivity(showLocalImageIntent);



                if(mPrefs.contains("L_A_U")) {

                    if(mPrefs.getBoolean("L_A_U", true)) {

                        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                        View popupView = inflater.inflate(R.layout.logout_confirm, null);
                        final PopupWindow popupWindow = new PopupWindow(
                                popupView,
                                550,
                                300);
                        popupWindow.setFocusable(true);
                        popupWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                        popupWindow.setAnimationStyle(R.style.AnimationPopup);

                        Button yes = (Button) popupView.findViewById(R.id.buttonYes);
                        Button no = (Button) popupView.findViewById(R.id.buttonNo);
                        popupWindow.showAtLocation(findViewById(R.id.navigation), Gravity.CENTER, 0, 0);

                        yes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                finish();
                                Intent loginIntent = new Intent(activity, LoginActivity.class);
                                startActivity(loginIntent);
                            }
                        });

                        no.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                popupWindow.dismiss();
                            }
                        });

                    }
                }

                break;
            case 2:
                /* Handle the error */
                String error = resultData.getString(Intent.EXTRA_TEXT);
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                break;
        }
    }
    /*@Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        adapter.notifyDataSetChanged();
    }*/
}
