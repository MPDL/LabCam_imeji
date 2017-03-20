package de.mpg.mpdl.labcam.code.data.service.impl;

import de.mpg.mpdl.labcam.Model.MessageModel.ItemMessage;
import de.mpg.mpdl.labcam.code.data.repository.ItemMessageRepository;
import de.mpg.mpdl.labcam.code.data.service.ItemMessageService;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by yingli on 3/20/17.
 */

public class ItemMessageServiceImp extends ItemMessageService{

    @Inject
    ItemMessageRepository itemMessageRepository;

    @Inject
    public ItemMessageServiceImp() {
    }

    @Override
    public Observable<ItemMessage> getCollectionItems(String collectionId, int size, int offset) {
        return itemMessageRepository.getCollectionItems(collectionId, size, offset);
    }
}
