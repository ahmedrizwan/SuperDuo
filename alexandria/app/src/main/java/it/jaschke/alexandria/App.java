package it.jaschke.alexandria;

import android.app.Application;

import it.jaschke.alexandria.components.AppComponent;
import it.jaschke.alexandria.components.DaggerAppComponent;
import it.jaschke.alexandria.modules.AppModule;

/**
 * Created by ahmedrizwan on 8/31/15.
 */
public class App extends Application {

    private AppComponent mAppComponent;

    @Override
    public void onCreate() {
        super.onCreate();
         mAppComponent = DaggerAppComponent.builder().appModule(new AppModule(this)).build();
    }

    public AppComponent getAppComponent(){
        return mAppComponent;
    }

}
