package de.mpg.mpdl.labcam.code.injection.module;

import de.mpg.mpdl.labcam.code.data.repository.CollectionMessageRepository;
import de.mpg.mpdl.labcam.code.data.repository.impl.CollectionMessageRepositoryImpl;
import de.mpg.mpdl.labcam.code.data.service.CollectionMessageService;
import de.mpg.mpdl.labcam.code.data.service.impl.CollectionMessageServiceImp;

import dagger.Module;
import dagger.Provides;

/**
 * Created by yingli on 3/20/17.
 */
@Module
public class CollectionMessageModule {
    @Provides
    CollectionMessageService provideCollectionMessageService(CollectionMessageServiceImp service){
        return  service;
    }

    @Provides
    CollectionMessageRepository provideCollectionMessageRepository(CollectionMessageRepositoryImpl repository){
        return  repository;
    }

}
