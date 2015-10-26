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
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import example.com.mpdlcamera.R;

/**
 * Created by allen on 27/08/15.
 */
public class ImagesGridAdapter extends BaseAdapter {
    private Activity activity;
    private LayoutInflater inflater;
    private List<String> dataItems;
    private final String LOG_TAG = ImagesGridAdapter.class.getSimpleName();
    private HashMap<Integer, Boolean> mSelection = new HashMap<Integer, Boolean>();


    public ImagesGridAdapter(Activity c, List<String> dataItems) {
        this.activity = c;
        this.dataItems = dataItems;
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

    public void clearSelection() {
        mSelection = new HashMap<Integer, Boolean>();
        notifyDataSetChanged();
    }


    public int getCount() {
        return dataItems.size();
    }

    public Object getItem(int position) {
        return dataItems.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    // create a new ImageView for each item referenced by the Adapter
    //@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View grid;
        //View grid = super.getView(position, convertView, parent);//let the adapter handle setting up the row views


        WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        Point size = new Point();
        display.getSize(size);

        Log.v(size.x / 2 + " ", size.y / 2 + "");

        if(convertView==null){
            LayoutInflater inflater= (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            grid = inflater.inflate(R.layout.image_grid_cell, parent, false);
        }else{
            grid = convertView;
        }

        grid.setBackgroundColor(activity.getResources().getColor(android.R.color.background_light)); //default color

        if (mSelection.get(position) != null) {
            grid.setBackgroundColor(activity.getResources().getColor(android.R.color.holo_blue_dark));// this is a selected position so make it red
        }

        ImageView imageView = (ImageView) grid.findViewById(R.id.image_view);

        String filePath = dataItems.get(position);
        Uri uri = Uri.fromFile(new File(filePath));
        // /storage/emulated/0/DCIM/Camera/IMG_20150408_170256.jpg
        //             /sdcard/DCIM/Camera/IMG_20150408_170256.jpg
        Picasso.with(activity)
                .load(uri)
                .resize(size.x / 2, size.y)
                .centerInside()
                .into(imageView);

        return grid;
    }
}
