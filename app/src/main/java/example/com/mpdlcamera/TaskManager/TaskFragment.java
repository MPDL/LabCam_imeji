package example.com.mpdlcamera.TaskManager;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
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
public class TaskFragment extends Fragment{

    private List<Task> taskList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_task, container,false);
        //taskManager listview
        ListView taskManagerListView = (ListView) view.findViewById(R.id.listView_task);
        taskList = DeviceStatus.getTasks();
        TaskManagerAdapter taskManagerAdapter = new TaskManagerAdapter(this.getActivity(),taskList);
        taskManagerListView.setAdapter(taskManagerAdapter);
        return view;
    }



    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment UserFragment.
     */

    public TaskFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }
}
