package de.mpg.mpdl.labcam.code.base;

import android.os.Bundle;

import javax.inject.Inject;


public abstract class BaseMvpActivity<T extends BasePresenter> extends BaseCompatActivity implements
                                                                                          BaseView{


    @Inject
    protected T mPresenter;

    protected abstract void injectComponent();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        injectComponent();
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPresenter.pause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mPresenter.stop();
    }

    @Override
    protected void onDestroy() {
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
