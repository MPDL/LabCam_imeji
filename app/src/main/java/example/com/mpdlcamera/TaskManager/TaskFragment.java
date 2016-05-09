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

import com.activeandroid.query.Select;

import java.util.List;

import example.com.mpdlcamera.AutoRun.TaskUploadService;
import example.com.mpdlcamera.AutoRun.dbObserver;
import example.com.mpdlcamera.Model.LocalModel.Image;
import example.com.mpdlcamera.Model.LocalModel.Settings;
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

    // db observer handler
    static ContentResolver resolver;
    static Handler mHandler;
    static dbObserver dbObserver;
    static Uri uri;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mPrefs = getActivity().getSharedPreferences("myPref", 0);
        userId =  mPrefs.getString("userId", "");

        View view = inflater.inflate(R.layout.fragment_task, container,false);
        //taskManager listview
        taskManagerListView = (ListView) view.findViewById(R.id.listView_task);
        taskList = DeviceStatus.getUserTasks(userId);
        Settings settings = new Select().from(Settings.class).where("userId = ?", userId).executeSingle();

        // auTask only for delete
        Task auTask = new Task();
        for(Task task:taskList){
            Log.v(LOG_TAG,"--------------------------");
            Log.v(LOG_TAG,"taskMode: "+task.getUploadMode());
            Log.v(LOG_TAG,"collection: "+task.getCollectionName());
            Log.v(LOG_TAG,"taskTotalItems: "+task.getTotalItems());
            Log.v(LOG_TAG,"finished: "+task.getFinishedItems());
            Log.v(LOG_TAG,"CollectionId: "+task.getCollectionId());
            Log.v(LOG_TAG, "taskState: " + task.getState());
            List<Image> imageList = new Select().from(Image.class).where("taskId = ?", task.getTaskId()).execute();
            Log.v(LOG_TAG,"imageNum: "+imageList.size());
            for (Image image: imageList){
                Log.v(LOG_TAG,"imageName: "+image.getImageName());
                Log.v(LOG_TAG,"imageState: "+image.getState());
            }

            /** exception **/
               if(task!=null&& settings!=null){
                   if (task.getUploadMode().equalsIgnoreCase("AU") && !settings.isAutoUpload()) {
                       auTask = task;
                   }
               }


        }
        taskList.remove(auTask);
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
        uri = Uri.parse("content://example.com.mpdlcamera/tasks");
        resolver = getActivity().getContentResolver();

        mPrefs = getActivity().getSharedPreferences("myPref", 0);
        userId =  mPrefs.getString("userId", "");
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                if(msg.what==1234){
                    Log.v("~~~", "1234~~~");
                    try {
                        taskList = DeviceStatus.getUserTasks(userId);
                        Settings settings = new Select().from(Settings.class).where("userId = ?", userId).executeSingle();
                        Task auTask = new Task();
                        for(Task task:taskList){
                            if(task.getUploadMode().equalsIgnoreCase("AU")&&!settings.isAutoUpload()){
                                    auTask = task;
                            }
                            taskList.remove(auTask);
                        }

                        if(taskList!=null){
                            taskManagerAdapter.notifyDataSetChanged();
                        }
                    }catch (Exception e){}
                }
            }
        };
        dbObserver = new dbObserver(getActivity(),mHandler);
        resolver.registerContentObserver(uri, true, dbObserver);

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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

}