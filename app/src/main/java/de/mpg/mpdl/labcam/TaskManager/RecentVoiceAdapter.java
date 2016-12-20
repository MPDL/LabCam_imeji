package de.mpg.mpdl.labcam.TaskManager;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import de.mpg.mpdl.labcam.Model.LocalModel.Note;
import de.mpg.mpdl.labcam.Model.LocalModel.Voice;
import de.mpg.mpdl.labcam.R;

/**
 * Created by Yunqing on 20.12.16.
 */

public class RecentVoiceAdapter extends BaseAdapter {

    private LayoutInflater inflater;

    private Activity activity;
    private List<Voice> voiceList;


    public RecentVoiceAdapter(Activity activity, List<Voice> voiceList) {
        this.activity = activity;
        this.voiceList = voiceList;
    }

    @Override
    public int getCount() {
        return voiceList.size();
    }

    @Override
    public Object getItem(int i) {
        return voiceList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }


    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (view == null)
            view = inflater.inflate(R.layout.recent_voice_list_cell, null);

        TextView taskName = (TextView) view.findViewById(R.id.voice_list_cell_text);

        String taskInfo = voiceList.get(i).getCreateTime();

        taskName.setText(taskInfo);

        return view;

    }
}
