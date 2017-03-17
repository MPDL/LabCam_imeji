package de.mpg.mpdl.labcam.code.data.net.api;

import de.mpg.mpdl.labcam.code.data.model.ImejiFolderModel;


import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

/**
 * Created by yingli on 3/17/17.
 */

public interface ImejiFolderApi {

    //get collection by id
    @GET("collections/{id}")
    Observable<ImejiFolderModel> getCollectionById(@Path("id") String collectionId);
}
