package de.mpg.mpdl.labcam.code.data.repository.impl;

import de.mpg.mpdl.labcam.Model.MessageModel.CollectionMessage;
import de.mpg.mpdl.labcam.code.data.net.RetrofitFactory;
import de.mpg.mpdl.labcam.code.data.net.api.CollectionMessageApi;
import de.mpg.mpdl.labcam.code.data.repository.CollectionMessageRepository;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by yingli on 3/17/17.
 */

public class CollectionMessageRepositoryImpl implements CollectionMessageRepository{

    @Inject
    public CollectionMessageRepositoryImpl(){}

    @Override
    public Observable<CollectionMessage> getCollections() {
        return RetrofitFactory.getInstance().create(CollectionMessageApi.class).getCollections();
    }

    @Override
    public Observable<CollectionMessage> getGrantedCollectionMessage(String q) {
        return RetrofitFactory.getInstance().create(CollectionMessageApi.class).getGrantedCollectionMessage(q);
    }
}
