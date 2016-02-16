package example.com.mpdlcamera.TaskManager;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import org.w3c.dom.Text;

import java.util.List;

import example.com.mpdlcamera.AutoRun.ManualUploadService;
import example.com.mpdlcamera.AutoRun.TaskUploadService;
import example.com.mpdlcamera.Model.LocalModel.Image;
import example.com.mpdlcamera.Model.LocalModel.Task;
import example.com.mpdlcamera.R;
import example.com.mpdlcamera.Utils.DeviceStatus;

/**
 * Created by yingli on 1/22/16.
 */
public class TaskManagerAdapter extends BaseAdapter {

    private static String TAG = TaskManagerAdapter.class.getSimpleName();
    private LayoutInflater inflater;

    private Activity activity;
    private List<Task> taskList;

    private RemoveTaskInterface removeTaskInterface;

    public TaskManagerAdapter() {
    }

    public TaskManagerAdapter(Activity activity, List<Task> taskList, RemoveTaskInterface removeTaskInterface) {
        this.activity = activity;
        this.taskList = taskList;
        this.removeTaskInterface = removeTaskInterface;
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
    public View getView(final int position, View view, ViewGroup viewGroup) {


        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (view == null)
            view = inflater.inflate(R.layout.task_list_cell, null);
        TextView taskNameTextView = (TextView) view.findViewById(R.id.tv_task_name);
        taskNameTextView.setText(taskList.get(position).getTaskName());

        ProgressBar firstBar = (ProgressBar) view.findViewById(R.id.firstBar);

        //get task
        final Task task = taskList.get(position);
        int currentNum = task.getFinishedItems();
        int maxNum = task.getTotalItems();

        firstBar.setMax(maxNum);
        firstBar.setProgress(currentNum);

        //totalNumTextView
        TextView totalNumTextView = (TextView) view.findViewById(R.id.totalItems);
        totalNumTextView.setText(maxNum+"");

        //finishedNumTextView
        TextView finishedNumTextView = (TextView) view.findViewById(R.id.finishedItems);
        finishedNumTextView.setText(currentNum + "");

        //DeleteTask
        ImageView deleteTaskImageView = (ImageView) view.findViewById(R.id.task_delete);

        if(task.getUploadMode().equalsIgnoreCase("AU")){
            deleteTaskImageView.setVisibility(View.INVISIBLE);
            deleteTaskImageView.setFocusable(false);
        }else {
            deleteTaskImageView.setVisibility(View.VISIBLE);
            deleteTaskImageView.setFocusable(true);
            deleteTaskImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new AlertDialog.Builder(activity)
                            .setTitle("Delete entry")
                            .setMessage("Are you sure you want to delete this entry?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // continue with delete
                                    deleteTask(task, position);
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();

                }
            });
        }
        //pause/play Task
        CheckBox isPausedCheckBox = (CheckBox) view.findViewById(R.id.checkBox_is_paused);
        isPausedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                String currentTaskId = task.getTaskId();
                if(!compoundButton.isChecked()){
                    // pause clicked
                    try {
                        Task currentTask = new Select().from(Task.class).where("taskId = ?", currentTaskId).executeSingle();
                        currentTask.setState(String.valueOf(DeviceStatus.state.WAITING));
                        currentTask.save();
                        Log.v(TAG, "setState: WAITING" );
                        if(currentTask.getUploadMode().equalsIgnoreCase("AU")){
                            // start AU TaskUploadService
                            Log.v(TAG,"start TaskUploadService");
                            Intent uploadIntent = new Intent(activity, TaskUploadService.class);
                            uploadIntent.putExtra("isBusy",false);
                            activity.startService(uploadIntent);
                        }else{
                            // start ManualUploadService
                            Intent manualUploadServiceIntent = new Intent(activity,ManualUploadService.class);
                            manualUploadServiceIntent.putExtra("currentTaskId", currentTaskId);
                            activity.startService(manualUploadServiceIntent);
                        }
                    }catch (Exception e){}

                }else if(compoundButton.isChecked()){
                    // pause button
                    try {
                        Task currentTask = new Select().from(Task.class).where("taskId = ?", currentTaskId).executeSingle();
                        currentTask.setState(String.valueOf(DeviceStatus.state.STOPPED));
                        currentTask.save();
                        Log.v(TAG, "setState: STOPPED");
                        if(currentTask.getUploadMode().equalsIgnoreCase("AU")){
                            // start AU TaskUploadService
                            Intent uploadIntent = new Intent(activity, TaskUploadService.class);
                            uploadIntent.putExtra("isBusy",false);
                            activity.startService(uploadIntent);
                        }else{
                            // start ManualUploadService
                            Intent manualUploadServiceIntent = new Intent(activity,ManualUploadService.class);
                            manualUploadServiceIntent.putExtra("currentTaskId", currentTaskId);
                            activity.startService(manualUploadServiceIntent);
                        }
                    }catch (Exception e){}

                }
            }
        });

        return view;
    }

    private void deleteTask(Task task,int position){
        String currentTaskId = task.getTaskId();
        new Delete().from(Task.class).where("taskId = ?", currentTaskId).execute();
        Log.v(TAG,"task delete clicked");
        //TODO: delete from data list
        removeTaskInterface.remove(position);
        /** if it is a Au, what to do? */
    }
}

