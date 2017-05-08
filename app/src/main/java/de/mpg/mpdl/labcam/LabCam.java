package de.mpg.mpdl.labcam;

import android.content.Context;
import android.support.multidex.MultiDex;

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
        MultiDex.install(this);
        mContext = getApplicationContext();
    }
}
