package example.com.mpdlcamera.Settings;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import com.activeandroid.util.Log;

import java.util.List;

import example.com.mpdlcamera.Model.ImejiFolder;
import example.com.mpdlcamera.R;

/**
 * Created by allen on 06/10/15.
 */
public class SettingsListAdapter extends BaseAdapter {
    private Activity activity;
    private LayoutInflater inflater;
    private List<ImejiFolder> folderItems;
    private final String LOG_TAG = SettingsListAdapter.class.getSimpleName();

    public SettingsListAdapter(Activity activity, List<ImejiFolder> folderItems) {
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

        WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        //Log.v(size.x  + " width", size.y  + " height");

        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.remote_settings_list_cell, null);

        TextView title = (TextView) convertView.findViewById(R.id.setting_item_cell_title);
        TextView user = (TextView) convertView.findViewById(R.id.setting_item_user);
        RadioButton checkBox = (RadioButton) convertView.findViewById(R.id.radio_button);
        TextView date = (TextView) convertView.findViewById(R.id.setting_item_date);


        if(folderItems.size()>0) {
            // getting item data for the row
            ImejiFolder collection = folderItems.get(position);

            //title
            title.setText(collection.getTitle());
            Log.v(LOG_TAG,collection.getTitle() );

            // user
            if(collection.getContributors() != null) {
                user.setText(collection.getContributors().get(0).getCompleteName());
            }
            // date
            date.setText(String.valueOf(collection.getModifiedDate()).split("\\+")[0]);
        }
        return convertView;
    }

}
