package example.com.mpdlcamera.NetChangeManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.util.ArrayList;


/**
 * NetWorkStateReceiver is a NetWork state receiver
 * Add the following configs in manifest:
 *              <receiver android:name="com.ice.android.common.net.NetWorkStateReceiver" >
 *                 <intent-filter>
 *                       <action android:name="android.net.conn.CONNECTIVITY_CHANGE" />
 *                 </intent-filter>
 *              </receiver>
 *
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

         //TODO: ConnectivityManager.EXTRA_NETWORK_INFO is aborted after API14, delete following line after testing
//        NetworkInfo ni=(NetworkInfo) intent.getExtras().get(ConnectivityManager.EXTRA_NETWORK_INFO);

        if(ni!=null && ni.getState()== NetworkInfo.State.CONNECTED) {
            networkAvailable = true;
            Log.i(TAG,"Network "+ni.getTypeName()+" connected");
        }
     }
     if(intent.getExtras().getBoolean(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE)) {
         networkAvailable = false;
         Log.d(TAG, "no network connectivity");
     }
        notifyObserver();
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
