package de.mpg.mpdl.labcam.code.common.observer;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.util.Log;

/**
 * Created by yingli on 1/26/16.
 *
 *
 */
public class dbObserver extends ContentObserver {
    private static String TAG = "dbContentObserver";

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
        // do s.th.
        // depending on the handler you might be on the UI
        // thread, so be cautious!

        Log.i(TAG, "the Tasks table has changed");
//        Toast.makeText(mContext,"db changed",Toast.LENGTH_LONG).show();
//
//        Uri taskUri = Uri.parse("content://de.mpg.mpdl.labcam/tasks") ;
//        Cursor c = mContext.getContentResolver().query(taskUri, null, null, null, null);
//        String sb = "";
//        if (c!=null){
//             sb = "bla";
//        }

        mHandler.sendEmptyMessage(1234);

    }
}
