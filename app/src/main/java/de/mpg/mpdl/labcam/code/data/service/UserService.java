package de.mpg.mpdl.labcam.code.data.service;

import de.mpg.mpdl.labcam.code.base.BaseService;
import de.mpg.mpdl.labcam.code.data.model.UserModel;
import rx.Observable;

/**
 * Created by yingli on 3/16/17.
 */

public abstract class UserService extends BaseService{

    public abstract Observable<UserModel> basicLogin();
}
