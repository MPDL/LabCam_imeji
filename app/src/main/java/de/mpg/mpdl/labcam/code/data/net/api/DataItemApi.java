package de.mpg.mpdl.labcam.code.data.net.api;

import java.util.Map;

import de.mpg.mpdl.labcam.Model.DataItem;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import rx.Observable;

/**
 * Created by yingli on 3/17/17.
 */

public interface DataItemApi {
    @Multipart
    @POST("items?syntax=raw")
    Observable<DataItem> uploadItem(@PartMap() Map<String, RequestBody> partMap,
                                    @Part MultipartBody.Part img);


}
