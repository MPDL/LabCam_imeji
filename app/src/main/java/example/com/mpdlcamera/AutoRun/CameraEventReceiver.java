package example.com.mpdlcamera.AutoRun;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import example.com.mpdlcamera.Folder.UploadService;
import example.com.mpdlcamera.Upload.UploadResultReceiver;


/**
 * Created by yingli on 12/16/15.
 */
public class CameraEventReceiver extends BroadcastReceiver implements UploadResultReceiver.Receiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "a new photo", Toast.LENGTH_LONG)
                .show();
        UploadResultReceiver mReceiver = new UploadResultReceiver(new Handler());
        mReceiver.setReceiver(this);
        Intent uploadIntent = new Intent(context, UploadService.class);
        intent.putExtra("receiver", mReceiver);
        context.startService(uploadIntent);
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {

    }
}
