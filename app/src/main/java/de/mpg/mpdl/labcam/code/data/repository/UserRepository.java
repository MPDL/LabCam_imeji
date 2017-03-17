package de.mpg.mpdl.labcam.code.data.repository;

import de.mpg.mpdl.labcam.code.data.model.UserModel;

import rx.Observable;

/**
 * Created by yingli on 3/16/17.
 */

public interface UserRepository {
    Observable<UserModel> basicLogin();
}
