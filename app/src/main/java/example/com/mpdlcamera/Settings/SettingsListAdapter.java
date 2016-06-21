package example.com.mpdlcamera.Settings;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
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
import example.com.mpdlcamera.UploadActivity.CollectionIdInterface;
import example.com.mpdlcamera.Utils.DeviceStatus;

/**
 * Created by allen on 12/10/15.
 */
public class SettingsListAdapter extends BaseAdapter {
    private Activity activity;
    private LayoutInflater inflater;
    private List<ImejiFolder> folderItems;
    private final String LOG_TAG = SettingsListAdapter.class.getSimpleName();
    int selectedPosition = -1;
    private SharedPreferences mPrefs;

    private String collectionId;
    private CollectionIdInterface ie;

    //selected collection

    public SettingsListAdapter(Activity activity, List<ImejiFolder> folderItems,CollectionIdInterface ie) {
        this.activity = activity;
        this.folderItems = folderItems;
        this.ie = ie;
        String lastCollectionId ="";
        mPrefs = activity.getSharedPreferences("myPref", 0);
        String userId = mPrefs.getString("userId", "");
        try{
            lastCollectionId = DeviceStatus.getAuTask(userId).getCollectionId();
        }
        catch(Exception e){
        }
        if(lastCollectionId!=null){
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
        mPrefs =  activity.getSharedPreferences("myPref", 0);


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
                ie.setCollectionId(selectedPosition);
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

                ie.setCollectionId(selectedPosition);
                //onclick place radioButton to selected position
                collectionId = folderItems.get(selectedPosition).getImejiId();

                notifyDataSetChanged();

                //collectionID and remoteServer

//                SharedPreferences.Editor editor = mPrefs.edit();
//                editor.putString("collectionID", folderItems.get(selectedPosition).id);
//                editor.putString("remoteServer", folderItems.get(selectedPosition).getTitle());

//                System.out.println(LOG_TAG + " selectedPosition on click " + selectedPosition);
//                System.out.println(preferences.getString("collectionID","default") + selectedPosition);

//                editor.apply();


            }
        });


        return convertView;
    }

}
