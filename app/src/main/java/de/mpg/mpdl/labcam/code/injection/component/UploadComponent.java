package de.mpg.mpdl.labcam.code.injection.component;

import de.mpg.mpdl.labcam.code.common.service.UploadService;
import de.mpg.mpdl.labcam.code.injection.PerActivity;
import de.mpg.mpdl.labcam.code.injection.module.ActivityModule;
import de.mpg.mpdl.labcam.code.injection.module.DataItemModule;

import dagger.Component;

/**
 * Created by yingli on 3/28/17.
 */
@PerActivity
@Component(dependencies = ApplicationComponent.class, modules = {ActivityModule.class,
        DataItemModule.class})
public interface UploadComponent {
    void inject(UploadService baseActivity);
}
