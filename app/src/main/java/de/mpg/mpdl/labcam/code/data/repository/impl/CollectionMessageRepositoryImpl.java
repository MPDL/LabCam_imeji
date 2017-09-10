package de.mpg.mpdl.labcam.code.data.repository.impl;

import javax.inject.Inject;

import de.mpg.mpdl.labcam.Model.MessageModel.CollectionMessage;
import de.mpg.mpdl.labcam.code.data.net.RetrofitFactory;
import de.mpg.mpdl.labcam.code.data.net.api.CollectionMessageApi;
import de.mpg.mpdl.labcam.code.data.repository.CollectionMessageRepository;
import rx.Observable;

/**
 * Created by yingli on 3/17/17.
 */

public class CollectionMessageRepositoryImpl implements CollectionMessageRepository{

    @Inject
    public CollectionMessageRepositoryImpl(){}

    @Override
    public Observable<CollectionMessage> getCollections(String q,
                                                        int size,
                                                        int offset) {
        return RetrofitFactory.getInstance().create(CollectionMessageApi.class).getCollections(q, size, offset);
    }

    @Override
    public Observable<CollectionMessage> getGrantedCollectionMessage(String q) {
        return RetrofitFactory.getInstance().create(CollectionMessageApi.class).getGrantedCollectionMessage(q);
    }
}
