package de.mpg.mpdl.labcam.code.common.observer;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.util.Log;

public class dbObserver extends ContentObserver {

    private Context mContext  ;
    private Handler mHandler ;   //update UI thread
    private boolean selfChange;


    public dbObserver(Context context,Handler handler) {
        super(handler);
        mContext = context;
        mHandler = handler;
    }


    @Override
    public void onChange(boolean selfChange) {
        this.selfChange = selfChange;
        mHandler.sendEmptyMessage(1234);

    }
}
