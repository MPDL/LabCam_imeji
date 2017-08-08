package de.mpg.mpdl.labcam.code.common.observer;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.util.Log;

public class DatabaseObserver extends ContentObserver {

    private Handler mHandler ;   //update UI thread

    public DatabaseObserver(Context context, Handler handler) {
        super(handler);
        mHandler = handler;
    }


    @Override
    public void onChange(boolean selfChange) {
        mHandler.sendEmptyMessage(1234);

    }
}
