package de.mpg.mpdl.labcam.code.activity;

import android.app.Activity;
import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;

import de.mpg.mpdl.labcam.code.common.observer.dbObserver;
import de.mpg.mpdl.labcam.Model.LocalModel.Settings;
import de.mpg.mpdl.labcam.Model.LocalModel.Task;
import de.mpg.mpdl.labcam.R;
import de.mpg.mpdl.labcam.code.common.callback.RemoveTaskInterface;
import de.mpg.mpdl.labcam.code.common.widget.DBConnector;
import de.mpg.mpdl.labcam.code.base.BaseCompatActivity;
import de.mpg.mpdl.labcam.code.common.adapter.TaskManagerAdapter;
import de.mpg.mpdl.labcam.code.common.widget.Constants;
import de.mpg.mpdl.labcam.code.utils.PreferenceUtil;

import java.util.Iterator;
import java.util.List;

import butterknife.BindView;

public class ActiveTaskActivity extends BaseCompatActivity implements RemoveTaskInterface {

    @BindView(R.id.listView_task)
    ListView taskManagerListView;

    private Activity activity = this;
    //user info
    private String userId;
    private String serverUrl;

    //taskList contents
    private TaskManagerAdapter taskManagerAdapter;
    private List<Task> taskList;

    // db observer handler
    static ContentResolver resolver;
    static Handler mHandler;
    static de.mpg.mpdl.labcam.code.common.observer.dbObserver dbObserver;
    static Uri uri;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_active_task;
    }

    @Override
    protected void initContentView(Bundle savedInstanceState) {
        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar_active_task);
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        uri = Uri.parse("content://de.mpg.mpdl.labcam/tasks");
        resolver = activity.getContentResolver();

        userId =  PreferenceUtil.getString(this, Constants.SHARED_PREFERENCES, Constants.USER_ID, "");
        serverUrl = PreferenceUtil.getString(this, Constants.SHARED_PREFERENCES, Constants.SERVER_NAME, "");


        /** handler observer **/
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                if(msg.what==1234){
                    Log.v("~~~", "1234~~~");

                    taskList = DBConnector.getActiveTasks(userId, serverUrl);

                    Settings settings = DBConnector.getSettingsByUserId(getApplicationContext(), userId);
                    if(taskList==null){
                        Log.e("handler", "task list is empty");
                        return;
                    }

                    // settings not null, auto task exist
                    if(settings!=null) {

                        // get autoTask and remove from list
                        Iterator<Task> taskIterator = taskList.iterator();
                        while (taskIterator.hasNext()) {
                            //ConcurrentModificationException here
                            Task theTask = taskIterator.next();
                            if (theTask.getUploadMode().equalsIgnoreCase("AU") && !settings.isAutoUpload()) {
                                taskIterator.remove();
                            }
                        }
                    }

                    if(taskList!=null){
                        taskManagerAdapter.notifyDataSetChanged();
                    }
                }
            }
        };
        dbObserver = new dbObserver(activity, mHandler);
        resolver.registerContentObserver(uri, true, dbObserver);

        taskList = DBConnector.getActiveTasks(userId, serverUrl);

        Settings settings = DBConnector.getSettingsByUserId(getApplicationContext(), userId);
//        Settings settings = new Select().from(Settings.class).where("userId = ?", userId).executeSingle();

        // auTask only for delete
        Task auTask = new Task();
        for(Task task:taskList){
            /** exception **/
            if(task!=null&& settings!=null){
                // auto task, autoUpload switch off
                if (task.getUploadMode().equalsIgnoreCase("AU") && task.getTotalItems()== 0 ) {
                    auTask = task;
                }
            }


        }
        taskList.remove(auTask);
        taskManagerAdapter = new TaskManagerAdapter(this.activity,taskList,this);
        taskManagerAdapter.notifyDataSetChanged();
        taskManagerListView.setAdapter(taskManagerAdapter);
    }

    @Override
    public void onPause() {
        resolver.unregisterContentObserver(dbObserver);
        super.onPause();
    }

    @Override
    public void onResume() {
        resolver.registerContentObserver(uri, true, dbObserver);
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void remove(int position) {
        try {
            taskList.remove(position);
            taskManagerAdapter.notifyDataSetChanged();
        }catch (IndexOutOfBoundsException e){
            e.printStackTrace();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }
}