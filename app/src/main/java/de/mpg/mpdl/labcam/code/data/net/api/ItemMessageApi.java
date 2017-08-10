package de.mpg.mpdl.labcam.code.data.net.api;

import de.mpg.mpdl.labcam.Model.MessageModel.ItemMessage;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;


/**
 * Created by yingli on 3/17/17.
 */

public interface ItemMessageApi {

    //get all items by collection id
    @GET("collections/{id}/items")
    Observable<ItemMessage> getCollectionItems(@Path("id") String collectionId,
                                  @Query("size") int size,
                                  @Query("offset") int offset);
}
