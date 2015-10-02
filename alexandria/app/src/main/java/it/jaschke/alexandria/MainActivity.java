package it.jaschke.alexandria;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import it.jaschke.alexandria.databinding.ActivityMainBinding;


public class MainActivity extends BaseActivity {

    /**
     * Used to store the last screen title.
     */
    private CharSequence title;
    public static boolean IS_TABLET = false;
    private BroadcastReceiver messageReceiver;

    public static final String MESSAGE_EVENT = "MESSAGE_EVENT";
    public static final String MESSAGE_KEY = "MESSAGE_EXTRA";

    ActivityMainBinding mActivityMainBinding;

    public boolean tabletMode() {
        return mActivityMainBinding.listContainer != null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        //toolbar
        setSupportActionBar(mActivityMainBinding.mainToolbar);

        if (savedInstanceState == null)
            FragmentNavigation.launchBookListFragment(this, mActivityMainBinding);

        messageReceiver = new MessageReciever();
        IntentFilter filter = new IntentFilter(MESSAGE_EVENT);
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(messageReceiver, filter);

    }


    public void setTitle(int titleId) {
        title = getString(titleId);
    }


    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(messageReceiver);
        super.onDestroy();
    }


    public ActivityMainBinding getBinding() {
        return mActivityMainBinding;
    }

    private class MessageReciever extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getStringExtra(MESSAGE_KEY) != null) {
                Toast.makeText(MainActivity.this, intent.getStringExtra(MESSAGE_KEY), Toast.LENGTH_LONG)
                        .show();
            }
        }
    }

    public void goBack(View view) {
        getSupportFragmentManager().popBackStack();
    }

    private boolean isTablet() {
        return (getApplicationContext().getResources()
                .getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() < 2) {
            finish();
        }
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_about:
                new MaterialDialog.Builder(this)
                        .title(R.string.about)
                        .content(R.string.about_text)
                        .positiveText(R.string.close)
                        .show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}