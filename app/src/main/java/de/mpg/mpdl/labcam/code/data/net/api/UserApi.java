package de.mpg.mpdl.labcam.code.data.net.api;


import de.mpg.mpdl.labcam.code.data.model.UserModel;

import retrofit2.http.POST;
import rx.Observable;

/**
 * Created by yingli on 3/16/17.
 */

public interface UserApi {
    @POST("login")
    Observable<UserModel> basicLogin();
}
