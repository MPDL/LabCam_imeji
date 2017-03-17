package de.mpg.mpdl.labcam.code.data.service.impl;

import de.mpg.mpdl.labcam.code.data.model.UserModel;
import de.mpg.mpdl.labcam.code.data.repository.UserRepository;
import de.mpg.mpdl.labcam.code.data.service.UserService;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by yingli on 3/16/17.
 */

public class UserServiceImp extends UserService {
    @Inject
    UserRepository userRepository;

    @Inject
    public UserServiceImp() {
    }

    @Override
    public Observable<UserModel> basicLogin() {
        return userRepository.basicLogin();
    }

}
