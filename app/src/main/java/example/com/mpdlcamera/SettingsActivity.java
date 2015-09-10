package example.com.mpdlcamera;

import android.app.ListActivity;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedHashSet;


/**
 * Created by kiran on 25.09.15.
 */

public class SettingsActivity extends ListActivity {

    private CheckBox checkSyncAll;
    //private Switch switchFolder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        String[] albums = new String[]{MediaStore.Images.Media.BUCKET_DISPLAY_NAME};
        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;


        final ArrayList<String> folders = new ArrayList<String>();
        Cursor cur = getContentResolver().query(images, albums, null, null, null);


        checkSyncAll = (CheckBox) SettingsActivity.this.findViewById(R.id.syncAllCheck);

        //Logging the imaeg count
        Log.i("ListingImages", " query count=" + cur.getCount());

        if (cur.moveToFirst()) {
            String album;
            int albumLocation = cur.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);

            do {
                album = cur.getString(albumLocation);
                folders.add(album);
                Log.i("ListingImages", " album=" + album);
            } while (cur.moveToNext());
        }

        ArrayList<String> imageFolders = new ArrayList<String>();
        imageFolders = new ArrayList<String>(new LinkedHashSet<String>(folders));


        this.setListAdapter(new ArrayAdapter<String>(this, R.layout.row, R.id.folder, imageFolders));


        final ListView listView = getListView();

        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);

        final int size = getListAdapter().getCount();


        for (int i = 0; i < size; i++) {
            LinearLayout linearOne = (LinearLayout) getViewByPosition(i, listView);
            LinearLayout linearTwo = (LinearLayout) linearOne.getChildAt(1);
            Switch mSwitch = (Switch) linearTwo.getChildAt(0);
            mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    View v = (View) buttonView.getParent();
                    TextView textView = null;
                    textView = (TextView) v.findViewById(R.id.folder);
                    String folderName = textView.getText().toString();

                }
            });
        }
        /*
        Sync All Check box functionality
         */
        checkSyncAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkSyncAll.isChecked()) {


                    for (int i = 0; i < size; i++) {
                        LinearLayout linearOne = (LinearLayout) getViewByPosition(i, listView);
                        LinearLayout linearTwo = (LinearLayout) linearOne.getChildAt(1);
                        Switch mSwitch = (Switch) linearTwo.getChildAt(0);
                        mSwitch.setChecked(true);
                    }
                }
                if (!checkSyncAll.isChecked()) {

                    for (int i = 0; i < size; i++) {
                        LinearLayout linearOne = (LinearLayout) getViewByPosition(i, listView);
                        LinearLayout linearTwo = (LinearLayout) linearOne.getChildAt(1);
                        Switch mSwitch = (Switch) linearTwo.getChildAt(0);
                        mSwitch.setChecked(false);
                    }
                }

            }
        });


    }

    /*

     */
    public View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

    /*
    OnClick method for Switch button
     */

    public void switchActivity(View view) {

        Switch sw = (Switch) view;
        sw.setOnCheckedChangeListener(null);
        if (sw.isChecked()) {
            LinearLayout linearOne = (LinearLayout) sw.getParent();
            LinearLayout linearTwo = (LinearLayout) linearOne.getParent();
            TextView txtView = (TextView) linearTwo.getChildAt(0);


            // Perform actions when the switch is on
        } else {
            sw.setOnCheckedChangeListener(null);

            // Perform actions when the switch is off
        }

    }
}
