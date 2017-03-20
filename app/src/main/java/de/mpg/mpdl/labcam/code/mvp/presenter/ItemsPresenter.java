package de.mpg.mpdl.labcam.code.mvp.presenter;

import de.mpg.mpdl.labcam.Model.MessageModel.ItemMessage;
import de.mpg.mpdl.labcam.code.base.BaseAbstractPresenter;
import de.mpg.mpdl.labcam.code.base.BaseActivity;
import de.mpg.mpdl.labcam.code.base.BaseSubscriber;
import de.mpg.mpdl.labcam.code.data.service.ItemMessageService;
import de.mpg.mpdl.labcam.code.mvp.view.ItemsView;

import javax.inject.Inject;

/**
 * Created by yingli on 3/20/17.
 */

public class ItemsPresenter extends BaseAbstractPresenter<ItemsView> {
    @Inject
    ItemMessageService itemMessageService;

    @Inject
    public ItemsPresenter() {
    }

    public void getCollectionItems(String collectionId, int size, int offset, BaseActivity act) {
        if (!checkNetWork()) {
            return;
        }
        mView.showLoading();
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
}
