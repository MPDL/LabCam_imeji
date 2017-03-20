package de.mpg.mpdl.labcam.code.data.service;

import de.mpg.mpdl.labcam.Model.MessageModel.ItemMessage;
import de.mpg.mpdl.labcam.code.base.BaseService;

import rx.Observable;

/**
 * Created by yingli on 3/20/17.
 */

public abstract class ItemMessageService extends BaseService{
    public abstract Observable<ItemMessage> getCollectionItems(String collectionId, int size, int offset);
}
