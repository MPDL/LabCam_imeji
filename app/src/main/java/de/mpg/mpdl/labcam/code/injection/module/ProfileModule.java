package de.mpg.mpdl.labcam.code.injection.module;

import dagger.Module;
import dagger.Provides;
import de.mpg.mpdl.labcam.code.data.repository.ProfileRepository;
import de.mpg.mpdl.labcam.code.data.repository.impl.ProfileRepositoryImpl;
import de.mpg.mpdl.labcam.code.data.service.ProfileService;
import de.mpg.mpdl.labcam.code.data.service.impl.ProfileServiceImp;

/**
 * Created by yingli on 3/20/17.
 */

@Module
public class ProfileModule {
    @Provides
    ProfileService provideProfileService(ProfileServiceImp service){
        return service;
    }

    @Provides
    ProfileRepository provideProfileRepository(ProfileRepositoryImpl repository) {
        return  repository;
    }
}
