package it.jaschke.alexandria;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import javax.inject.Inject;

import it.jaschke.alexandria.components.AppComponent;

/**
 * Created by ahmedrizwan on 8/31/15.
 */
public class BaseActivity extends AppCompatActivity {

    @Inject
    public SharedPreferences mSharedPreferences;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getApplicationComponent().inject(this);
    }

    protected AppComponent getApplicationComponent() {
        return ((App)getApplication()).getAppComponent();
    }
}
