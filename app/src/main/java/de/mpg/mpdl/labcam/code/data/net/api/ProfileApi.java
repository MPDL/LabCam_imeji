package de.mpg.mpdl.labcam.code.data.net.api;

import com.google.gson.JsonObject;

import de.mpg.mpdl.labcam.code.data.model.TO.MetadataProfileTO;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

/**
 * Created by yingli on 3/17/17.
 */

public interface ProfileApi {
    //get collection by id
    @GET("profiles")
    Observable<MetadataProfileTO> createProfile(@Body JsonObject jsonObject);

    @GET("/profiles/{id}")
    Observable<MetadataProfileTO> getProfileById(@Path("id") String profileId);

    @DELETE("profiles/{id}")
    Observable<Response> deleteProfileById(@Path("id") String profileId);
}
