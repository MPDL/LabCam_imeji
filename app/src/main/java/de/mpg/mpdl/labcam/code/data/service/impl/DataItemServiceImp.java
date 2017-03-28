package de.mpg.mpdl.labcam.code.data.service.impl;

import de.mpg.mpdl.labcam.Model.DataItem;
import de.mpg.mpdl.labcam.code.data.repository.DataItemRepository;
import de.mpg.mpdl.labcam.code.data.service.DataItemService;

import java.util.Map;

import javax.inject.Inject;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import rx.Observable;

/**
 * Created by yingli on 3/17/17.
 */

public class DataItemServiceImp extends DataItemService{

    @Inject
    DataItemRepository dataItemRepository;

    @Inject
    public DataItemServiceImp() {
    }

    @Override
    public Observable<DataItem> uploadItem(Map<String, RequestBody> map, MultipartBody.Part img) {
        return dataItemRepository.uploadItem(map, img);
    }
}
