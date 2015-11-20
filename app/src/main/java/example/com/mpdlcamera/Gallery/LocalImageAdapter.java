package example.com.mpdlcamera.Gallery;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import example.com.mpdlcamera.R;
import example.com.mpdlcamera.SQLite.FileId;
import example.com.mpdlcamera.SQLite.MySQLiteHelper;

/**
 *  Created by allen on 27/08/15.
 */

public class LocalImageAdapter extends BaseAdapter {

    private Activity activity;
    private Boolean isActiveFolder;
    private List<String> galleryItems;
    private HashMap<Integer, Boolean> mSelection = new HashMap<Integer, Boolean>();


    public void setNewSelection(int position, boolean value) {
        mSelection.put(position, value);
        notifyDataSetChanged();
    }

    public boolean isPositionChecked(int position) {
        Boolean result = mSelection.get(position);
        return result == null ? false : result;
    }

    public Set<Integer> getCurrentCheckedPosition() {
        return mSelection.keySet();
    }

    public void removeSelection(int position) {
        mSelection.remove(position);
        notifyDataSetChanged();
    }


    public LocalImageAdapter(Activity activity, List<String> galleryItems, Boolean isActiveFolder) {
        this.activity = activity;
        this.galleryItems = galleryItems;
        this.isActiveFolder = isActiveFolder;
    }

    public void clearSelection() {
        mSelection = new HashMap<Integer, Boolean>();
        notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return galleryItems.size();
    }

    @Override
    public Object getItem(int position) {
        return galleryItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /*
        reloads the view everytime the screen refreshes
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View grid;
        WindowManager windowManager = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();


        Point size = new Point();
        display.getSize(size);

        Log.v(size.x / 2 + " ", size.y / 2 + "");

        if(convertView==null){
            LayoutInflater inflater= (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            grid = inflater.inflate(R.layout.gallery_grid_cell, parent, false);
        }else{
            grid = convertView;
        }

        Button buttonCloud = (Button) grid.findViewById(R.id.cloud);
        Button buttonUploading = (Button) grid.findViewById(R.id.uploading);
        String positionFile = (String) this.getItem(position);
        //buttonUploading.setVisibility(View.INVISIBLE);

        File directory = new File(positionFile);
        String file = directory.getName();
        SharedPreferences preferences = activity.getSharedPreferences("myPref", 0);
        String CollectionId = preferences.getString("collectionID","");
        String fileCollectionName = file + CollectionId;

        MySQLiteHelper db = new MySQLiteHelper(activity);

        boolean flag = db.getFile(fileCollectionName);
        //boolean b = true;

        List<FileId> fileIds = db.getAllFiles();

        String status = null;
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(activity);
        if(pref.contains("status")) {
            status = pref.getString("status","");
        }

        if(isActiveFolder) {
            //not uploaded for active folder
            if (!flag) {
                if (status.equalsIgnoreCase("manual")) {
                    buttonCloud.setVisibility(View.GONE);
                    buttonUploading.setVisibility(View.GONE);
                } else {
                    buttonCloud.setVisibility(View.GONE);
                    buttonUploading.setVisibility(View.VISIBLE);
                }
            } else{ //uploaded
                buttonCloud.setVisibility(View.VISIBLE);
                buttonUploading.setVisibility(View.GONE);
            }
        }else {
            //not uploaded for non-active folder
            if (!flag) {
                buttonCloud.setVisibility(View.GONE);
                buttonUploading.setVisibility(View.GONE);
            } else { //uploaded
                buttonCloud.setVisibility(View.VISIBLE);
                buttonUploading.setVisibility(View.GONE);
            }
        }

        grid.setBackgroundColor(activity.getResources().getColor(android.R.color.background_light));


        if (mSelection.get(position) != null) {
            grid.setBackgroundColor(activity.getResources().getColor(android.R.color.holo_blue_dark));
        }

        ImageView imageView = (ImageView) grid.findViewById(R.id.image_view);


        String filePath = galleryItems.get(position);
        Uri uri = Uri.fromFile(new File(filePath));

        Picasso.with(activity)
                .load(uri)
                .resize(size.x / 2, size.y)
                .centerInside()
                .into(imageView);

        return grid;
    }
}
