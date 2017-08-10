package de.mpg.mpdl.labcam.code.base;

import android.content.Context;

import javax.inject.Inject;

import de.mpg.mpdl.labcam.code.utils.DeviceStatus;

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
            mView.onError("Network failure");
            return false;
        }

        return true;
    }

}
