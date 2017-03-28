package de.mpg.mpdl.labcam.code.data.repository.impl;

import de.mpg.mpdl.labcam.Model.DataItem;
import de.mpg.mpdl.labcam.code.data.net.RetrofitFactory;
import de.mpg.mpdl.labcam.code.data.net.api.DataItemApi;
import de.mpg.mpdl.labcam.code.data.repository.DataItemRepository;

import java.util.Map;

import javax.inject.Inject;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import rx.Observable;

/**
 * Created by yingli on 3/17/17.
 */

public class DataItemRepositoryImpl implements DataItemRepository {

    @Inject
    public DataItemRepositoryImpl() {
    }

    @Override
    public Observable<DataItem> uploadItem(Map<String, RequestBody> map, MultipartBody.Part img) {
        return RetrofitFactory.getInstance().create(DataItemApi.class).uploadItem(map, img);
    }
}
