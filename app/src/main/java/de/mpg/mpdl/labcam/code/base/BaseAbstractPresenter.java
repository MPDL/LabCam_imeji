package de.mpg.mpdl.labcam.code.base;

import android.content.Context;

import de.mpg.mpdl.labcam.code.utils.DeviceStatus;

import javax.inject.Inject;


/**
 * Created by jzhu on 2016/11/22.
 */

public class BaseAbstractPresenter<T extends BaseView> implements BasePresenter<T> {
    @Inject
    Context mContext;

    protected T mView;

    /**
     * Method that control the lifecycle of the view. It should be called in the view's
     * (Activity or Fragment) onResume() method.
     */
    public void resume() {

    }

    /**
     * Method that controls the lifecycle of the view. It should be called in the view's
     * (Activity or Fragment) onPause() method.
     */
    public void pause() {

    }

    /**
     * Method that controls the lifecycle of the view. It should be called in the view's
     * (Activity or Fragment) onStop() method.
     */
    public void stop() {

    }

    /**
     * Method that control the lifecycle of the view. It should be called in the view's
     * (Activity or Fragment) onDestroy() method.
     */
    public void destroy() {
        mView = null;
    }

    public void setView(T view) {
        this.mView = view;
    }


    public boolean checkNetWork() {

        if (!DeviceStatus.isNetworkAvailable(mContext)) {
//            mView.onError(mContext.getResources().getString(R.string.exception_network));
            mView.onError("Network failure");
            return false;
        }

        return true;
    }

}
