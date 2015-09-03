package example.com.mpdlcamera.Folder;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

import example.com.mpdlcamera.Model.ImejiFolder;

/**
 * Created by allen on 06/04/15.
 */
public class FolderListAdapter extends BaseAdapter {
    private Activity activity;
    private LayoutInflater inflater;
    private List<ImejiFolder> dataItems;

    public FolderListAdapter(Activity activity, List<ImejiFolder> dataItems) {
        this.activity = activity;
        this.dataItems = dataItems;
    }

    @Override
    public int getCount() {
        return dataItems.size();
    }

    @Override
    public Object getItem(int location) {
        return dataItems.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

//        if (inflater == null)
//            inflater = (LayoutInflater) activity
//                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        if (convertView == null)
//            convertView = inflater.inflate(R.layout.list_item_cell, null);
//
//        ImageView imageView = (ImageView) convertView.findViewById(R.id.list_item_cell_thumbnail);
//        TextView title = (TextView) convertView.findViewById(R.id.list_item_cell_title);
//        TextView fileName = (TextView) convertView.findViewById(R.id.list_item_user);
//        TextView date = (TextView) convertView.findViewById(R.id.list_item_cell_date);
//
//        // getting item data for the row
//        ImejiFolder m = dataItems.get(position);
//
//        // thumbnail image
//        Picasso.with(activity)
//                .load(m.getThumbnailUrl())
//                .into(imageView);
//        //Log.v("getThumbnailUrl ",m.getThumbnailUrl());
//
//        // title
//        title.setText(m.getMetaDataLocal().getTitle());
//
//        // user
//        //artist.setText(m.getCreatedBy().getFamilyName());
//        fileName.setText(m.getMetaDataLocal().getCreator());
//
//        // date
//        date.setText(String.valueOf(m.getCreatedDate()).split("\\+")[0]);

        return convertView;
    }

}
