package de.mpg.mpdl.labcam.code.data.net.api;

import com.google.gson.JsonObject;

import de.mpg.mpdl.labcam.code.data.model.ImejiFolderModel;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import rx.Observable;

/**
 * Created by yingli on 3/17/17.
 */

public interface ImejiFolderApi {

    //get collection by id
    @GET("collections/{id}")
    Observable<ImejiFolderModel> getCollectionById(@Path("id") String collectionId);

    @POST("collections")
    Observable<ImejiFolderModel> createCollection(@Body JsonObject jsonBody);

    @PUT("collections/{id}")
    Observable<ImejiFolderModel> updateCollection(@Path("id") String collectionId,
                          @Body JsonObject jsonObject);
}
