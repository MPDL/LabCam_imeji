package de.mpg.mpdl.labcam.code.base;

public interface BaseView {
    void showLoading();
    void hideLoading();
    void onError(String msg);
    void onSuccess(String msg);
    void onThrowable(Throwable e);

    //Special Response
    void onSuccess();

}
