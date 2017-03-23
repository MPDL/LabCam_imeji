package de.mpg.mpdl.labcam.code.data.service.impl;

import com.google.gson.JsonObject;

import de.mpg.mpdl.labcam.code.data.model.TO.MetadataProfileTO;
import de.mpg.mpdl.labcam.code.data.repository.ProfileRepository;
import de.mpg.mpdl.labcam.code.data.service.ProfileService;

import javax.inject.Inject;

import retrofit2.Response;
import rx.Observable;

/**
 * Created by yingli on 3/17/17.
 */

public class ProfileServiceImp extends ProfileService{

    @Inject
    ProfileRepository profileRepository;

    @Inject
    public ProfileServiceImp() {
    }

    @Override
    public Observable<MetadataProfileTO> createProfile(JsonObject jsonObject) {
        return profileRepository.createProfile(jsonObject);
    }

    @Override
    public Observable<MetadataProfileTO> getProfileById(String profileId) {
        return profileRepository.getProfileById(profileId);
    }

    @Override
    public Observable<Response> deleteProfileById(String profileId) {
        return profileRepository.deleteProfileById(profileId);
    }
}
