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

    public static void setRestServer(String restServer) {
        REST_SERVER = restServer;
    }

    public static void uploadItem(TypedFile typedFile,
                                  String json,
                                  Callback<DataItem> callback,
                                  String APIkey) {
        ImejiAPI imejiAPI = ServiceGenerator.
            createService(ImejiAPI.class, REST_SERVER, APIkey);
        imejiAPI.uploadItem(typedFile, json, callback);
    }


    //getCollectionById
    public static void getCollectionById(String collectionId,
                                          Callback<ImejiFolder> callback,
                                          String APIkey) {
        ImejiAPI imejiAPI = ServiceGenerator.createService(ImejiAPI.class, REST_SERVER, APIkey);

        // Fetch and print a list of the items to this library.
        imejiAPI.getCollectionById(collectionId, callback);
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
