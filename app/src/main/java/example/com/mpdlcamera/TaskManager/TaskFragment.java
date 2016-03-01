package example.com.mpdlcamera.TaskManager;

import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.List;

import example.com.mpdlcamera.AutoRun.TaskUploadService;
import example.com.mpdlcamera.AutoRun.dbObserver;
import example.com.mpdlcamera.Model.LocalModel.Task;
import example.com.mpdlcamera.R;
import example.com.mpdlcamera.Utils.DeviceStatus;

/**
 * Created by yingli on 1/22/16.
 */
public class TaskFragment extends Fragment implements RemoveTaskInterface{

    private static final String LOG_TAG = TaskFragment.class.getSimpleName();
    private List<Task> taskList;
    private ListView taskManagerListView;

    //
    private TaskUploadService mBoundService;
    boolean mIsBound;

    //user info
    private String userId;
    private SharedPreferences mPrefs;

    private TaskManagerAdapter taskManagerAdapter;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mPrefs = getActivity().getSharedPreferences("myPref", 0);
        userId =  mPrefs.getString("userId", "");

        View view = inflater.inflate(R.layout.fragment_task, container,false);
        //taskManager listview
        taskManagerListView = (ListView) view.findViewById(R.id.listView_task);
        taskList = DeviceStatus.getUserTasks(userId);
        for(Task task:taskList){
            Log.v(LOG_TAG,"mode: "+task.getUploadMode());
            Log.v(LOG_TAG,"collection: "+task.getCollectionName());
            Log.v(LOG_TAG,"total:: "+task.getTotalItems());
            Log.v(LOG_TAG,"finished: "+task.getFinishedItems());
            Log.v(LOG_TAG,"State: "+task.getState());
        }

        taskManagerAdapter = new TaskManagerAdapter(this.getActivity(),taskList,this);
        taskManagerAdapter.notifyDataSetChanged();
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


//    MyObserver observer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Uri uri = Uri.parse("content://example.com.mpdlcamera/tasks");
        ContentResolver resolver = getActivity().getContentResolver();

        mPrefs = getActivity().getSharedPreferences("myPref", 0);
        userId =  mPrefs.getString("userId", "");
        Handler mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                if(msg.what==1234){
                    Log.v("~~~", "1234~~~");
                    try {
                        taskList = DeviceStatus.getUserTasks(userId);
                        if(taskList!=null){
                            taskManagerAdapter.notifyDataSetChanged();
                        }
                    }catch (Exception e){}
                }
            }
        };

        resolver.registerContentObserver(uri, true, new dbObserver(getActivity(), mHandler));

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

    @Override
    public void onDestroy() {
        super.onDestroy();
//        doUnbindService();
    }

    @Override
    public void remove(int postion) {
        try{
        taskList.remove(postion);
        taskManagerAdapter.notifyDataSetChanged();
        } catch (Exception e){
            //press too fast
        }
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

//    private ServiceConnection mConnection = new ServiceConnection() {
//        public void onServiceConnected(ComponentName className, IBinder service) {
//            // This is called when the connection with the service has been
//            // established, giving us the service object we can use to
//            // interact with the service.  Because we have bound to a explicit
//            // service that we know is running in our own process, we can
//            // cast its IBinder to a concrete class and directly access it.
//            mBoundService = ((TaskUploadService.AutoUploadServiceBinder) service).getService();
//
//        }
//
//        public void onServiceDisconnected(ComponentName className) {
//            // This is called when the connection with the service has been
//            // unexpectedly disconnected -- that is, its process crashed.
//            // Because it is running in our same process, we should never
//            // see this happen.
//            mBoundService = null;
//        }
//    };
//
//
//    void doBindService() {
//        // Establish a connection with the service.  We use an explicit
//        // class name because we want a specific service implementation that
//        // we know will be running in our own process (and thus won't be
//        // supporting component replacement by other applications).
//        getActivity().bindService(new Intent(getActivity(), TaskUploadService.class), mConnection, Context.BIND_AUTO_CREATE);
//
//        mIsBound = true;
//    }
//
//    void doUnbindService() {
//        if (mIsBound) {
//            // Detach our existing connection.
//            getActivity().unbindService(mConnection);
//            mIsBound = false;
//        }
//    }
}