package example.com.mpdlcamera.AutoRun;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import example.com.mpdlcamera.NetChangeManager.NetChangeObserver;
import example.com.mpdlcamera.NetChangeManager.NetWorkStateReceiver;
import example.com.mpdlcamera.Upload.UploadResultReceiver;

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
//        Toast.makeText(this, "AutoRunService detect network Connect...", Toast.LENGTH_LONG).show();
        // FIXME: 2/10/16 unknow bug in UploadService
//        UploadResultReceiver mReceiver = new UploadResultReceiver(new Handler());
//        mReceiver.setReceiver(this);
//        Intent intent = new Intent(this, UploadService.class);
//        intent.putExtra("receiver", mReceiver);
//        this.startService(intent);
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
}