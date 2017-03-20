package de.mpg.mpdl.labcam.code.data.service;

import de.mpg.mpdl.labcam.Model.DataItem;
import de.mpg.mpdl.labcam.code.base.BaseService;

import okhttp3.MultipartBody;
import rx.Observable;

/**
 * Created by yingli on 3/17/17.
 */

public abstract class DataItemService extends BaseService{
    public abstract Observable<DataItem> uploadItem(MultipartBody.Part[] img);
}
