package de.mpg.mpdl.labcam.code.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

/**
 * Created by yingli on 3/28/17.
 */

public abstract class BaseMvpDialogFragment <T extends BasePresenter> extends BaseDialogFragment implements
                                                                                                 BaseView  {

    @Inject
    protected T mPresenter;

    protected abstract void injectComponent();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        injectComponent();
        return super.onCreateView(inflater, container, savedInstanceState);
    }


    @Override
    public void onResume() {
        super.onResume();
        mPresenter.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mPresenter.pause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mPresenter.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.destroy();
    }


    @Override
    public void onError(String msg) {
        showToast(msg);
    }

    @Override
    public void onSuccess(String msg) {
        showToast(msg);
    }
    @Override
    public void onThrowable(Throwable e) {

    }
    @Override
    public void onSuccess() {

    }
}
