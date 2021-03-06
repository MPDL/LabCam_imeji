package de.mpg.mpdl.labcam.code.mvp.presenter;

import com.google.gson.JsonObject;

import javax.inject.Inject;

import de.mpg.mpdl.labcam.Model.MessageModel.CollectionMessage;
import de.mpg.mpdl.labcam.code.base.BaseAbstractPresenter;
import de.mpg.mpdl.labcam.code.base.BaseActivity;
import de.mpg.mpdl.labcam.code.base.BaseSubscriber;
import de.mpg.mpdl.labcam.code.data.model.ImejiFolderModel;
import de.mpg.mpdl.labcam.code.data.service.CollectionMessageService;
import de.mpg.mpdl.labcam.code.data.service.ImejiFolderService;
import de.mpg.mpdl.labcam.code.mvp.view.RemoteCollectionSettingsView;

/**
 * Created by yingli on 3/23/17.
 */

public class RemoteCollectionSettingsPresenter extends BaseAbstractPresenter<RemoteCollectionSettingsView> {
    @Inject
    CollectionMessageService collectionMessageService;

    @Inject
    ImejiFolderService imejiFolderService;

    @Inject
    public RemoteCollectionSettingsPresenter() {
        // required constructor
    }

    public void getGrantedCollectionMessage(String q, BaseActivity act){
        if (!checkNetWork()) {
            mView.noInternet();
        }
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

    public void createCollection(JsonObject jsonBody, BaseActivity act){
        if (!checkNetWork()) {
            return;
        }
        imejiFolderService.execute(new BaseSubscriber<ImejiFolderModel>(mView) {
            @Override
            public void onNext(ImejiFolderModel model) {
                mView.createCollectionsSuc(model);
            }

            @Override
            public void onError(Throwable e) {
                mView.createCollectionsFail(e);
            }
        }, imejiFolderService.createCollection(jsonBody), act);
    }

}
