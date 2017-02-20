package de.mpg.mpdl.labcam.code.base;

import android.net.ParseException;
import android.text.TextUtils;
import android.util.Log;

import de.mpg.mpdl.labcam.code.data.exception.BusinessException;

import org.json.JSONException;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;

import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.adapter.rxjava.HttpException;

public class BaseSubscriber<T> extends DefaultSubscriber<T> {

    private BaseView baseView;

    public BaseSubscriber(BaseView baseView) {
        this.baseView = baseView;
    }

    @Override
    public void onNext(T t) {
        super.onNext(t);
        if(t instanceof Response){
            int code  = ((Response) t).code();
            if( code == 200){
                baseView.onSuccess();
            }else if(code == 401){
//                AuthExpiredEvent event = new AuthExpiredEvent();
//                RxBus.getDefault().post(event);
                baseView.onError("登录失效，请重新登录");
            }else{
                handleError((Response)t);
            }
        }
    }

    @Override
    public void onCompleted() {
        super.onCompleted();
        if (baseView != null) {
            baseView.hideLoading();
        }
    }

    @Override
    public void onError(Throwable e) {
        if (baseView != null) {
            baseView.hideLoading();
        }
        if (e instanceof BusinessException) {

        } else if (e instanceof ConnectException
                || e instanceof SocketTimeoutException) {// 超时
            baseView.onError("网络不畅，请稍后再试！");
        } else if (e instanceof HttpException) {// server 异常
            httpExceptionHandling(e);
        } else if (e instanceof JSONException
                || e instanceof ParseException) {
            baseView.onError("数据解析异常");
        } else {
//            baseView.onError("出了点小问题");
            onOtherError(e);
        }
        e.printStackTrace();

    }

    public void onOtherError(Throwable e) {
        super.onError(e);
        if (baseView != null) {
            if (e.getMessage() != null) {
                Log.e(baseView.getClass().getSimpleName(), "" + e.getMessage());
            }
            e.printStackTrace();
        }
    }

    private void handleError(Response<ResponseBody> body){
        String msg = null;
        try {
            msg = body.errorBody().string();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        if(!TextUtils.isEmpty(msg)){
            baseView.onError(msg);
        }
    }

    private void httpExceptionHandling(Throwable e){
        int code = ((HttpException) e).code();
        if(code == 401 ){
            // TODO: 1/7/17 没有权限 跳转
//            AuthExpiredEvent event = new AuthExpiredEvent();
//            RxBus.getDefault().post(event);
            toastErrorMessage(e);
//            baseView.onError("登录失效，请重新登录");
        }else if(code == 500 ){
            baseView.onError("服务器出了点小问题，请稍后再试");
        }else{ // 其他返回码 400, bla bla
            toastErrorMessage(e);
        }
    }

    private void toastErrorMessage(Throwable e) {
        HttpException ex = (HttpException) e;
        try {
            String jsonBody = ex.response().errorBody().string();

            if (!TextUtils.isEmpty(jsonBody)) {
                baseView.onError(jsonBody);
                return;
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
}
