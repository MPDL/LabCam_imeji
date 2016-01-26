package example.com.mpdlcamera.TaskManager;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

import example.com.mpdlcamera.Model.LocalModel.Task;
import example.com.mpdlcamera.R;

/**
 * Created by yingli on 1/22/16.
 */
public class TaskManagerAdapter extends BaseAdapter {

    private LayoutInflater inflater;

    private Activity activity;
    private List<Task> taskList;

    public TaskManagerAdapter() {
    }

    public TaskManagerAdapter(Activity activity, List<Task> taskList) {
        this.activity = activity;
        this.taskList = taskList;
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
    public View getView(int position, View view, ViewGroup viewGroup) {


        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (view == null)
            view = inflater.inflate(R.layout.task_list_cell, null);
        TextView taskNameTextView = (TextView) view.findViewById(R.id.tv_task_name);
        taskNameTextView.setText(taskList.get(position).getTaskName());

        ProgressBar firstBar = (ProgressBar) view.findViewById(R.id.firstBar);
        Task task = taskList.get(position);
        int currentNum = task.getFinishedItems();
        int maxNum = task.getTotalItems();

        if (currentNum == 0 || currentNum == maxNum) {
            //make the progress bar visible
            firstBar.setVisibility(View.VISIBLE);
            firstBar.setMax(maxNum);
        }else if ( currentNum< firstBar.getMax() ) {
            //Set first progress bar value
            firstBar.setProgress(currentNum);
            //Set the second progress bar value
        }else {
            firstBar.setProgress(0);
            firstBar.setSecondaryProgress(0);
            currentNum = 0;
            firstBar.setVisibility(View.GONE);
        }

        TextView totalNumTextView = (TextView) view.findViewById(R.id.totalItems);
        totalNumTextView.setText(maxNum+"");

        TextView finishedNumTextView = (TextView) view.findViewById(R.id.finishedItems);
        finishedNumTextView.setText(currentNum+"");

        return view;
    }

}
