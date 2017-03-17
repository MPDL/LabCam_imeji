package de.mpg.mpdl.labcam.code.mvp.view;

import de.mpg.mpdl.labcam.code.base.BaseView;
import de.mpg.mpdl.labcam.code.data.model.UserModel;

/**
 * Created by yingli on 3/16/17.
 */

public interface LoginView extends BaseView{
    void loginSuc(UserModel model);
    void loginFail(Throwable e);
}
