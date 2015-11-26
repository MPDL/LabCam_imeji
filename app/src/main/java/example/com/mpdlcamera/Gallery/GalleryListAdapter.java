package example.com.mpdlcamera.Gallery;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.ShapeDrawable;
import android.net.Uri;
import android.preference.PreferenceManager;
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
import example.com.mpdlcamera.SQLite.FileId;
import example.com.mpdlcamera.SQLite.MySQLiteHelper;

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
    private String CollectionId;
    boolean flag = false;
    Boolean matchGallery = false;
  //  SharedPreferences mPreferences;
    String status = "Off";
    TextView title;
    TextView mStatus;
    TextView upCount;
    String message;
    ProgressBar progressBar;
    ImageView imageView;
    Point size;


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


    /*
            reloads the view everytime the screen refreshes
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        size = new Point();
        display.getSize(size);


        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.gallery_list_cell, null);

        RelativeLayout relativeLayout = (RelativeLayout) convertView.findViewById(R.id.relOne);
        ShapeDrawable rectangleShape = new ShapeDrawable();

        Paint paint = rectangleShape.getPaint();

        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1);
        relativeLayout.setBackground(rectangleShape);

        imageView = (ImageView) convertView.findViewById(R.id.list_gallery_cell_thumbnail);
        title = (TextView) convertView.findViewById(R.id.list_item_gallery_title);
        mStatus = (TextView) convertView.findViewById(R.id.list_item_gallery_status);
        upCount = (TextView) convertView.findViewById(R.id.list_item_gallery_ucount);

        SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {

            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                System.out.println(key);
            }
        };

        SharedPreferences oPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        oPreferences.registerOnSharedPreferenceChangeListener(listener);

        if (size.x > size.y) {
            imageView.getLayoutParams().height = size.x /3;
        } else {
            imageView.getLayoutParams().height = size.y /3;
        }

       // for(int i=0; i<galleryList.size();i++) {
            // getting item data for the row
            Gallery gallery = galleryList.get(position);
            //Gallery gallery = galleryList.get(i);
            Log.v(LOG_TAG, gallery.getGalleryName());

        Thumbnail thumbnail = new Thumbnail(activity);
        String imagePath = null;

        if(!galleriesOne.contains(gallery.getGalleryName())) {
            flag = true;
            imagePath = thumbnail.getLatestImage(gallery,flag);
            galleriesOne.add(gallery.getGalleryName());
        }
        else {
            flag = false;
            imagePath = thumbnail.getLatestImage(gallery, flag);
        }


        //ListGalleries(gallery);

        if (gallery.getItems() != null) {
            if (gallery.getItems().size() > 0) {
                Gallery galleryTwo = gallery.getItems().get(0);

            }
        }
       // title.setText(gallery.getGalleryName());

        String galleryPath = gallery.getGalleryPath();

        String iPath = this.localPath;
        // File imgFile = new File(iPath);
        File imageFile = new File(imagePath);

        Uri uri = Uri.fromFile(imageFile);

        if(imageFile.exists()) {
            // imageView.setImageDrawable(draw);
            Picasso.with(activity)
                    .load(uri)
                    .resize(size.x / 2, size.y)
                    .centerInside()
                    .into(imageView);
        }
        SharedPreferences mPreferences = activity.getSharedPreferences("folder", Context.MODE_PRIVATE);
        SharedPreferences nPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences filePreferences = activity.getSharedPreferences("myPref", 0);
        CollectionId = filePreferences.getString("collectionID","");

        int uCount = getUploadingCount(gallery);
        int fCount = gallery.getCount();

        File directory = new File(galleryPath);
        int fCount2 = 0;

        for (File  file : directory.listFiles())
        {
            String fName = file.getName();
            if(!file.isDirectory() && (fName.endsWith("jpg") || fName.endsWith("jpeg") || fName.endsWith("png") || fName.endsWith("gif")))
                fCount2++;
        }
        if(mPreferences.contains(gallery.getGalleryName())) {

            status = mPreferences.getString(gallery.getGalleryName(), "");


            if (status.equalsIgnoreCase("On")) {

                message = "Activated";
                upCount.setVisibility(View.INVISIBLE);
//                if (nPreferences.getString("UploadStatus", "").equalsIgnoreCase("true")) {
//                    message = "Uploaded";
//                  //  progressBar.setVisibility(View.INVISIBLE);
//                    upCount.setVisibility(View.GONE);
//
//                } else {
//                    message = "Uploading";
//                    upCount.setText(gallery.getCount()-uCount + "file(s) are remaining");
//                 //   progressBar.setVisibility(View.INVISIBLE);
//                }
            } else {
                message = "Not Activated";
              //  progressBar.setVisibility(View.INVISIBLE);
                upCount.setVisibility(View.GONE);
            }


            if(gallery.getGalleryName().equalsIgnoreCase("Camera")) {
                title.setText(gallery.getGalleryName() + "(" + (fCount2) + ")");
            }
            else  title.setText(gallery.getGalleryName() + "(" + (fCount2) + ")");

            mStatus.setText(message);
           // upCount.setVisibility(View.INVISIBLE);
        }
        else {if(gallery.getGalleryName().equalsIgnoreCase("Camera")) {
            title.setText(gallery.getGalleryName() + "(" + (fCount2) + ")");
        }
            else title.setText(gallery.getGalleryName() + "(" + (fCount2) + ")");

            mStatus.setText("Not Activated");
            upCount.setVisibility(View.GONE);

        }

        return convertView;
    }

    /*
        returns the uploading count of the gallery
     */
    private int getUploadingCount(Gallery gallery) {

        Integer uploadCount = 0;
        String folderPath = gallery.getGalleryPath();
        File directory = new File(folderPath);
        File[] files = directory.listFiles();
        MySQLiteHelper db = new MySQLiteHelper(activity);
        SQLiteDatabase dBase = db.getWritableDatabase();

        List<FileId> fileIds = db.getAllFiles();

        for(File imageFile : files) {

            String fileName = imageFile.getName();
            String filePlusId = fileName + CollectionId;
            if(db.getFileStatus(filePlusId).equalsIgnoreCase("uploaded")){
                uploadCount++;
            }
        }

        return uploadCount;


    }

/*    private void simplify(Gallery gallery) {

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

        Uri uri = Uri.fromFile(imgFile1);

        if(imgFile1.exists()) {
          // imageView.setImageDrawable(draw);
               Picasso.with(activity)
                        .load(uri)
                       .resize(size.x / 2, size.y)
                       .centerInside()
                        .into(imageView);
        }
        SharedPreferences mPreferences = activity.getSharedPreferences("folder", Context.MODE_PRIVATE);
        SharedPreferences nPreferences = PreferenceManager.getDefaultSharedPreferences(activity);

        if(mPreferences.contains(gallery.getGalleryName())) {

            status = mPreferences.getString(gallery.getGalleryName(),"");

        }

        if(status.equalsIgnoreCase("On")) {

            if(nPreferences.getString("UStatus","").equalsIgnoreCase("true")) {
                gh="Uploaded....";
                progressBar.setVisibility(View.INVISIBLE);

            }
            else {
                gh="Uploading";
                progressBar.setVisibility(View.VISIBLE);
            }
        }
        else {
            gh="Not Activated";
            progressBar.setVisibility(View.INVISIBLE);
        }


        title.setText(gallery.getGalleryName() + "(" + gallery.getCount() + ")");
        mStatus.setText(gh);





    }*/




}
