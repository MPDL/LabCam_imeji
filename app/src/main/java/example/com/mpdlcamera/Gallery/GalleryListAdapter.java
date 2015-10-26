package example.com.mpdlcamera.Gallery;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
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
import android.widget.RelativeLayout;
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
    private ArrayList<String> galleriesOne = new ArrayList<>();
    boolean flag = false;
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

        RelativeLayout relativeLayout = (RelativeLayout) convertView.findViewById(R.id.relOne);
        ShapeDrawable rectShape = new ShapeDrawable();

        Paint paint = rectShape.getPaint();

        paint.setColor(Color.GRAY);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        relativeLayout.setBackground(rectShape);


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
        String imPath = null;

            if(!galleriesOne.contains(gallery.getGalleryName())) {
                flag = true;
                imPath = thumbnail.getLatestImage(gallery,flag);
                galleriesOne.add(gallery.getGalleryName());
            }
        else {
                flag = false;
                imPath = thumbnail.getLatestImage(gallery, flag);
            }


        //ListGalleries(gallery);

            if (gallery.getItems() != null) {
                if (gallery.getItems().size() > 0) {
                    Gallery m = gallery.getItems().get(0);

                }
            }
            title.setText(gallery.getGalleryName());

            String gpath = gallery.getGalleryPath();

            String iPath = this.localPath;



           // File imgFile = new File(iPath);
            File imgFile1 = new File(imPath);

            imageView.setImageResource(0);
            Drawable draw = Drawable.createFromPath(imPath);
        draw = resize(draw);

        Bitmap b = ((BitmapDrawable)draw).getBitmap();

      //  File f = (File) b;

            if(imgFile1.exists()) {
                imageView.setImageDrawable(draw);
              /*  Picasso.with(activity)
                        .load(b)
                        .into(imageView); */

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

    private Drawable resize(Drawable image) {
        Bitmap bitmap = ((BitmapDrawable) image).getBitmap();
        Bitmap bitmapResized = Bitmap.createScaledBitmap(bitmap,
                (int) (bitmap.getWidth() * 0.5), (int) (bitmap.getHeight() * 0.5), false);
        return new BitmapDrawable(activity.getResources(), bitmapResized);
    }





}
