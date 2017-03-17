package de.mpg.mpdl.labcam.code.data.repository.impl;

import de.mpg.mpdl.labcam.code.data.model.UserModel;
import de.mpg.mpdl.labcam.code.data.net.RetrofitFactory;
import de.mpg.mpdl.labcam.code.data.net.api.UserApi;
import de.mpg.mpdl.labcam.code.data.repository.UserRepository;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by yingli on 3/16/17.
 */

public class UserRepositoryImpl implements UserRepository{

    @Inject
    public UserRepositoryImpl() {
    }

    @Override
    public Observable<UserModel> basicLogin() {
        return RetrofitFactory.getInstance().create(UserApi.class).basicLogin();
    }

}
