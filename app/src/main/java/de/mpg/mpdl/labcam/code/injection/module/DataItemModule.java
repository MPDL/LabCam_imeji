package de.mpg.mpdl.labcam.code.injection.module;

import de.mpg.mpdl.labcam.code.data.repository.DataItemRepository;
import de.mpg.mpdl.labcam.code.data.repository.impl.DataItemRepositoryImpl;
import de.mpg.mpdl.labcam.code.data.service.DataItemService;
import de.mpg.mpdl.labcam.code.data.service.impl.DataItemServiceImp;

import dagger.Module;
import dagger.Provides;

/**
 * Created by yingli on 3/20/17.
 */
@Module
public class DataItemModule {
    @Provides
    DataItemService provideDataItemService(DataItemServiceImp service){
        return service;
    }

    @Provides
    DataItemRepository provideDataItemRepository(DataItemRepositoryImpl repository){
        return repository;
    }
}
