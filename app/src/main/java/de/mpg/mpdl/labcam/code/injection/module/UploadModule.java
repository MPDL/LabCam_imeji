package de.mpg.mpdl.labcam.code.injection.module;

import dagger.Module;
import dagger.Provides;
import de.mpg.mpdl.labcam.code.data.repository.UploadRepository;
import de.mpg.mpdl.labcam.code.data.repository.impl.UploadRepositoryImpl;

/**
 * Created by yingli on 8/10/17.
 */

@Module
public class UploadModule {

    @Provides
    UploadRepository provideUploadRepository(UploadRepositoryImpl repository){
        return repository;
    }

}
