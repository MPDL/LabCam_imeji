package de.mpg.mpdl.labcam.code.data.repository;


import de.mpg.mpdl.labcam.Model.DataItem;

import okhttp3.MultipartBody;
import rx.Observable;

/**
 * Created by yingli on 3/17/17.
 */

public interface DataItemRepository {
    Observable<DataItem> uploadItem(MultipartBody.Part[] img);
}