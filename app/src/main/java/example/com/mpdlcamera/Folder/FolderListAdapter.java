package example.com.mpdlcamera.Folder;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import example.com.mpdlcamera.Model.DataItem;
import example.com.mpdlcamera.Model.ImejiFolder;
import example.com.mpdlcamera.R;

/**
 * Created by allen on 06/04/15.
 */
public class FolderListAdapter extends BaseAdapter {
    private Activity activity;
    private LayoutInflater inflater;
    private List<ImejiFolder> folderItems;
    private final String LOG_TAG = FolderListAdapter.class.getSimpleName();

    public FolderListAdapter(Activity activity, List<ImejiFolder> folderItems) {
        this.activity = activity;
        this.folderItems = folderItems;
    }

    @Override
    public int getCount() {
        return folderItems.size();
    }

    @Override
    public Object getItem(int location) {
        return folderItems.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.folder_list_cell, null);

        ImageView imageView = (ImageView) convertView.findViewById(R.id.list_item_cell_thumbnail);
        TextView title = (TextView) convertView.findViewById(R.id.list_item_cell_title);
        TextView description = (TextView) convertView.findViewById(R.id.list_item_user);
        //TextView date = (TextView) convertView.findViewById(R.id.list_item_cell_date);


        if(folderItems.size()>0) {
            // getting item data for the row
            ImejiFolder collection = folderItems.get(position);

            if (collection.getItems() != null) {
                if (collection.getItems().size() > 0) {
                    DataItem m = collection.getItems().get(0);
                    Log.v(LOG_TAG, m.getWebResolutionUrlUrl());

                    Picasso.with(activity)
                            .load(m.getWebResolutionUrlUrl())
                            .into(imageView);
                }
            }
            title.setText(collection.getTitle());

            //title
            title.setText(collection.getTitle());

            // user
            description.setText(collection.getDescription());

            // date
            //date.setText(String.valueOf(m.getCreatedDate()).split("\\+")[0]);
        }
        return convertView;
    }

}
