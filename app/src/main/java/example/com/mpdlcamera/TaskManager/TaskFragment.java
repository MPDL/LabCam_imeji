package example.com.mpdlcamera.TaskManager;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import example.com.mpdlcamera.R;

/**
 * Created by yingli on 1/22/16.
 */
public class TaskFragment extends DialogFragment{

    public TaskFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task, container);

        return view;
    }
}
