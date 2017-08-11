package de.mpg.mpdl.labcam.code.data.net.api;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Created by yingli on 8/11/17.
 */

public interface UploadApi {
    @Multipart
    @POST("items")
    Call<ResponseBody> uploadItem(
            @Part MultipartBody.Part file,
            @Part("json") RequestBody json
    );
}
