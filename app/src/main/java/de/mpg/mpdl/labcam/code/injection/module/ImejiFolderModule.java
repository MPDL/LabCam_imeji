package de.mpg.mpdl.labcam.code.injection.module;

import dagger.Module;
import dagger.Provides;
import de.mpg.mpdl.labcam.code.data.repository.ImejiFolderRepository;
import de.mpg.mpdl.labcam.code.data.repository.impl.ImejiFolderRepositoryImpl;
import de.mpg.mpdl.labcam.code.data.service.ImejiFolderService;
import de.mpg.mpdl.labcam.code.data.service.impl.ImejiFolderServiceImp;

/**
 * Created by yingli on 3/17/17.
 */
@Module
public class ImejiFolderModule {
    @Provides
    ImejiFolderService provideUserService(ImejiFolderServiceImp service){
        return service;
    }

    @Provides
    ImejiFolderRepository provideUserRepository(ImejiFolderRepositoryImpl repository){
        return repository;
    }
}
