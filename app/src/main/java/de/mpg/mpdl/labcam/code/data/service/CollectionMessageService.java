package de.mpg.mpdl.labcam.code.data.service;

import de.mpg.mpdl.labcam.Model.MessageModel.CollectionMessage;
import de.mpg.mpdl.labcam.code.base.BaseService;

import rx.Observable;

/**
 * Created by yingli on 3/17/17.
 */

public abstract class CollectionMessageService extends BaseService{

    public abstract Observable<CollectionMessage> getCollections();

    public abstract Observable<CollectionMessage> getGrantedCollectionMessage(String q);
}
