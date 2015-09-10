package example.com.mpdlcamera.Folder;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import example.com.mpdlcamera.Model.DataItem;
import example.com.mpdlcamera.Model.ImejiFolder;
import example.com.mpdlcamera.R;

/**
 * Created by allen on 27/08/15.
 */
public class FolderGridAdapter extends BaseAdapter {
    private Activity activity;
    private LayoutInflater inflater;
    private List<ImejiFolder> dataItems;
    private final String LOG_TAG = FolderGridAdapter.class.getSimpleName();

    public FolderGridAdapter(Activity c, List<ImejiFolder> dataItems) {
        this.activity = c;
        this.dataItems = dataItems;
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
            grid = inflater.inflate(R.layout.folder_grid_cell, parent, false);
        }else{
            grid = convertView;
        }

        ImageView imageView = (ImageView) grid.findViewById(R.id.collection_first_item);
        TextView title = (TextView) grid.findViewById(R.id.collection_title);

        if(dataItems.size()>0) {
            // getting item data for the row
            ImejiFolder collection = dataItems.get(position);

            if(collection.getItems() != null) {
                if (collection.getItems().size() > 0) {
                    DataItem m = collection.getItems().get(0);
                    Log.v(LOG_TAG, m.getWebResolutionUrlUrl());

                    Picasso.with(activity)
                            .load(m.getWebResolutionUrlUrl())
                            .into(imageView);
                }
            }
            title.setText(collection.getTitle());
        }
        return grid;

    }
}
