package de.mpg.mpdl.labcam.code.injection.component;

import de.mpg.mpdl.labcam.code.activity.LoginActivity;
import de.mpg.mpdl.labcam.code.injection.PerActivity;
import de.mpg.mpdl.labcam.code.injection.module.ActivityModule;
import de.mpg.mpdl.labcam.code.injection.module.ImejiFolderModule;
import de.mpg.mpdl.labcam.code.injection.module.UserModule;

import dagger.Component;

/**
 * Created by yingli on 3/16/17.
 */
@PerActivity
@Component(dependencies = ApplicationComponent.class, modules = {ActivityModule.class,
        UserModule.class, ImejiFolderModule.class})
public interface UserComponent {
    void inject(LoginActivity baseActivity);
}
