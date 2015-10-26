package example.com.mpdlcamera.Gallery;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Point;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import example.com.mpdlcamera.Model.Gallery;
import example.com.mpdlcamera.R;

/**
 * Created by kiran on 22.10.15.
 */

public class GalleryListAdapter extends BaseAdapter {

    private Activity activity;
    private List<Gallery> galleryList;
    private final String LOG_TAG = GalleryListAdapter.class.getSimpleName();
    private LayoutInflater inflater;
    private ArrayList<Gallery> galleries = new ArrayList<Gallery>();
    private String localPath;
    Boolean matchGallery = false;
    SharedPreferences mPreferences;
    String status = "Off";


    private void setLocalPath(String path) {
        this.localPath = path;
    }

    public GalleryListAdapter(Activity activity) {
        this.activity = activity;
    }

    public GalleryListAdapter(Activity activity, List<Gallery> galleryList) {
        this.activity = activity;
        this.galleryList = galleryList;
    }

    @Override
    public int getCount() {
        return galleryList.size();
    }

    @Override
    public Object getItem(int location) {
        return galleryList.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);


        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.gallery_list_cell, null);

        ImageView imageView = (ImageView) convertView.findViewById(R.id.list_gallery_cell_thumbnail);
        TextView title = (TextView) convertView.findViewById(R.id.list_item_gallery_title);
        TextView mStatus = (TextView) convertView.findViewById(R.id.list_item_gallery_status);
        ProgressBar progressBar = (ProgressBar) convertView.findViewById(R.id.progBar);



        if (size.x > size.y) {
            imageView.getLayoutParams().height = 1 * size.y /4;
        } else {
            imageView.getLayoutParams().height = 1 * size.y /4;
        }

       // for(int i=0; i<galleryList.size();i++) {
            // getting item data for the row
            Gallery gallery = galleryList.get(position);
            //Gallery gallery = galleryList.get(i);
            Log.v(LOG_TAG, gallery.getGalleryName());
            Thumbnail thumbnail = new Thumbnail(activity);
            String imPath = thumbnail.getLatestImage(gallery);
            //ListGalleries(gallery);

            if (gallery.getItems() != null) {
                if (gallery.getItems().size() > 0) {
                    Gallery m = gallery.getItems().get(0);

                }
            }
            title.setText(gallery.getGalleryName());

            String iPath = this.localPath;


           // File imgFile = new File(iPath);
            File imgFile1 = new File(imPath);
            if(imgFile1.exists()) {
                Picasso.with(activity)
                        .load(imgFile1)
                        .into(imageView);

            }
        mPreferences = PreferenceManager.getDefaultSharedPreferences(activity);

        if(mPreferences.contains(gallery.getGalleryName())) {

            status = mPreferences.getString(gallery.getGalleryName(),"");



        }

        if(status.equalsIgnoreCase("On")) {

            if(mPreferences.getString("UStatus","").equalsIgnoreCase("true")) {
                mStatus.setText("Uploaded");
                progressBar.setVisibility(View.GONE);

            }
            else
                mStatus.setText("Uploading");


        }
        else
            mStatus.setText("Not Activated");
             progressBar.setVisibility(View.GONE);


        title.setText(gallery.getGalleryName() + "(" + gallery.getCount() + ")");


        return convertView;
    }





}
