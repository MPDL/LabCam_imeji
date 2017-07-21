package de.mpg.mpdl.labcam.code.injection.component;

import de.mpg.mpdl.labcam.code.activity.ItemsActivity;
import de.mpg.mpdl.labcam.code.activity.MainActivity;
import de.mpg.mpdl.labcam.code.activity.RemoteCollectionSettingsActivity;
import de.mpg.mpdl.labcam.code.common.fragment.CollectionViewFragment;
import de.mpg.mpdl.labcam.code.common.fragment.RemoteListDialogFragment;
import de.mpg.mpdl.labcam.code.injection.PerActivity;
import de.mpg.mpdl.labcam.code.injection.module.ActivityModule;
import de.mpg.mpdl.labcam.code.injection.module.CollectionMessageModule;
import de.mpg.mpdl.labcam.code.injection.module.ImejiFolderModule;
import de.mpg.mpdl.labcam.code.injection.module.ItemMessageModule;

import dagger.Component;

/**
 * Created by yingli on 3/20/17.
 */

@PerActivity
@Component(dependencies = ApplicationComponent.class, modules = {ActivityModule.class,
        ItemMessageModule.class, CollectionMessageModule.class, ImejiFolderModule.class})
public interface CollectionComponent {
    void inject(ItemsActivity baseActivity);
    void inject(RemoteCollectionSettingsActivity baseActivity);
    void inject(RemoteListDialogFragment baseActivity);
    void inject(MainActivity baseActivity);
    void inject(CollectionViewFragment baseActivity);
}
