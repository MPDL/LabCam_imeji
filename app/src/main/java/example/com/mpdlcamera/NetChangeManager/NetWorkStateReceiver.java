package example.com.mpdlcamera.NetChangeManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.activeandroid.query.Select;

import java.util.ArrayList;
import java.util.List;

import example.com.mpdlcamera.AutoRun.ManualUploadService;
import example.com.mpdlcamera.AutoRun.TaskUploadService;
import example.com.mpdlcamera.Model.LocalModel.Task;


/**
 * NetWorkStateReceiver is a NetWork state receiver
 * Add the following configs in manifest:
 *              <uses-permission android:name="android.permission.CHANGE_NETWORK_STATE" />
 *              <uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
 *              <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
 *              <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
 * Created by yingli on 12/8/15.
 */
public class NetWorkStateReceiver extends BroadcastReceiver {

    private static final String TAG = "NetWorkStateReceiver";
    private static boolean networkAvailable = true;

    /** store all the observers   */
    private static ArrayList<NetChangeObserver> netChangeObserverArrayList = new ArrayList<NetChangeObserver>();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG,"Network connectivity change");
     if(intent.getExtras()!=null) {

         ConnectivityManager connectivityManager = ((ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE));
         NetworkInfo ni = connectivityManager.getActiveNetworkInfo();

        if(ni!=null && ni.getState()== NetworkInfo.State.CONNECTED) {
            networkAvailable = true;
            notifyObserver();
            try {

                Task autoTask = new Select().from(Task.class).where("uploadMode = ?", "AU").executeSingle();

                List<Task> ManualTaskList = new Select().from(Task.class).where("uploadMode = ?", "MU").execute();
                //activate upload services
                if (autoTask.getUploadMode().equalsIgnoreCase("AU")) {
                    // start AU TaskUploadService
                    Intent uploadIntent = new Intent(context, TaskUploadService.class);
                    context.startService(uploadIntent);
                }

                for (Task task : ManualTaskList) {
                    // start
                    String currentTaskId = task.getTaskId();
                    Intent manualUploadServiceIntent = new Intent(context, ManualUploadService.class);
                    manualUploadServiceIntent.putExtra("currentTaskId", currentTaskId);
                    context.startService(manualUploadServiceIntent);
                }
            }catch (Exception e){
                Log.v(TAG,"Exception in activate upload services");
            }

            Log.i(TAG, "Network " + ni.getTypeName() + " connected");
        }
     }
     if(intent.getExtras().getBoolean(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE)) {
         networkAvailable = false;
         notifyObserver();
         Log.d(TAG, "no network connectivity");
     }

    }

    /**
     * register NetStateObserver
     * @param observer
     */
    public static void registerNetStateObserver(NetChangeObserver observer){
        if(netChangeObserverArrayList == null){
            netChangeObserverArrayList = new ArrayList<NetChangeObserver>();
        }
        netChangeObserverArrayList.add(observer);
    }

    /**
     * deregister NetStateObserver
     * @param observer
     */
    public static void unRegisterNetStateObserver(NetChangeObserver observer){
        if(netChangeObserverArrayList != null){
            netChangeObserverArrayList.remove(observer);
        }
    }


    private void notifyObserver(){
        if(netChangeObserverArrayList !=null && netChangeObserverArrayList.size() >0){
            for(NetChangeObserver observer : netChangeObserverArrayList){
                if(observer != null){
                    if(networkAvailable){
                        observer.OnConnect();
                    }else{
                        observer.OnDisConnect();
                    }
                }
            }
        }
    }
}
