package de.mpg.mpdl.labcam.code.injection.component;

import dagger.Component;
import de.mpg.mpdl.labcam.code.common.service.UploadService;
import de.mpg.mpdl.labcam.code.injection.PerActivity;
import de.mpg.mpdl.labcam.code.injection.module.UploadModule;

/**
 * Created by yingli on 3/28/17.
 */
@PerActivity
@Component(modules = {UploadModule.class})
public interface UploadComponent {
    void inject(UploadService service);
}
