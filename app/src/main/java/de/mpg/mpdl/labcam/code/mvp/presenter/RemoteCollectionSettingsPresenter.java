package de.mpg.mpdl.labcam.code.mvp.presenter;

import de.mpg.mpdl.labcam.Model.MessageModel.CollectionMessage;
import de.mpg.mpdl.labcam.code.base.BaseAbstractPresenter;
import de.mpg.mpdl.labcam.code.base.BaseActivity;
import de.mpg.mpdl.labcam.code.base.BaseSubscriber;
import de.mpg.mpdl.labcam.code.data.model.ImejiFolderModel;
import de.mpg.mpdl.labcam.code.data.service.CollectionMessageService;
import de.mpg.mpdl.labcam.code.data.service.ItemMessageService;
import de.mpg.mpdl.labcam.code.mvp.view.RemoteCollectionSettingsView;

import javax.inject.Inject;

/**
 * Created by yingli on 3/23/17.
 */

public class RemoteCollectionSettingsPresenter extends BaseAbstractPresenter<RemoteCollectionSettingsView> {
    @Inject
    CollectionMessageService collectionMessageService;

    @Inject
    public RemoteCollectionSettingsPresenter() {
    }

    public void getGrantedCollectionMessage(String q, BaseActivity act){
        if (!checkNetWork()) {
            return;
        }
        mView.showLoading();
        collectionMessageService.execute(new BaseSubscriber<CollectionMessage>(mView) {
            @Override
            public void onNext(CollectionMessage model) {
                mView.getCollectionsSuc(model);
            }

            @Override
            public void onError(Throwable e) {
                mView.getCollectionsFail(e);
            }
        }, collectionMessageService.getGrantedCollectionMessage(q), act);
    }

}
