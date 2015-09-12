package it.jaschke.alexandria;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.Toast;

import it.jaschke.alexandria.api.Callback;
import it.jaschke.alexandria.databinding.ActivityMainBinding;
import it.jaschke.alexandria.fragments.BookDetail;
import it.jaschke.alexandria.fragments.ListOfBooks;


public class MainActivity extends BaseActivity implements Callback {

    /**
     * Used to store the last screen title.
     */
    private CharSequence title;
    public static boolean IS_TABLET = false;
    private BroadcastReceiver messageReceiver;

    public static final String MESSAGE_EVENT = "MESSAGE_EVENT";
    public static final String MESSAGE_KEY = "MESSAGE_EXTRA";

    ActivityMainBinding mActivityMainBinding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IS_TABLET = isTablet();
        if (IS_TABLET) {
//            setContentView(R.layout.activity_main_tablet);
//            mActivityMainBinding = DataBindingUtil.setContentView(this,R.layout.activity_main_tablet);
        } else {
            mActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        }

        messageReceiver = new MessageReciever();
        IntentFilter filter = new IntentFilter(MESSAGE_EVENT);
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver,filter);

        //toolbar
        setSupportActionBar(mActivityMainBinding.mainToolbar);

        //DrawerLayout - Commented out because NavigationView not necessary for now
        /*
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        ActionBarDrawerToggle actionBarDrawerToggle = new
                ActionBarDrawerToggle(this, drawerLayout, mActivityMainBinding.mainToolbar, R.string.open, R.string.close);
        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setHomeButtonEnabled(true);
        }
        actionBarDrawerToggle.syncState();

        mActivityMainBinding.drawerListView.setOnItemClickListener((parent, view, position, id) -> {

        });
        mActivityMainBinding.drawerListView.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_activated_1,
                android.R.id.text1,
                new String[]{
                        getString(R.string.books),
                        getString(R.string.about),
                }));
                */

        //load the list of books fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.includeContainer, new ListOfBooks())
                .addToBackStack((String) title)
                .commit();

        //fab
//        mActivityMainBinding.fab.setOnClickListener(view -> {
//            //launch the fragment for scan
//            getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.includeContainer, new AddBook())
//                    .addToBackStack((String) title)
//                    .commit();
//        });

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

    @Override
    public void onItemSelected(String ean) {
        Bundle args = new Bundle();
        args.putString(BookDetail.EAN_KEY, ean);

        BookDetail fragment = new BookDetail();
        fragment.setArguments(args);

        int id = R.id.includeContainer;
        if (findViewById(R.id.right_container) != null) {
            id = R.id.right_container;
        }

        getSupportFragmentManager().beginTransaction()
                .replace(id, fragment)
                .addToBackStack("Book Detail")
                .commit();

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

}