package example.com.mpdlcamera.Upload;

/**
 * Created by kiran on 28.09.15.
 */

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

public class UploadResultReceiver extends ResultReceiver {
    private Receiver mReceiver;

    public UploadResultReceiver(Handler handler) {
        super(handler);
    }

    public void setReceiver(Receiver receiver) {
        mReceiver = receiver;
    }

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
