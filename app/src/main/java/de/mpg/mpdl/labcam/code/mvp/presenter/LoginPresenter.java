package de.mpg.mpdl.labcam.code.mvp.presenter;

import de.mpg.mpdl.labcam.Model.User;
import de.mpg.mpdl.labcam.code.base.BaseAbstractPresenter;
import de.mpg.mpdl.labcam.code.base.BaseActivity;
import de.mpg.mpdl.labcam.code.base.BaseSubscriber;
import de.mpg.mpdl.labcam.code.data.model.ImejiFolderModel;
import de.mpg.mpdl.labcam.code.data.model.UserModel;
import de.mpg.mpdl.labcam.code.data.service.ImejiFolderService;
import de.mpg.mpdl.labcam.code.data.service.UserService;
import de.mpg.mpdl.labcam.code.mvp.view.LoginView;

import javax.inject.Inject;

/**
 * Created by yingli on 3/16/17.
 */

public class LoginPresenter extends BaseAbstractPresenter<LoginView> {
    @Inject
    UserService userService;

    @Inject
    ImejiFolderService imejiFolderService;

    @Inject
    public LoginPresenter() {
    }

    public void basicLogin(BaseActivity act) {
        if (!checkNetWork()) {
            return;
        }
        mView.showLoading();
        userService.execute(new BaseSubscriber<UserModel>(mView) {
            @Override
            public void onNext(UserModel model) {
                mView.loginSuc(model);
            }

            @Override
            public void onError(Throwable e) {
                mView.loginFail(e);
            }

        }, userService.basicLogin(), act);
    }


    public void getCollectionById(String collectionId, BaseActivity act){
        if (!checkNetWork()) {
            return;
        }
        mView.showLoading();
        imejiFolderService.execute(new BaseSubscriber<ImejiFolderModel>(mView) {
            @Override
            public void onNext(ImejiFolderModel model) {
                mView.getCollectionByIdSuc(model);
            }

            @Override
            public void onError(Throwable e) {
                mView.getCollectionByIdFail(e);
            }

        }, imejiFolderService.getCollectionById(collectionId), act);
    }
}
