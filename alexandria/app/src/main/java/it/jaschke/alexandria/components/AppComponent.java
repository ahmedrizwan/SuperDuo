package it.jaschke.alexandria.components;

import javax.inject.Singleton;

import dagger.Component;
import it.jaschke.alexandria.BaseActivity;
import it.jaschke.alexandria.modules.AppModule;

/**
 * Created by ahmedrizwan on 8/30/15.
 */
@Component(modules = {AppModule.class})
@Singleton
public interface AppComponent {

    void inject(BaseActivity baseActivity);

}
