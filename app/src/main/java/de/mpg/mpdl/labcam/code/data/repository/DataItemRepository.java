package de.mpg.mpdl.labcam.code.data.repository;


import de.mpg.mpdl.labcam.Model.DataItem;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import rx.Observable;

/**
 * Created by yingli on 3/17/17.
 */

public interface DataItemRepository {
    Observable<DataItem> uploadItem(Map<String, RequestBody> map, MultipartBody.Part img);
}
