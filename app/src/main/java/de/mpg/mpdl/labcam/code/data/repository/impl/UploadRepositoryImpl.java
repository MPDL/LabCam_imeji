package de.mpg.mpdl.labcam.code.data.repository.impl;

import javax.inject.Inject;

import de.mpg.mpdl.labcam.code.data.net.RetrofitFactory;
import de.mpg.mpdl.labcam.code.data.repository.UploadRepository;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;

/**
 * Created by yingli on 8/10/17.
 */


public class UploadRepositoryImpl implements UploadRepository{

    @Inject
    public UploadRepositoryImpl() {
    }

    @Override
    public Call<ResponseBody> uploadItem(MultipartBody.Part file, String json) {
        return RetrofitFactory.getInstance().create(RetrofitFactory.ImejiAPI.class).uploadItem(file, json);
    }
}
