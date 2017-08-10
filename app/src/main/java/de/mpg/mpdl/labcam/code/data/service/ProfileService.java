package de.mpg.mpdl.labcam.code.data.service;

import com.google.gson.JsonObject;

import de.mpg.mpdl.labcam.code.base.BaseService;
import de.mpg.mpdl.labcam.code.data.model.TO.MetadataProfileTO;
import retrofit2.Response;
import rx.Observable;

/**
 * Created by yingli on 3/17/17.
 */

public abstract class ProfileService extends BaseService{

    public abstract Observable<MetadataProfileTO> createProfile(JsonObject jsonObject);

    public abstract Observable<MetadataProfileTO> getProfileById(String profileId);

    public abstract Observable<Response> deleteProfileById(String profileId);
}
