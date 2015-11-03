package example.com.mpdlcamera.Gallery;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.net.Uri;
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

import example.com.mpdlcamera.Model.Gallery;
import example.com.mpdlcamera.R;
import example.com.mpdlcamera.SQLite.FileId;
import example.com.mpdlcamera.SQLite.MySQLiteHelper;

/**
 * Created by kiran on 30.10.15.
 */
public class GalleryGridAdapter extends BaseAdapter {

    private Activity activity;
    private Context context;
    private List<String> galleryItems;
    private HashMap<Integer, Boolean> mSelection = new HashMap<Integer, Boolean>();


    public GalleryGridAdapter(Context context) {
        this.context = context;
    }
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


    public GalleryGridAdapter(Activity activity, List<String> galleryItems, Context context) {
        this.activity = activity;
        this.galleryItems = galleryItems;
        this.context = context;
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View grid;
        WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();


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

        Button button = (Button) grid.findViewById(R.id.cloud);
        String filep = (String) this.getItem(position);

        File dir = new File(filep);
        String file = dir.getName();

        MySQLiteHelper db = new MySQLiteHelper(context);

        boolean b = db.getFile(file);
        //boolean b = true;

        List<FileId> fileIds = db.getAllFiles();

        if(!b) {

                button.setVisibility(View.INVISIBLE);
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
