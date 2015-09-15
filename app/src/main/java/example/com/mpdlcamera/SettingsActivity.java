package example.com.mpdlcamera;

import android.app.ListActivity;
import android.content.Context;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import android.provider.MediaStore;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;

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

        //Generate listView from ArrayList
        displayListView();



    }

    /*
    Displaying the ListView by using adapter
     */
    private void displayListView() {

        String[] albums = new String[]{MediaStore.Images.Media.BUCKET_DISPLAY_NAME};
        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;


        final ArrayList<String> folders = new ArrayList<String>();

        final ArrayList<FolderModel> folders1 = new ArrayList<FolderModel>();
        Cursor cur = getContentResolver().query(images, albums, null, null, null);

       // CharArrayBuffer buffer = new CharArrayBuffer(29);
        //cur.copyStringToBuffer(0,buffer);
        int co = cur.getColumnCount();
        String nam = cur.getColumnName(0);
        String[] nama = cur.getColumnNames();
        int rows = cur.getCount();

        checkSyncAll = (CheckBox) SettingsActivity.this.findViewById(R.id.syncAllCheck);

        //Logging the image count
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
        while (folderIterator.hasNext()) {
            FolderModel folderOne = new FolderModel(folderIterator.next(), false);
            folderList.add(folderOne);
        }



        switchAdapter = new CustomAdapter(this,
                R.layout.row, folderList);
        setListAdapter(switchAdapter);

        final ListView listView = getListView();

        final int size = getListAdapter().getCount();

       /* Switch mainSwitch = (Switch) findViewById(R.id.fswitch);

        mainSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                System.out.println("nobds");
            }
        });
 */
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
        return (((CustomAdapter) getListAdapter()).getItem(position));
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
                LayoutInflater vi = (LayoutInflater) getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.row, null);

                holder = new ViewHolder();
                holder.textView = (TextView) convertView.findViewById(R.id.folder);
                holder.fSwitch = (Switch) convertView.findViewById(R.id.fswitch);
                convertView.setTag(holder);

                holder.fSwitch.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Switch sw = (Switch) v;
                        FolderModel folder = (FolderModel) sw.getTag();

                        folder.setSelected(sw.isChecked());
                    }
                });
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.fSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    RelativeLayout rl = (RelativeLayout) buttonView.getParent();
                    TextView tv = (TextView) rl.findViewById(R.id.folder);
                    String folder = tv.getText().toString();

                    Toast.makeText(getApplicationContext(), folder + "is now synced", Toast.LENGTH_LONG).show();

                    Uri uri;
                    Cursor cursor;
                    int column_index_data, column_index_folder_name,column_index_file_name;
                    ArrayList<String> listOfAllImages = new ArrayList<String>();
                    String absolutePathOfImage = null;
                    String absoluteFileName = null;
                    String absoluteFolderName = null;
                    uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

                    String[] projection = { MediaStore.MediaColumns.DATA,
                            MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.DISPLAY_NAME};

                    cursor = getContentResolver().query(uri, projection, null,
                            null, null);

                    column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);



                    column_index_folder_name = cursor
                            .getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);





                    while (cursor.moveToNext()) {
                        absolutePathOfImage = cursor.getString(column_index_data);

                        absoluteFolderName = cursor.getString(column_index_folder_name);

                        if(absoluteFolderName.equalsIgnoreCase(folder)) {


                            listOfAllImages.add(absolutePathOfImage);
                        }
                    }


                    for(String juz : listOfAllImages) {

                        Bitmap bm = BitmapFactory.decodeFile(juz);

                    }











                }
            });

            FolderModel folder = folderList.get(position);
            holder.textView.setText(folder.getFolder());

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


  /*  public void switchActivity(View view) {

        System.out.println("coming here");
        Switch sw = (Switch) view;
        sw.setOnCheckedChangeListener(null);
        if (sw.isChecked()) {
            RelativeLayout linearOne = (RelativeLayout) sw.getParent();
            TextView tv = (TextView) linearOne.findViewById(R.id.folder);

            String folder = tv.getText().toString();

            Toast.makeText(getApplicationContext()," you have selected  " + folder, Toast.LENGTH_LONG).show();


            // Perform actions when the switch is on
        } else {
            sw.setOnCheckedChangeListener(null);

            // Perform actions when the switch is off
        }

    }*/
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            
}


