package de.mpg.mpdl.labcam.code.data.repository;

import com.google.gson.JsonObject;

import de.mpg.mpdl.labcam.code.data.model.TO.MetadataProfileTO;
import retrofit2.Response;
import rx.Observable;

/**
 * Created by yingli on 3/17/17.
 */

public interface ProfileRepository {

    Observable<MetadataProfileTO> createProfile(JsonObject jsonObject);

    Observable<MetadataProfileTO> getProfileById(String profileId);

    Observable<Response> deleteProfileById(String profileId);
}
