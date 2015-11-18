package example.com.mpdlcamera.Retrofit;

import java.util.List;

import example.com.mpdlcamera.Model.ImejiFolder;
import example.com.mpdlcamera.Model.DataItem;
import example.com.mpdlcamera.Model.User;
import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.http.Path;
import retrofit.mime.TypedFile;

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

    @POST("/login")
    User basicLogin();


    /*
     *  For collections
    */
    //get all collections
    @GET("/collections")
    void getCollections(Callback<List<ImejiFolder>> callback);

    //get all items by collection id
    @GET("/collections/{id}/items?size=100")
    void getCollectionItems(@Path("id") String collectionId,
                            Callback<List<DataItem>> callback);



}
