package de.mpg.mpdl.labcam.code.common.observer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import de.mpg.mpdl.labcam.Model.LocalModel.Task;
import de.mpg.mpdl.labcam.code.common.service.ManualUploadService;
import de.mpg.mpdl.labcam.code.common.service.TaskUploadService;
import de.mpg.mpdl.labcam.code.common.widget.Constants;
import de.mpg.mpdl.labcam.code.common.widget.DBConnector;
import de.mpg.mpdl.labcam.code.utils.PreferenceUtil;


/**
 * NetWorkStateReceiver is a NetWork state receiver
 * Add the following configs in manifest:
 *              <uses-permission android:name="android.permission.CHANGE_NETWORK_STATE" />
 *              <uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
 *              <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
 *              <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
 */
public class NetWorkStateReceiver extends BroadcastReceiver {

    private static final String TAG = "NetWorkStateReceiver";
    private static boolean networkAvailable = true;
    private String userId;
    private String serverName;

    /** store all the observers   */
    private static ArrayList<NetChangeObserver> netChangeObserverArrayList = new ArrayList<NetChangeObserver>();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG,"Network connectivity change");
        userId =  PreferenceUtil.getString(context, Constants.SHARED_PREFERENCES, Constants.USER_ID, "");
        serverName = PreferenceUtil.getString(context, Constants.SHARED_PREFERENCES, Constants.SERVER_NAME, "");

        if(intent.getExtras()!=null) {
            ConnectivityManager connectivityManager = ((ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE));
            NetworkInfo ni = connectivityManager.getActiveNetworkInfo();

            if(ni!=null && ni.getState()== NetworkInfo.State.CONNECTED) {
                networkAvailable = true;
                notifyObserver();

                // autoTask is null when first install
                Task autoTask = DBConnector.getAuTask(userId, serverName);
                List<Task> ManualTaskList = DBConnector.getActiveManualTasks(userId, serverName);
                //activate upload services
                if (autoTask!=null && autoTask.getUploadMode().equalsIgnoreCase("AU")) {
                    // start AU TaskUploadService
                    Intent uploadIntent = new Intent(context, TaskUploadService.class);
                    Log.i(TAG,"reStart AU after reconnect to internet");
                    context.startService(uploadIntent);
                }

                for (Task task : ManualTaskList) {
                    // start
                    Long currentTaskId = task.getId();
                    Intent manualUploadServiceIntent = new Intent(context, ManualUploadService.class);
                    Log.i(TAG,"currentTaskId: "+currentTaskId);
                    manualUploadServiceIntent.putExtra("currentTaskId", currentTaskId);
                    context.startService(manualUploadServiceIntent);
                }

                Log.i(TAG, "Network " + ni.getTypeName() + " connected");
            }else {
                // stop service
                Intent uploadIntent = new Intent(context, TaskUploadService.class);
                context.stopService(uploadIntent);
                Intent manualUploadServiceIntent = new Intent(context, ManualUploadService.class);
                context.stopService(manualUploadServiceIntent);

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
