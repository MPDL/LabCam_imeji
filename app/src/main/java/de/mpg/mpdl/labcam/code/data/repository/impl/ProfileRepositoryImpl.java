package de.mpg.mpdl.labcam.code.data.repository.impl;

import com.google.gson.JsonObject;

import de.mpg.mpdl.labcam.code.data.model.TO.MetadataProfileTO;
import de.mpg.mpdl.labcam.code.data.net.RetrofitFactory;
import de.mpg.mpdl.labcam.code.data.net.api.ProfileApi;
import de.mpg.mpdl.labcam.code.data.repository.ProfileRepository;

import javax.inject.Inject;

import retrofit2.Response;
import rx.Observable;

/**
 * Created by yingli on 3/17/17.
 */

public class ProfileRepositoryImpl implements ProfileRepository{
    @Inject
    public ProfileRepositoryImpl() {
    }

    @Override
    public Observable<MetadataProfileTO> createProfile(JsonObject jsonObject) {
        return RetrofitFactory.getInstance().create(ProfileApi.class).createProfile(jsonObject);
    }

    @Override
    public Observable<MetadataProfileTO> getProfileById(String profileId) {
        return RetrofitFactory.getInstance().create(ProfileApi.class).getProfileById(profileId);
    }

    @Override
    public Observable<Response> deleteProfileById(String profileId) {
        return RetrofitFactory.getInstance().create(ProfileApi.class).deleteProfileById(profileId);
    }
}
