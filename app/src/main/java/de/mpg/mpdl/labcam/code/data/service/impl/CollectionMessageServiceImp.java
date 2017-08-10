package de.mpg.mpdl.labcam.code.data.service.impl;

import javax.inject.Inject;

import de.mpg.mpdl.labcam.Model.MessageModel.CollectionMessage;
import de.mpg.mpdl.labcam.code.data.repository.CollectionMessageRepository;
import de.mpg.mpdl.labcam.code.data.service.CollectionMessageService;
import rx.Observable;

/**
 * Created by yingli on 3/17/17.
 */

public class CollectionMessageServiceImp extends CollectionMessageService{

    @Inject
    CollectionMessageRepository collectionMessageRepository;

    @Inject
    public CollectionMessageServiceImp() {
    }

    @Override
    public Observable<CollectionMessage> getCollections() {
        return collectionMessageRepository.getCollections();
    }

    @Override
    public Observable<CollectionMessage> getGrantedCollectionMessage(String q) {
        return collectionMessageRepository.getGrantedCollectionMessage(q);
    }
}
