package de.mpg.mpdl.labcam.code.injection.module;

import de.mpg.mpdl.labcam.code.data.repository.ItemMessageRepository;
import de.mpg.mpdl.labcam.code.data.repository.impl.ItemMessageRepositoryImpl;
import de.mpg.mpdl.labcam.code.data.service.ItemMessageService;
import de.mpg.mpdl.labcam.code.data.service.impl.ItemMessageServiceImp;

import dagger.Module;
import dagger.Provides;

/**
 * Created by yingli on 3/20/17.
 */
@Module
public class ItemMessageModule {

    @Provides
    ItemMessageService provideItemMessageService(ItemMessageServiceImp service){
        return service;
    }

    @Provides
    ItemMessageRepository provideItemMessageRepository(ItemMessageRepositoryImpl repository){
        return repository;
    }
}
