package example.com.mpdlcamera.Retrofit;

import com.google.gson.JsonObject;

import java.util.List;

import example.com.mpdlcamera.Model.DataItem;
import example.com.mpdlcamera.Model.ImejiFolder;
import example.com.mpdlcamera.Model.ImejiProfile;
import example.com.mpdlcamera.Model.MessageModel.CollectionMessage;
import example.com.mpdlcamera.Model.MessageModel.ItemMessage;
import example.com.mpdlcamera.Model.TO.MetadataProfileTO;
import example.com.mpdlcamera.Model.User;
import retrofit.Callback;
import retrofit.client.Response;
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
import retrofit.mime.TypedString;

/**
 * Created by allen on 27/08/15.
 */
public interface ImejiAPI {

    /*
     *  For items
    */

    @GET("/items")
    void getItems(Callback<List<DataItem>> callback);

    //get one item by itemId
    @GET("/items/{id}?syntax=raw")
    List<DataItem> getItemById(@Path("id") String itemId,
                               Callback<Response> callback);

    @Multipart
    @POST("/items?syntax=raw")
    void postItem(@Part("file") TypedFile file,
                  @Part("json") String json,
                  Callback<DataItem> callback);

    @DELETE("/items/{id}")
    void deleteItemById(@Path("id") String itemId,
                        Callback<Response> callback);



    /*
     *  For users
    */

    //get all users
    @GET("/users")
    void getUsers(Callback<List<User>> callback);

    //get one User by userId
    @GET("/users/{userId}")
    List<User> getUserById(@Path("userId") String userId,
                           Callback<Response> callback);


    /**
     * login
     * @param callback
     */
    @POST("/login")
    void basicLogin(Callback<User> callback);


    /*
     *  For collections
    */
    //get all collections
    @GET(value = "/collections?size=30")
    void getCollections(Callback<CollectionMessage> callback);

    @GET(value = "/collections?size=30")
    void getCollectionMessage(Callback<JsonObject> callback);

    @GET(value = "/collections?size=30")
    void getGrantedCollectionMessage(@Query("q") String q,Callback<CollectionMessage> callback);

    //get all items by collection id
    @GET("/collections/{id}/items")
    void getCollectionItems(@Path("id") String collectionId,
                            @Query("size") int size,
                            @Query("offset") int offset,
                            Callback<ItemMessage> callback);

    //get collection by id
    @GET("/collections/{id}")
    void getCollectionById (@Path("id") String collectionId,
                            Callback<ImejiFolder> callback);

    @POST("/collections")
    void createCollection(@Body JsonObject jsonBody ,
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

    @PUT("/collections/{id}")
    void updateCollection(@Path("id") String collectionId,
            @Body JsonObject jsonObject,
            Callback<ImejiFolder> callback);
}
