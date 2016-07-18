package example.com.mpdlcamera.Retrofit;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.List;

import example.com.mpdlcamera.Model.DataItem;
import example.com.mpdlcamera.Model.ImejiFolder;
import example.com.mpdlcamera.Model.ImejiProfile;
import example.com.mpdlcamera.Model.MessageModel.CollectionMessage;
import example.com.mpdlcamera.Model.MessageModel.ItemMessage;
import example.com.mpdlcamera.Model.User;
import retrofit.Callback;
import retrofit.client.Response;
import retrofit.mime.TypedFile;

/**
 * Created by allen on 27/08/15.
 */
public class RetrofitClient {
    private static String REST_SERVER;

    public static String getRestServer() {
        return REST_SERVER;
    }

    public static void setRestServer(String restServer) {
        REST_SERVER = restServer;
    }

    public static void login(String username, String password,Callback<User> callback){
        ImejiAPI imejiAPI = ServiceGenerator.
                createService(ImejiAPI.class, REST_SERVER, username, password);
        imejiAPI.basicLogin(callback);
    }

    public static void apiLogin(String APIkey,Callback<User> callback){
        ImejiAPI imejiAPI = ServiceGenerator.
                createService(ImejiAPI.class, REST_SERVER,APIkey);
        imejiAPI.basicLogin(callback);
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
                                  String APIkey) {
        ImejiAPI imejiAPI = ServiceGenerator.
            createService(ImejiAPI.class, REST_SERVER, APIkey);
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
    public static void getCollections(Callback<CollectionMessage> callback,
                                      String APIkey) {
        ImejiAPI imejiAPI = ServiceGenerator.createService(ImejiAPI.class, REST_SERVER, APIkey);

        imejiAPI.getCollections(callback);
    }



    public static void getCollectionMessage(Callback<JsonObject> callback,
                                            String APIkey) {
        ImejiAPI imejiAPI = ServiceGenerator.createService(ImejiAPI.class, REST_SERVER, APIkey);

        imejiAPI.getCollectionMessage(callback);
    }

    public static void getGrantCollectionMessage(Callback<CollectionMessage> callback,
                                            String APIkey) {
        ImejiAPI imejiAPI = ServiceGenerator.createService(ImejiAPI.class, REST_SERVER, APIkey);

        String q = "grant=\"upload\"";
        imejiAPI.getGrantedCollectionMessage(q, callback);
    }



    public static void getCollectionItems(String collectionId,
                                          int offset,
                                      Callback<ItemMessage> callback,
                                      String APIkey) {
        ImejiAPI imejiAPI = ServiceGenerator.createService(ImejiAPI.class, REST_SERVER, APIkey);

        // Fetch and print a list of the items to this library.
        imejiAPI.getCollectionItems(collectionId,10, offset, callback);
    }

    //getCollectionById
    public static void getCollectionById(String collectionId,
                                          Callback<ImejiFolder> callback,
                                          String APIkey) {
        ImejiAPI imejiAPI = ServiceGenerator.createService(ImejiAPI.class, REST_SERVER, APIkey);

        // Fetch and print a list of the items to this library.
        imejiAPI.getCollectionById(collectionId, callback);
    }

    //createCollection
    public static void createCollection(String title,
                                        String description,
                                         Callback<ImejiFolder> callback,
                                         String APIkey) {
        ImejiAPI imejiAPI = ServiceGenerator.createService(ImejiAPI.class, REST_SERVER, APIkey);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("title",title);
        jsonObject.addProperty("description",description);
        System.out.println(jsonObject);
        // Fetch and print a list of the items to this library.
        imejiAPI.createCollection(jsonObject, callback);
    }

    /** meta data and profile **/

    /**
     * createProfile
     * @param jsonStr
     * @param callback
     * @param APIkey
     */
    public static void createProfile(String jsonStr,
                                     Callback<ImejiProfile> callback, String APIkey) {
        ImejiAPI imejiAPI = ServiceGenerator.createService(ImejiAPI.class, REST_SERVER, APIkey);
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(jsonStr).getAsJsonObject();
        imejiAPI.createProfile(jsonObject, callback);
    }

    /**
     * updateCollection
     * @param collectionId
     * @param jsonObject
     * @param callback
     * @param APIkey
     */
    public static void updateCollection(String collectionId,
                                        JsonObject jsonObject,
                                     Callback<ImejiFolder> callback, String APIkey) {
        ImejiAPI imejiAPI = ServiceGenerator.createService(ImejiAPI.class, REST_SERVER, APIkey);
        imejiAPI.updateCollection(collectionId,jsonObject, callback);
    }
}
