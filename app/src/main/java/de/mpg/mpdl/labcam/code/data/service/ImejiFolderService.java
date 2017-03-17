package de.mpg.mpdl.labcam.code.data.service;

import de.mpg.mpdl.labcam.code.base.BaseService;
import de.mpg.mpdl.labcam.code.data.model.ImejiFolderModel;


import rx.Observable;

/**
 * Created by yingli on 3/17/17.
 */

public abstract class ImejiFolderService extends BaseService{

    public abstract Observable<ImejiFolderModel> getCollectionById(String collectionId);
}
