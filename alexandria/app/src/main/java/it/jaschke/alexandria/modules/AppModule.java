package it.jaschke.alexandria.modules;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import it.jaschke.alexandria.R;

/**
 * Created by ahmedrizwan on 8/30/15.
 */
@Module
public class AppModule {

    private static Application sApplication;

    public AppModule(Application application) {
        sApplication = application;
    }

    @Provides
    @Singleton
    public Context providesContext(){
        return sApplication.getApplicationContext();
    }

    @Provides
    @Singleton
    public SharedPreferences providesSharedPreferences(Context context){
        return context.getSharedPreferences(context.getString(R.string.shared_prefs), Context.MODE_PRIVATE);
    }
    
}
