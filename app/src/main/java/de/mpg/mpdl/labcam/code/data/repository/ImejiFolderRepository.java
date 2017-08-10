package de.mpg.mpdl.labcam.code.data.repository;

import com.google.gson.JsonObject;

import de.mpg.mpdl.labcam.code.data.model.ImejiFolderModel;
import rx.Observable;


/**
 * Created by yingli on 3/17/17.
 */

public interface ImejiFolderRepository {
    Observable<ImejiFolderModel> getCollectionById(String collectionId);

    Observable<ImejiFolderModel> createCollection(JsonObject jsonBody);

    Observable<ImejiFolderModel> updateCollection(String collectionId, JsonObject jsonObject);
}
