package de.mpg.mpdl.labcam.code.common.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import java.util.List;

import de.mpg.mpdl.labcam.code.common.observer.NetChangeObserver;
import de.mpg.mpdl.labcam.code.common.observer.NetWorkStateReceiver;
import de.mpg.mpdl.labcam.code.common.observer.UploadResultReceiver;

/**
 * Created by yingli on 12/10/15.
 */
public class AutoRunService extends Service implements UploadResultReceiver.Receiver,NetChangeObserver {


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        NetWorkStateReceiver.registerNetStateObserver(this);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        Toast.makeText(this, "***** AutoRunService *****: onStart", Toast.LENGTH_LONG).show();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void OnConnect() {
    }

    @Override
    public void OnDisConnect() {
//        Toast.makeText(this, "AutoRunService detect network disconnect...", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        NetWorkStateReceiver.unRegisterNetStateObserver(this);
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {

    }


//    check if the app is running in the foreground using this method
    public static boolean isForeground(Context ctx, String myPackage){
        ActivityManager manager = (ActivityManager) ctx.getSystemService(ACTIVITY_SERVICE);
        List< ActivityManager.RunningTaskInfo > runningTaskInfo = manager.getRunningTasks(1);

        ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
        if(componentInfo.getPackageName().equals(myPackage)) {
            return true;
        }
        return false;
    }
}