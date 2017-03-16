package de.mpg.mpdl.labcam.code.common.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.activeandroid.query.Delete;

import de.mpg.mpdl.labcam.code.common.service.ManualUploadService;
import de.mpg.mpdl.labcam.code.common.service.TaskUploadService;
import de.mpg.mpdl.labcam.Model.LocalModel.Task;
import de.mpg.mpdl.labcam.R;
import de.mpg.mpdl.labcam.code.common.widget.DBConnector;
import de.mpg.mpdl.labcam.code.common.callback.RemoveTaskInterface;
import de.mpg.mpdl.labcam.code.utils.DeviceStatus;

import java.util.Date;
import java.util.List;

/**
 * Created by yingli on 1/22/16.
 */
public class TaskManagerAdapter extends BaseAdapter {

    private static String TAG = TaskManagerAdapter.class.getSimpleName();
    SharedPreferences mPref;
    String userId;
    String serverName;
    private LayoutInflater inflater;

    private Activity activity;
    private List<Task> taskList;

    private RemoveTaskInterface removeTaskInterface;

    static final int  AU_WAITING = 1001;
    static final int AU_UPLOADING = 1002;
    static final int AU_FINISH = 1003;

    static final int MU_UPLOADING = 2002;
    static final int MU_FINISH = 2003;

    // interrupt (collectionID not exist)
    static final int AU_FAILED = 3001;
    static final int MU_FAILED = 3002;

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

        // set task info according to mode
        TextView taskNameTextView = (TextView) view.findViewById(R.id.tv_task_name);
        if(taskList.get(position).getUploadMode().equalsIgnoreCase("AU")){
            // AU
            taskNameTextView.setText("Automatic upload to");
        }else {
            // MU
            taskNameTextView.setText(taskList.get(position).getTotalItems()+" selected photos upload to");
        }
        // set collection name
        TextView taskCollectionTextView = (TextView) view.findViewById(R.id.tv_task_collection);
        taskCollectionTextView.setText(taskList.get(position).getCollectionName());

        //ProgressBar
        ProgressBar firstBar = (ProgressBar) view.findViewById(R.id.firstBar);

        //get task
        final Task task = taskList.get(position);
        int currentNum = task.getFinishedItems();
        int maxNum = task.getTotalItems();

        firstBar.setMax(maxNum);
        firstBar.setProgress(currentNum);

        //percent
        TextView percentTextView = (TextView) view.findViewById(R.id.tv_percent);
        int percent;
        if(maxNum!=0){
            percent = (currentNum * 100) / maxNum;
            Log.d(TAG,"currentNum: "+currentNum);
            Log.d(TAG,"maxNum: "+maxNum);}

        else {
            percent = 0;
        }
        percentTextView.setText(percent + "");


        /**
         * layout changes in different phrases
         */
        int phrase = -1;


        if(task.getUploadMode().equalsIgnoreCase("AU")){
            if(maxNum == 0){
                phrase = AU_WAITING;
            }else if(currentNum == maxNum){
                phrase = AU_FINISH;
            }else {
                phrase = AU_UPLOADING;
            }

        }else {
            if(currentNum == maxNum){
                phrase = MU_FINISH;
            }else {
                phrase = MU_UPLOADING;
            }
        }

//        Log.e("<><>", phrase+"");
//        printTaskLog(phrase, task);

        //collection error
        if(task.getState().equalsIgnoreCase(String.valueOf(DeviceStatus.state.FAILED))){
            if(task.getUploadMode().equalsIgnoreCase("AU")){
                phrase = AU_FAILED;
            }else {
                phrase = MU_FAILED;
            }
        }

        RelativeLayout progressLayout = (RelativeLayout) view.findViewById(R.id.layout_progress);
        RelativeLayout toolButtonLayout = (RelativeLayout) view.findViewById(R.id.layout_stop_delete);
        RelativeLayout errorLayout = (RelativeLayout) view.findViewById(R.id.layout_error);
//        Button clearButton = (Button) view.findViewById(R.id.btn_clear);



        switch (phrase){
            case AU_WAITING:
                progressLayout.setVisibility(View.GONE);
                toolButtonLayout.setVisibility(View.GONE);
//                clearButton.setVisibility(View.GONE);
                errorLayout.setVisibility(View.GONE);
                Log.v(TAG,"AU_WAITING");

                break;
            case AU_UPLOADING:
                progressLayout.setVisibility(View.VISIBLE);
                toolButtonLayout.setVisibility(View.VISIBLE);
//                clearButton.setVisibility(View.GONE);
                errorLayout.setVisibility(View.GONE);
                Log.v(TAG, "AU_UPLOADING");

                break;
            case AU_FINISH:
                progressLayout.setVisibility(View.GONE);
                toolButtonLayout.setVisibility(View.GONE);
//                clearButton.setVisibility(View.GONE);
                errorLayout.setVisibility(View.GONE);
                Log.v(TAG, "AU_FINISH");

                break;
            case MU_UPLOADING:
                progressLayout.setVisibility(View.VISIBLE);
                toolButtonLayout.setVisibility(View.VISIBLE);
//                clearButton.setVisibility(View.GONE);
                errorLayout.setVisibility(View.GONE);
                Log.v(TAG, "MU_UPLOADING");

                break;
            case MU_FINISH:
                progressLayout.setVisibility(View.GONE);
                toolButtonLayout.setVisibility(View.GONE);
                errorLayout.setVisibility(View.GONE);
                Log.v(TAG, "MU_FINISH");
                break;

            case AU_FAILED:
                progressLayout.setVisibility(View.GONE);
                toolButtonLayout.setVisibility(View.VISIBLE);
                errorLayout.setVisibility(View.VISIBLE);
                Log.v(TAG, "AU_FAILED");

                break;
            case MU_FAILED:
                progressLayout.setVisibility(View.GONE);
                toolButtonLayout.setVisibility(View.VISIBLE);
                errorLayout.setVisibility(View.VISIBLE);
                Log.v(TAG, "MU_FAILED");
                break;

        }

        if(maxNum == 0||currentNum == maxNum){
            //show clear for MU
            if(task.getUploadMode().equalsIgnoreCase("AU")){
                //AU
            }

        }

        //DeleteTask
        ImageView deleteTaskImageView = (ImageView) view.findViewById(R.id.task_delete);


            deleteTaskImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new AlertDialog.Builder(activity)
                            .setTitle("Abort Uploading")
                            .setMessage("Are you sure you want to give up this uploading process?")
                            .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                    if (task.getUploadMode().equalsIgnoreCase("AU")) {
                                        //abort uploading, but keep the task
                                        //stop the service
                                        Intent uploadIntent = new Intent(activity, TaskUploadService.class);
                                        activity.stopService(uploadIntent);

                                        //TODO: delete all image with this taskId
                                        String collectionID = task.getCollectionId();
                                        String collectionName = task.getCollectionName();
                                        String username = task.getUserName();
                                        String userId = task.getUserId();
                                        task.delete();

                                        taskList.remove(task);


                                        //resume task(create task)

                                        Task newTask = new Task();
                                        newTask.setUploadMode("AU");
                                        newTask.setCollectionId(collectionID);
                                        newTask.setCollectionName(collectionName);
                                        newTask.setState(String.valueOf(DeviceStatus.state.WAITING));
                                        newTask.setUserName(username);
                                        newTask.setUserId(userId);
                                        newTask.setTotalItems(0);
                                        newTask.setFinishedItems(0);
                                        Long now = new Date().getTime();
                                        newTask.setStartDate(String.valueOf(now));
                                        newTask.save();
                                        taskList.add(newTask);

                                        notifyDataSetChanged();

                                    } else {
                                        // continue with delete
                                        deleteTask(task, position);
                                    }

                                }
                            })
                            .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                }
                            })
                            .setIcon(R.drawable.error_alert)
                            .show();

                }
            });



        //pause/play Task
        final CheckBox isPausedCheckBox = (CheckBox) view.findViewById(R.id.checkBox_is_paused);
        Log.e(TAG,"state: "+task.getState());

        if(task.getState().equalsIgnoreCase(String.valueOf(DeviceStatus.state.WAITING))){
            isPausedCheckBox.setChecked(false);
        }else if(task.getState().equalsIgnoreCase(String.valueOf(DeviceStatus.state.STOPPED))){
            //true is play button, means state now is paused
            isPausedCheckBox.setChecked(true);
        }else if(task.getState().equalsIgnoreCase(String.valueOf(DeviceStatus.state.FAILED))){
            isPausedCheckBox.setChecked(true);
        }


        isPausedCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Long currentTaskId = task.getId();
                if (!isPausedCheckBox.isChecked()) {
                    //  button clicked

                    getUserInfo();
                    Task currentTask = DBConnector.getTaskById(currentTaskId.toString(), userId, serverName);
                    currentTask.setState(String.valueOf(DeviceStatus.state.WAITING));
                    currentTask.save();
                    Log.v(TAG, "setState: WAITING");

                    if (currentTask.getState().equalsIgnoreCase(String.valueOf(DeviceStatus.state.WAITING))) {
                        isPausedCheckBox.setChecked(false);
                    } else {
                        //true is play button, means state now is paused
                        isPausedCheckBox.setChecked(true);
                    }

                    if (currentTask.getUploadMode().equalsIgnoreCase("AU")) {
                        // start AU TaskUploadService
                        Log.v(TAG, "start TaskUploadService");
                        Intent uploadIntent = new Intent(activity, TaskUploadService.class);
                        activity.startService(uploadIntent);
                    } else {
                        // start ManualUploadService
                        Log.v(TAG, "start manualUploadService");
                        Intent manualUploadServiceIntent = new Intent(activity, ManualUploadService.class);
                        manualUploadServiceIntent.putExtra("currentTaskId", currentTaskId);
                        activity.startService(manualUploadServiceIntent);
                    }


                } else if (isPausedCheckBox.isChecked()) {
                    //  button clicked
                    getUserInfo();
                    Task currentTask = DBConnector.getTaskById(currentTaskId.toString(), userId, serverName);
                    currentTask.setState(String.valueOf(DeviceStatus.state.STOPPED));
                    currentTask.save();
                    Log.v(TAG, "setState: STOPPED");

                    if (currentTask.getState().equalsIgnoreCase(String.valueOf(DeviceStatus.state.WAITING))) {
                        isPausedCheckBox.setChecked(false);
                    } else {
                        //true is play button, means state now is paused
                        isPausedCheckBox.setChecked(true);
                    }

                    if (currentTask.getUploadMode().equalsIgnoreCase("AU")) {
                        // start AU TaskUploadService
                        Intent uploadIntent = new Intent(activity, TaskUploadService.class);
//                            uploadIntent.putExtra("isBusy",false);
                        activity.startService(uploadIntent);
                    } else {
                        // start ManualUploadService
                        Intent manualUploadServiceIntent = new Intent(activity, ManualUploadService.class);
                        manualUploadServiceIntent.putExtra("currentTaskId", currentTaskId);
                        activity.startService(manualUploadServiceIntent);
                    }


                }
            }
        });
        return view;
    }

    private void deleteTask(Task task,int position){
        Long currentTaskId = task.getId();
        new Delete().from(Task.class).where("Id = ?", currentTaskId).execute();
        //// TODO: 3/16/17 delete imgs in task
        Log.e(TAG, "task delete clicked");
        removeTaskInterface.remove(position);
        taskList.remove(task);
        notifyDataSetChanged();
        /** if it is a Au, what to do? */
    }

    private void getUserInfo(){
        mPref = activity.getSharedPreferences("myPref", 0);
        userId = mPref.getString("userId", "");
        serverName = mPref.getString("serverName", "");
    }
}

