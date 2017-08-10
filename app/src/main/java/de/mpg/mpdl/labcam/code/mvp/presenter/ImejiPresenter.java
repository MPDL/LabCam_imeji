package de.mpg.mpdl.labcam.code.mvp.presenter;

import javax.inject.Inject;

import de.mpg.mpdl.labcam.Model.MessageModel.CollectionMessage;
import de.mpg.mpdl.labcam.Model.MessageModel.ItemMessage;
import de.mpg.mpdl.labcam.code.base.BaseAbstractPresenter;
import de.mpg.mpdl.labcam.code.base.BaseActivity;
import de.mpg.mpdl.labcam.code.base.BaseSubscriber;
import de.mpg.mpdl.labcam.code.data.service.CollectionMessageService;
import de.mpg.mpdl.labcam.code.data.service.ItemMessageService;
import de.mpg.mpdl.labcam.code.mvp.view.ImejiView;

/**
 * Created by yingli on 3/29/17.
 */

public class ImejiPresenter extends BaseAbstractPresenter<ImejiView> {

    @Inject
    ItemMessageService itemMessageService;

    @Inject
    CollectionMessageService collectionMessageService;


    @Inject
    public ImejiPresenter() {
    }

    public void getCollectionItems(String collectionId, int size, int offset, BaseActivity act) {
        if (!checkNetWork()) {
            return;
        }
        itemMessageService.execute(new BaseSubscriber<ItemMessage>(mView) {
            @Override
            public void onNext(ItemMessage model) {
                mView.getItemsSuc(model);
            }

            @Override
            public void onError(Throwable e) {
                mView.getItemsFail(e);
            }

        }, itemMessageService.getCollectionItems(collectionId, size, offset), act);
    }

    public void getCollectionMessage(BaseActivity act){
        if (!checkNetWork()) {
            return;
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
        }, collectionMessageService.getCollections(), act);
    }
}
