package de.mpg.mpdl.labcam.Retrofit;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import de.mpg.mpdl.labcam.Model.DataItem;
import de.mpg.mpdl.labcam.Model.ImejiFolder;
import de.mpg.mpdl.labcam.Model.MessageModel.CollectionMessage;
import de.mpg.mpdl.labcam.Model.MessageModel.ItemMessage;
import de.mpg.mpdl.labcam.code.data.model.TO.MetadataProfileTO;
import de.mpg.mpdl.labcam.code.data.net.RetrofitFactory;
import de.mpg.mpdl.labcam.code.data.net.api.DataItemApi;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit.Callback;
import retrofit.mime.TypedFile;
import rx.Observable;

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

    public static Observable<DataItem> uploadItem(Map<String, RequestBody> partMap, MultipartBody.Part img) {
       return RetrofitFactory.getInstance().create(DataItemApi.class).uploadItem(partMap, img);
    }

    public static void uploadItem(TypedFile typedFile,
                                  String json,
                                  Callback<DataItem> callback,
                                  String APIkey) {
        ImejiAPI imejiAPI = ServiceGenerator.
            createService(ImejiAPI.class, REST_SERVER, APIkey);
        imejiAPI.uploadItem(typedFile, json, callback);
    }

    public static void updateItem(String itemId, TypedFile typedFile,
                                  String json,
                                  Callback<DataItem> callback,
                                  String APIkey) {
        ImejiAPI imejiAPI = ServiceGenerator.
                createService(ImejiAPI.class, REST_SERVER, APIkey);
        imejiAPI.putItem(itemId, typedFile, json, callback);
    }

    /*
        for collection
     */
    public static void getCollections(Callback<CollectionMessage> callback,
                                      String APIkey) {
        ImejiAPI imejiAPI = ServiceGenerator.createService(ImejiAPI.class, REST_SERVER, APIkey);

        imejiAPI.getCollections(callback);
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
                                     Callback<MetadataProfileTO> callback, String APIkey) {
        ImejiAPI imejiAPI = ServiceGenerator.createService(ImejiAPI.class, REST_SERVER, APIkey);
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(jsonStr).getAsJsonObject();
        imejiAPI.createProfile(jsonObject, callback);
    }


    /**
     * get Profile
     * @param profileId
     * @param callback
     * @param APIkey
     */
    public static void getProfileById(String profileId,
                                      Callback<MetadataProfileTO> callback, String APIkey){
        ImejiAPI imejiAPI = ServiceGenerator.createService(ImejiAPI.class, REST_SERVER, APIkey);
        imejiAPI.getProfileById(profileId,callback);
    }

    /**
     * delete Profile
     * @param profileId
     * @param callback
     * @param APIkey
     */
    public static void deleteProfileById(String profileId,
                                      Callback<String> callback, String APIkey){
        ImejiAPI imejiAPI = ServiceGenerator.createService(ImejiAPI.class, REST_SERVER, APIkey);
        imejiAPI.deleteProfileById(profileId,callback);
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
