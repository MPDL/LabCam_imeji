package de.mpg.mpdl.labcam.code.common.adapter;

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

import de.mpg.mpdl.labcam.Model.ImejiFolder;
import de.mpg.mpdl.labcam.R;
import de.mpg.mpdl.labcam.code.common.callback.CollectionIdInterface;
import de.mpg.mpdl.labcam.code.common.widget.DBConnector;
import de.mpg.mpdl.labcam.code.common.widget.Constants;
import de.mpg.mpdl.labcam.code.utils.PreferenceUtil;

import java.util.List;

/**
 * Created by allen on 12/10/15.
 */
public class SettingsListAdapter extends BaseAdapter {
    private Activity activity;
    private LayoutInflater inflater;
    private List<ImejiFolder> folderItems;
    int selectedPosition = -1;

    private String collectionId;
    private CollectionIdInterface ie;

    //selected collection

    public SettingsListAdapter(Activity activity, List<ImejiFolder> folderItems,CollectionIdInterface ie) {
        this.activity = activity;
        this.folderItems = folderItems;
        this.ie = ie;
        String lastCollectionId ="";

        String userId = PreferenceUtil.getString(activity, Constants.SHARED_PREFERENCES, Constants.USER_ID, "");
        String serverName = PreferenceUtil.getString(activity, Constants.SHARED_PREFERENCES, Constants.SERVER_NAME, "");

        try {
            lastCollectionId = DBConnector.getAuTask(userId, serverName).getCollectionId();
        }catch (NullPointerException e){
            e.printStackTrace();
        }

        if(lastCollectionId!=null && !lastCollectionId.equalsIgnoreCase("")){
            collectionId = lastCollectionId;}
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

    /*
        creates the view everytime the screen refreshes
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.v("getView");

        WindowManager windowManager = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.remote_settings_list_cell, null);

        TextView title = (TextView) convertView.findViewById(R.id.setting_item_cell_title);
        TextView user = (TextView) convertView.findViewById(R.id.setting_item_user);
        RadioButton checkBox = (RadioButton) convertView.findViewById(R.id.radio_button);
        TextView date = (TextView) convertView.findViewById(R.id.setting_item_date);

        System.out.println("Adapter collectionId" + " " + collectionId);

        if(folderItems.size()>0) {

            // getting item data for the row
            ImejiFolder collection = folderItems.get(position);

            //checkBox
            if(collection.getImejiId().equals(collectionId)){
                selectedPosition = position;
                ie.setCollectionId(selectedPosition,true);
                notifyDataSetChanged();
            }

            //title
            title.setText(collection.getTitle());

            // user
            if(collection.getContributors() != null) {
                user.setText(collection.getContributors().get(0).getCompleteName());
            }
            // date
            date.setText(String.valueOf(collection.getModifiedDate()).split("\\+")[0]);
        }

        checkBox.setChecked(position == selectedPosition);
        checkBox.setTag(position);


        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                selectedPosition = (Integer)view.getTag();

                ie.setCollectionId(selectedPosition,false);
                //onclick place radioButton to selected position
                collectionId = folderItems.get(selectedPosition).getImejiId();

                notifyDataSetChanged();

            }
        });


        return convertView;
    }

}
