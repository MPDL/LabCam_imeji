package de.mpg.mpdl.labcam.code.injection.module;

import de.mpg.mpdl.labcam.code.data.repository.UserRepository;
import de.mpg.mpdl.labcam.code.data.repository.impl.UserRepositoryImpl;
import de.mpg.mpdl.labcam.code.data.service.UserService;
import de.mpg.mpdl.labcam.code.data.service.impl.UserServiceImp;

import dagger.Module;
import dagger.Provides;

/**
 * Created by yingli on 3/16/17.
 */
@Module
public class UserModule {
    @Provides
    UserService provideUserService(UserServiceImp service){
        return service;
    }

    @Provides
    UserRepository provideUserRepository(UserRepositoryImpl repository){
        return repository;
    }
}
