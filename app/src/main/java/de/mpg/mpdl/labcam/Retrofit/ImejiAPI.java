package de.mpg.mpdl.labcam.Retrofit;

import com.google.gson.JsonObject;

import de.mpg.mpdl.labcam.Model.DataItem;
import de.mpg.mpdl.labcam.Model.ImejiFolder;
import de.mpg.mpdl.labcam.Model.MessageModel.CollectionMessage;
import de.mpg.mpdl.labcam.Model.MessageModel.ItemMessage;
import de.mpg.mpdl.labcam.code.data.model.TO.MetadataProfileTO;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Part;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.mime.TypedFile;

/**
 * Created by allen on 27/08/15.
 */
public interface ImejiAPI {

    /*
     *  For items
    */

    @Multipart
    @POST("/items?syntax=raw")
    void uploadItem(@Part("file") TypedFile file,
                  @Part("json") String json,
                  Callback<DataItem> callback);

    @Multipart
    @POST("/items/{id}?syntax=raw")
    void putItem(@Path("id") String itemId,
                 @Part("file") TypedFile file,
                  @Part("json") String json,
                  Callback<DataItem> callback);


    /*
     *  For collections
    */
    //get all collections
    @GET(value = "/collections?size=10000")
    void getCollections(Callback<CollectionMessage> callback);

    @GET(value = "/collections?size=10000")
    void getGrantedCollectionMessage(@Query("q") String q, Callback<CollectionMessage> callback);

    //get all items by collection id
    @GET("/collections/{id}/items")
    void getCollectionItems(@Path("id") String collectionId,
                            @Query("size") int size,
                            @Query("offset") int offset,
                            Callback<ItemMessage> callback);

    //get collection by id
    @GET("/collections/{id}")
    void getCollectionById(@Path("id") String collectionId,
                           Callback<ImejiFolder> callback);

    @POST("/collections")
    void createCollection(@Body JsonObject jsonBody,
                          Callback<ImejiFolder> callback) ;

    /**
     * uploading
     */

    @POST("/profiles")
    void createProfile(@Body JsonObject jsonObject,
                       Callback<MetadataProfileTO> callback);

    @GET("/profiles/{id}")
    void getProfileById(@Path("id") String profileId,
                        Callback<MetadataProfileTO> callback);

    @DELETE("/profiles/{id}")
    void deleteProfileById(@Path("id") String profileId,
                           Callback<String> callback);


    @PUT("/collections/{id}")
    void updateCollection(@Path("id") String collectionId,
                          @Body JsonObject jsonObject,
                          Callback<ImejiFolder> callback);
}
