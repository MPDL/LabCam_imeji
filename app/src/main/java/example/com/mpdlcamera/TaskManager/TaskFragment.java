package example.com.mpdlcamera.TaskManager;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.List;

import example.com.mpdlcamera.Model.LocalModel.Task;
import example.com.mpdlcamera.R;
import example.com.mpdlcamera.Utils.DeviceStatus;

/**
 * Created by yingli on 1/22/16.
 */
public class TaskFragment extends DialogFragment{

    private List<Task> taskList;
    public TaskFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task, container);
        //taskManager listview
        ListView taskManagerListView = (ListView) view.findViewById(R.id.listView_task);
        taskList = DeviceStatus.getTasks();
        TaskManagerAdapter taskManagerAdapter = new TaskManagerAdapter(this.getActivity(),taskList);
        taskManagerListView.setAdapter(taskManagerAdapter);
        return view;
    }
}
