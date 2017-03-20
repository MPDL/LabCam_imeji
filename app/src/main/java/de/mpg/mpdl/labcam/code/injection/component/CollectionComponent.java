package de.mpg.mpdl.labcam.code.injection.component;
import de.mpg.mpdl.labcam.code.activity.ItemsActivity;
import de.mpg.mpdl.labcam.code.injection.PerActivity;
import de.mpg.mpdl.labcam.code.injection.module.ActivityModule;

import de.mpg.mpdl.labcam.code.injection.module.ItemMessageModule;


import dagger.Component;

/**
 * Created by yingli on 3/20/17.
 */

@PerActivity
@Component(dependencies = ApplicationComponent.class, modules = {ActivityModule.class,
        ItemMessageModule.class})
public interface CollectionComponent {
    void inject(ItemsActivity baseActivity);
}
