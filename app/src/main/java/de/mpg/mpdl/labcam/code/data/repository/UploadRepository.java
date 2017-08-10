package de.mpg.mpdl.labcam.code.data.repository;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;

/**
 * Created by yingli on 8/10/17.
 */

public interface UploadRepository {
    Call<ResponseBody> uploadItem(
            MultipartBody.Part file,
            String json
    );
}
