package de.mpg.mpdl.labcam.code.data.service.impl;

import com.google.gson.JsonObject;

import javax.inject.Inject;

import de.mpg.mpdl.labcam.code.data.model.ImejiFolderModel;
import de.mpg.mpdl.labcam.code.data.repository.ImejiFolderRepository;
import de.mpg.mpdl.labcam.code.data.service.ImejiFolderService;
import rx.Observable;

/**
 * Created by yingli on 3/17/17.
 */

public class ImejiFolderServiceImp extends ImejiFolderService{

    @Inject
    ImejiFolderRepository imejiFolderRepository;

    @Inject
    public ImejiFolderServiceImp(){

    }

    @Override
    public Observable<ImejiFolderModel> getCollectionById(String collectionId) {
        return imejiFolderRepository.getCollectionById(collectionId);
    }

    @Override
    public Observable<ImejiFolderModel> createCollection(JsonObject jsonBody) {
        return imejiFolderRepository.createCollection(jsonBody);
    }

    @Override
    public Observable<ImejiFolderModel> updateCollection(String collectionId, JsonObject jsonObject) {
        return imejiFolderRepository.updateCollection(collectionId, jsonObject);
    }
}
