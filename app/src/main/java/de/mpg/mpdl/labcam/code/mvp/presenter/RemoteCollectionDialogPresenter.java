package de.mpg.mpdl.labcam.code.mvp.presenter;

import de.mpg.mpdl.labcam.Model.MessageModel.CollectionMessage;
import de.mpg.mpdl.labcam.code.base.BaseAbstractPresenter;
import de.mpg.mpdl.labcam.code.base.BaseActivity;
import de.mpg.mpdl.labcam.code.base.BaseSubscriber;
import de.mpg.mpdl.labcam.code.data.service.CollectionMessageService;
import de.mpg.mpdl.labcam.code.data.service.ImejiFolderService;
import de.mpg.mpdl.labcam.code.mvp.view.RemoteCollectionDialogView;

import javax.inject.Inject;

/**
 * Created by yingli on 3/28/17.
 */

public class RemoteCollectionDialogPresenter extends BaseAbstractPresenter<RemoteCollectionDialogView> {
    @Inject
    CollectionMessageService collectionMessageService;

    @Inject
    ImejiFolderService imejiFolderService;

    @Inject
    public RemoteCollectionDialogPresenter() {
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
