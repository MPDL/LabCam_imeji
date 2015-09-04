package example.com.mpdlcamera;

import android.app.ListActivity;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Switch;

import java.util.ArrayList;
import java.util.LinkedHashSet;


/**
 * Created by kiran on 25.09.15.
 */

public class SettingsActivity extends ListActivity {

    private CheckBox checkSyncAll;
    private Switch switchFolder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        String[] albums = new String[]{MediaStore.Images.Media.BUCKET_DISPLAY_NAME};
        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;


        final ArrayList<String> folders = new ArrayList<String>();
        Cursor cur = getContentResolver().query(images,albums, null,null, null);

        switchFolder = (Switch) findViewById(R.id.switchf);

        checkSyncAll = (CheckBox) SettingsActivity.this.findViewById(R.id.syncAllCheck);
       // checkSyncAll.setOnCheckedChangeListener(this);

        Log.i("ListingImages", " query count=" + cur.getCount());

        if (cur.moveToFirst()) {
            String album;
            int albumLocation = cur.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);

            do{
                album = cur.getString(albumLocation);
                folders.add(album);
                Log.i("ListingImages", " album=" + album);
            }while (cur.moveToNext());
        }

        ArrayList<String> imageFolders = new ArrayList<String>();
        imageFolders = new ArrayList<String>(new LinkedHashSet<String>(folders));

        ArrayList<String> sumne = new ArrayList<String>();
        sumne.add("whatsapp");
        sumne.add("facebook");

        this.setListAdapter(new ArrayAdapter<String>(this, R.layout.row, R.id.folder,sumne ));


    }




}
