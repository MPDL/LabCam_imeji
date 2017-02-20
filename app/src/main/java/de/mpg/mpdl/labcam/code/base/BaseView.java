package de.mpg.mpdl.labcam.code.base;

/**
 * Created by jzhu on 2016/11/22.
 */

public interface BaseView {
    void showLoading();
    void hideLoading();
    void onError(String msg);
    void onSuccess(String msg);
    void onThrowable(Throwable e);

    //特殊处理Response
    void onSuccess();

}
