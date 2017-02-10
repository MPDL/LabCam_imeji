package de.mpg.mpdl.labcam.TaskManager;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import de.mpg.mpdl.labcam.Model.LocalModel.Task;
import de.mpg.mpdl.labcam.R;
import de.mpg.mpdl.labcam.Utils.DeviceStatus;

import java.util.List;

/**
 * Created by yingli on 6/7/16.
 */
public class RecentTaskAdapter extends BaseAdapter {

    private static String TAG = RecentTaskAdapter.class.getSimpleName();
    private LayoutInflater inflater;

    private Activity activity;
    private List<Task> taskList;

    public RecentTaskAdapter(Activity activity, List<Task> taskList) {
        this.activity = activity;
        this.taskList = taskList;
    }

    public RecentTaskAdapter() {
    }

    @Override
    public int getCount() {
        return taskList.size();
    }

    @Override
    public Object getItem(int i) {
        return taskList.get(i);
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
            view = inflater.inflate(R.layout.recent_task_list_cell, null);

        TextView taskName = (TextView) view.findViewById(R.id.tv_task_name);

        if(("AU_FINISHED").equalsIgnoreCase(taskList.get(i).getUploadMode())){
            String taskInfo = taskList.get(i).getTotalItems()+ " photo(s) automatically uploaded to "+ taskList.get(i).getCollectionName();
            taskName.setText(taskInfo);
        }else if(("MU").equalsIgnoreCase(taskList.get(i).getUploadMode())){
            String taskInfo = taskList.get(i).getTotalItems()+ " selected photo(s) uploaded to "+ taskList.get(i).getCollectionName();
            taskName.setText(taskInfo);
        }

        TextView taskTime = (TextView) view.findViewById(R.id.tv_time);

        long endDate = taskList.get(i).getEndDate();

        String dateAgo = DeviceStatus.twoDateDistance(DeviceStatus.longToDate(endDate), DeviceStatus.longToDate(DeviceStatus.dateNow()));

        taskTime.setText(dateAgo);

        return view;

    }
}
