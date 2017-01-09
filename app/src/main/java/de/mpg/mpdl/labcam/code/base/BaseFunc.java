package de.mpg.mpdl.labcam.code.base;

import de.mpg.mpdl.labcam.code.common.widget.Constants;
import de.mpg.mpdl.labcam.code.data.exception.BusinessException;

import rx.Observable;
import rx.functions.Func1;



public class BaseFunc<T> implements Func1<BaseResp<T>, Observable<T>> {
    @Override
    public Observable<T> call(BaseResp<T> resp) {
        if (!Constants.STATUS_SUCCESS.equals(resp.getStatus())) {
            return Observable.error(new BusinessException(
                    resp.getErrorCode(),
                    resp.getErrorMsg()));
        }
        return Observable.just(resp.getData());
    }
}
