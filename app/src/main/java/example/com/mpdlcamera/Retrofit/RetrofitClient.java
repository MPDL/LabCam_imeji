package example.com.mpdlcamera.Retrofit;

import java.util.List;

import example.com.mpdlcamera.Model.DataItem;
import example.com.mpdlcamera.Model.ImejiFolder;
import example.com.mpdlcamera.Utils.DeviceStatus;
import retrofit.Callback;
import retrofit.client.Response;
import retrofit.mime.TypedFile;

/**
 * Created by allen on 27/08/15.
 */
public class RetrofitClient {
    private final String LOG_TAG = RetrofitClient.class.getSimpleName();
    private static final String REST_SERVER = DeviceStatus.BASE_URL;



    public static void login(String username, String password){
        ImejiAPI imejiAPI = ServiceGenerator.
                createService(ImejiAPI.class, REST_SERVER, username, password);
        imejiAPI.basicLogin();
    }


    /*
    for items
     */
    public static void getItems(Callback<List<DataItem>> callback,
                                String username,
                                String password) {
        ImejiAPI imejiAPI = ServiceGenerator.createService(ImejiAPI.class, REST_SERVER, username, password);
        imejiAPI.getItems(callback);
    }
    public static void uploadItem(TypedFile typedFile,
                                  String json,
                                  Callback<DataItem> callback,
                                  String username,
                                  String password) {
        ImejiAPI imejiAPI = ServiceGenerator.
            createService(ImejiAPI.class, REST_SERVER, username, password);
        imejiAPI.postItem(typedFile, json, callback);
    }


    public static void deleteItem(String itemId,
                                  Callback<Response> callback,
                                  String username,
                                  String password) {
        ImejiAPI imejiAPI = ServiceGenerator.createService(ImejiAPI.class, REST_SERVER,
                username, password);
        imejiAPI.deleteItemById(itemId, callback);
    }



    /*
        for collection
     */
    public static void getCollections(Callback<List<ImejiFolder>> callback,
                                String username,
                                String password) {
        ImejiAPI imejiAPI = ServiceGenerator.createService(ImejiAPI.class, REST_SERVER, username, password);

        imejiAPI.getCollections(callback);
    }

    public static void getCollectionItems(String collectionId,
                                      Callback<List<DataItem>> callback,
                                      String username,
                                      String password) {
        ImejiAPI imejiAPI = ServiceGenerator.createService(ImejiAPI.class, REST_SERVER, username, password);

        // Fetch and print a list of the items to this library.
        imejiAPI.getCollectionItems(collectionId, callback);
    }




}
