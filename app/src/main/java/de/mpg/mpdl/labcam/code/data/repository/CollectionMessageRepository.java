package de.mpg.mpdl.labcam.code.data.repository;

import de.mpg.mpdl.labcam.Model.MessageModel.CollectionMessage;
import rx.Observable;

/**
 * Created by yingli on 3/17/17.
 */

public interface CollectionMessageRepository {

    Observable<CollectionMessage> getCollections(String q,
                                                 int size,
                                                 int offset);

    Observable<CollectionMessage> getGrantedCollectionMessage(String q);
}
