package de.mpg.mpdl.labcam.code.data.repository.impl;

import de.mpg.mpdl.labcam.Model.MessageModel.ItemMessage;
import de.mpg.mpdl.labcam.code.data.net.RetrofitFactory;
import de.mpg.mpdl.labcam.code.data.net.api.ItemMessageApi;
import de.mpg.mpdl.labcam.code.data.repository.ItemMessageRepository;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by yingli on 3/20/17.
 */

public class ItemMessageRepositoryImpl implements ItemMessageRepository{

    @Inject
    public ItemMessageRepositoryImpl() {
    }

    @Override
    public Observable<ItemMessage> getCollectionItems(String collectionId, int size, int offset) {
        return RetrofitFactory.getInstance().create(ItemMessageApi.class).getCollectionItems(collectionId, size, offset);
    }
}
