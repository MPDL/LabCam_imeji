package de.mpg.mpdl.labcam.code.data.service.impl;

import de.mpg.mpdl.labcam.code.data.model.ImejiFolderModel;
import de.mpg.mpdl.labcam.code.data.repository.ImejiFolderRepository;
import de.mpg.mpdl.labcam.code.data.service.ImejiFolderService;

import javax.inject.Inject;

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
}
