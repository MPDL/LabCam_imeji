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
                || e instanceof SocketTimeoutException) {
            baseView.onError("time out");
        } else if (e instanceof HttpException) {// server error
            httpExceptionHandling(e);
        } else if (e instanceof JSONException
                || e instanceof ParseException) {
            baseView.onError("data parsing error");
        } else {
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
            //TODO: handle error 401, for example toastErrorMessage(e);
        }else if(code == 500 ){
            //TODO: handle server error 500
        }else{
            //TODO: handle other error 400 401...
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
