package de.mpg.mpdl.labcam;

import android.content.Context;

import de.mpg.mpdl.labcam.code.base.BaseApplication;

/**
 * Created by yingli on 2/9/17.
 */

public class LabCam extends BaseApplication {
    private static Context mContext;

    public static Context getContext() {
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }
}
