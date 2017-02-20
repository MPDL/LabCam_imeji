package de.mpg.mpdl.labcam.code.base;

import android.app.Application;
import android.util.Log;

import de.mpg.mpdl.labcam.code.injection.component.ApplicationComponent;
import de.mpg.mpdl.labcam.code.injection.component.DaggerApplicationComponent;
import de.mpg.mpdl.labcam.code.injection.module.ApplicationModule;


public class BaseApplication extends Application {

    private ApplicationComponent applicationComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        initializeInjector();

    }

    private void initializeInjector() {
        this.applicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();
    }

    public ApplicationComponent getApplicationComponent() {
        return this.applicationComponent;
    }

    @Override
    public void onLowMemory() {
        Log.d("***", "memory_low");
        super.onLowMemory();
    }
}
