package example.com.mpdlcamera.ItemDetails;

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
import example.com.mpdlcamera.R;
import example.com.mpdlcamera.Utils.camPicassoLoader;

/**
 * Created by allen on 27/08/15.
 */
public class ItemsGridAdapter extends BaseAdapter {
    private Activity activity;
    private LayoutInflater inflater;
    private List<DataItem> dataItems;
    private final String LOG_TAG = ItemsGridAdapter.class.getSimpleName();

    public ItemsGridAdapter(Activity activity, List<DataItem> dataItems) {
        this.activity = activity;
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


        WindowManager windowManager = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();

        Point size = new Point();
        display.getSize(size);

        Log.v(size.x / 2 + " ", size.y / 2 + "");

        if(convertView==null){
            LayoutInflater inflater= (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            grid = inflater.inflate(R.layout.item_grid_cell, parent, false);
        }else{
            grid = convertView;
        }

        ImageView imageView = (ImageView) grid.findViewById(R.id.item_image);
        TextView title = (TextView) grid.findViewById(R.id.item_title);

        DataItem dataItem = dataItems.get(position);

        Picasso myPicasso = new Picasso.Builder(activity).downloader(new camPicassoLoader(activity)).build();
        myPicasso.load(dataItem.getWebResolutionUrlUrl())
                .resize(size.x / 2, size.y)
                .centerInside()
                .error(R.drawable.error_alert).into(imageView);
        
        title.setText(dataItem.getFilename());

        return grid;
    }
}
