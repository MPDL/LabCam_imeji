package de.mpg.mpdl.labcam.Upload;

/**
 * Created by kiran on 28.09.15.
 */

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

@SuppressLint("ParcelCreator")
public class UploadResultReceiver extends ResultReceiver {
    private Receiver mReceiver;

    public UploadResultReceiver(Handler handler) {
        super(handler);
    }

    public void setReceiver(Receiver receiver) {
        mReceiver = receiver;
    }

    /*
        receiver method which runs when it gets the result
     */
    public interface Receiver {
        public void onReceiveResult(int resultCode, Bundle resultData);
    }


    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (mReceiver != null) {
            mReceiver.onReceiveResult(resultCode, resultData);
        }
    }
}
