package de.mpg.mpdl.labcam.code.data.repository.impl;

import com.google.gson.JsonObject;

import de.mpg.mpdl.labcam.code.data.model.ImejiFolderModel;
import de.mpg.mpdl.labcam.code.data.net.RetrofitFactory;
import de.mpg.mpdl.labcam.code.data.net.api.ImejiFolderApi;
import de.mpg.mpdl.labcam.code.data.repository.ImejiFolderRepository;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by yingli on 3/17/17.
 */

public class ImejiFolderRepositoryImpl implements ImejiFolderRepository{

    @Inject
    public ImejiFolderRepositoryImpl(){}

    @Override
    public Observable<ImejiFolderModel> getCollectionById(String collectionId) {
        return RetrofitFactory.getInstance().create(ImejiFolderApi.class).getCollectionById(collectionId);
    }

    @Override
    public Observable<ImejiFolderModel> createCollection(JsonObject jsonBody) {
        return RetrofitFactory.getInstance().create(ImejiFolderApi.class).createCollection(jsonBody);
    }

    @Override
    public Observable<ImejiFolderModel> updateCollection(String collectionId, JsonObject jsonObject) {
        return RetrofitFactory.getInstance().create(ImejiFolderApi.class).updateCollection(collectionId, jsonObject);
    }
}
