package example.com.mpdlcamera.Gallery;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import example.com.mpdlcamera.Model.Gallery;
import example.com.mpdlcamera.R;

/**
 * Created by kiran on 22.10.15.
 */

public class GalleryListAdapter extends BaseAdapter {

    private final String LOG_TAG = GalleryListAdapter.class.getSimpleName();
    private Activity activity;

    // all albums
    private List<Gallery> galleryList;
    private LayoutInflater inflater;
    private ArrayList<Gallery> galleries = new ArrayList<Gallery>();
    private String localPath;
    private ArrayList<String> galleriesOne = new ArrayList<>();
    private String CollectionId;
    // flag for what?
    boolean flag = false;

  //  SharedPreferences mPreferences;
    TextView title;
    ImageView imageView;
    Point size;

    // album positionSet
    public Set<Integer> albumPositionSet = new HashSet<>();

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
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
            reloads the view every time the screen refreshes
     */
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        //get display size
        WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        size = new Point();
        display.getSize(size);

        // inflate layout
        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.gallery_list_cell, null);

        // didn't understand what's here
        RelativeLayout relativeLayout = (RelativeLayout) convertView.findViewById(R.id.relOne);
        ShapeDrawable rectangleShape = new ShapeDrawable();

        Paint paint = rectangleShape.getPaint();

        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1);
        relativeLayout.setBackground(rectangleShape);

        // view element declare
        imageView = (ImageView) convertView.findViewById(R.id.list_gallery_cell_thumbnail);
        title = (TextView) convertView.findViewById(R.id.list_item_gallery_title);

        // onSharedPreferenceChanged?
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

        // get current album
        Gallery gallery = galleryList.get(position);

        Log.v(LOG_TAG, gallery.getGalleryName());

        Thumbnail thumbnail = new Thumbnail(activity);
        String imagePath;

        // why not
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


        File directory = new File(galleryPath);
        int fCount2 = 0;

        for (File  file : directory.listFiles())
        {
            String fName = file.getName();
            if(!file.isDirectory() && (fName.endsWith("jpg") || fName.endsWith("jpeg") || fName.endsWith("png") || fName.endsWith("gif")))
                fCount2++;
        }
        if(mPreferences.contains(gallery.getGalleryName())) {

            if(gallery.getGalleryName().equalsIgnoreCase("Camera")) {
                title.setText(gallery.getGalleryName() + " (" + (fCount2) + ")");
            }
            else  title.setText(gallery.getGalleryName() + " (" + (fCount2) + ")");

           // upCount.setVisibility(View.INVISIBLE);
        }
        else {if(gallery.getGalleryName().equalsIgnoreCase("Camera")) {
            title.setText(gallery.getGalleryName() + " (" + (fCount2) + ")");
        }
            else title.setText(gallery.getGalleryName() + " (" + (fCount2) + ")");

        }

        if (onItemClickListener!=null){
            convertView.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     onItemClickListener.onItemClick(v, position);
                 }
             });
            convertView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onItemClickListener.onItemLongClick(v,position);
                    return false;
                }
            });
        }

        // checkMark
        ImageView checkMark = (ImageView) convertView.findViewById(R.id.album_check_mark);

        if(albumPositionSet.contains(position)){
            checkMark.setVisibility(View.VISIBLE);
        }else {
            checkMark.setVisibility(View.GONE);
        }

        return convertView;
    }


    public void setPositionSet(Set<Integer> positionSet){
        this.albumPositionSet = positionSet;
        Log.e("albumPositionSet",positionSet.size()+"");
        notifyDataSetChanged();
    }

    public interface OnItemClickListener{
        void onItemClick(View view, int position);
        void onItemLongClick(View view,int position);
    }

}
