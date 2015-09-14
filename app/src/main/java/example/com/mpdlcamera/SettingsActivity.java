package example.com.mpdlcamera;

import android.app.ListActivity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;


/**
 * Created by kiran on 25.09.15.
 */


public class SettingsActivity extends ListActivity {

    CustomAdapter switchAdapter = null;

    private CheckBox checkSyncAll;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Generate list View from ArrayList
        displayListView();



    }

    private void displayListView() {

        String[] albums = new String[]{MediaStore.Images.Media.BUCKET_DISPLAY_NAME};
        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;


        final ArrayList<String> folders = new ArrayList<String>();

        final ArrayList<FolderModel> folders1 = new ArrayList<FolderModel>();
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

        ArrayList<FolderModel> folderList = new ArrayList<FolderModel>();


        Iterator<String> folderIterator = imageFolders.iterator();
        while(folderIterator.hasNext()) {
            FolderModel folderOne = new FolderModel(folderIterator.next(),false);
            folderList.add(folderOne);
        }




        switchAdapter = new CustomAdapter(this,
                R.layout.row, folderList);
        setListAdapter(switchAdapter);

        final ListView listView = getListView();

        final int size = getListAdapter().getCount();

        checkSyncAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkSyncAll.isChecked()) {


                    for (int i = 0; i < size; i++) {
                        RelativeLayout RelativeOne = (RelativeLayout) getViewByPosition(i, listView);
                        Switch mSwitch = (Switch) RelativeOne.findViewById(R.id.fswitch);
                        mSwitch.setChecked(true);

                    }
                }
                if (!checkSyncAll.isChecked()) {

                    for (int i = 0; i < size; i++) {
                        RelativeLayout RelativeOne = (RelativeLayout) getViewByPosition(i, listView);
                        Switch mSwitch = (Switch) RelativeOne.findViewById(R.id.fswitch);
                        mSwitch.setChecked(false);

                    }
                }

            }
        });
    }


    private FolderModel getModel(int position) {
        return(((CustomAdapter)getListAdapter()).getItem(position));
    }


    private class CustomAdapter extends ArrayAdapter<FolderModel> {

        private ArrayList<FolderModel> folderList;

        public CustomAdapter(Context context, int textViewResourceId,
                                                ArrayList<FolderModel> folderList) {
            super(context, textViewResourceId, folderList);
            this.folderList = new ArrayList<FolderModel>();
            this.folderList.addAll(folderList);
        }

        private class ViewHolder {
            TextView textView;
            Switch fSwitch;

        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder = null;
            Log.v("ConvertView", String.valueOf(position));

            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.row, null);

                holder = new ViewHolder();
                holder.textView = (TextView) convertView.findViewById(R.id.folder);
                holder.fSwitch = (Switch) convertView.findViewById(R.id.fswitch);
                convertView.setTag(holder);

                holder.fSwitch.setOnClickListener( new View.OnClickListener() {
                    public void onClick(View v) {
                        Switch sw = (Switch) v ;
                        FolderModel country = (FolderModel) sw.getTag();
                        Toast.makeText(getApplicationContext(),
                                "Clicked on Checkbox: " + sw.getText() +
                                        " is " + sw.isChecked(),
                                Toast.LENGTH_LONG).show();
                        country.setSelected(sw.isChecked());
                    }
                });
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }

            FolderModel folder = folderList.get(position);
            holder.textView.setText(" (" +  folder.getFolder() + ")");
            //holder.name.setText(country.getName());
            holder.fSwitch.setChecked(folder.isSelected());
            holder.fSwitch.setTag(folder);

            return convertView;

        }
    }

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


                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            
}


