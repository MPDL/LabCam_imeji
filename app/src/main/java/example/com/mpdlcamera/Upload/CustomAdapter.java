package example.com.mpdlcamera.Upload;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;

import example.com.mpdlcamera.Model.LocalGallery;
import example.com.mpdlcamera.R;

/**
 * Created by kiran on 23.09.15.
 */
public class CustomAdapter extends ArrayAdapter<LocalGallery> {

    Context mContext;


    private final String LOG_TAG = CustomAdapter.class.getSimpleName();

    String prefOption;


    private ArrayList<LocalGallery> folderList;

    public CustomAdapter(Context context, int textViewResourceId,
                         ArrayList<LocalGallery> folderList) {
        super(context, textViewResourceId, folderList);
        this.mContext = context;
        this.folderList = new ArrayList<LocalGallery>();
        this.folderList.addAll(folderList);
    }

    private class ViewHolder {
        TextView textView;
        Switch fSwitch;

    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        ViewHolder holder = null;
        Log.v("ConvertView", String.valueOf(position));



        if (true) {
            LayoutInflater vi = (LayoutInflater) mContext.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.row, null);

            holder = new ViewHolder();
            holder.textView = (TextView) convertView.findViewById(R.id.folder);
            holder.fSwitch = (Switch) convertView.findViewById(R.id.fswitch);


            convertView.setTag(holder);

            holder.fSwitch.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Switch sw = (Switch) v;
                    LocalGallery folder = (LocalGallery) sw.getTag();

                    folder.setSelected(sw.isChecked());
                }
            });
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.fSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (!isChecked) {

                    RelativeLayout rl = (RelativeLayout) buttonView.getParent();
                    TextView tv = (TextView) rl.findViewById(R.id.folder);
                    String folder = tv.getText().toString();

                //    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
                    SharedPreferences settings = mContext.getSharedPreferences("folder", Context.MODE_PRIVATE);

                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString(folder, "Off");
                    editor.commit();


                } else if (isChecked) {

                   // SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
                    SharedPreferences settings = mContext.getSharedPreferences("folder", Context.MODE_PRIVATE);

                        RelativeLayout rl = (RelativeLayout) buttonView.getParent();
                        TextView tv = (TextView) rl.findViewById(R.id.folder);
                        String folder = tv.getText().toString();

                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(folder, "On");
                        editor.commit();


                }

            }


        });


        LocalGallery folder = folderList.get(position);
        holder.textView.setText(folder.getGallery());

        holder.fSwitch.setChecked(folder.isSelected());
        holder.fSwitch.setTag(folder);

        return convertView;

    }


}
