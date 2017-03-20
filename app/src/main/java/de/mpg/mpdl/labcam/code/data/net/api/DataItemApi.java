package de.mpg.mpdl.labcam.code.data.net.api;

import de.mpg.mpdl.labcam.Model.DataItem;

import okhttp3.MultipartBody;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import rx.Observable;

/**
 * Created by yingli on 3/17/17.
 */

public interface DataItemApi {
    //    @Part("file") TypedFile file
    //    @Part("json") String json
    @Multipart
    @POST("items?syntax=raw")
    Observable<DataItem> uploadItem(@Part MultipartBody.Part[] img);


}
