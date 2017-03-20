package de.mpg.mpdl.labcam.code.data.repository;

import de.mpg.mpdl.labcam.Model.MessageModel.ItemMessage;

import rx.Observable;

/**
 * Created by yingli on 3/20/17.
 */

public interface ItemMessageRepository {
    Observable<ItemMessage> getCollectionItems(String collectionId, int size, int offset);
}
