package barqsoft.footballscores.activities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilities;
import barqsoft.footballscores.adapters.ScoresPageAdapter;
import barqsoft.footballscores.fragments.ScoresFragment;
import barqsoft.footballscores.sync.SyncAdapter;
import de.greenrobot.event.EventBus;

/**
 * Created by ahmedrizwan on 14/10/2015.
 */
public class MainActivity extends AppCompatActivity {
    private final int NUM_PAGES = 5;
    public static int selected_match_id = 0;
    int current_fragment = 2;
    final String FOOTBALL_ACTION = "barqsoft.footballscores.update";

    public static int getSelectedMatchId() {
        return selected_match_id;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBus.getDefault().register(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.mainToolbar);
        setSupportActionBar(toolbar);
        ViewCompat.setLayoutDirection(toolbar, ViewCompat.LAYOUT_DIRECTION_LTR);
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        setupViewPager(viewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        setUpTabLayout(tabLayout, viewPager);
        ViewCompat.setLayoutDirection(tabLayout, ViewCompat.LAYOUT_DIRECTION_LTR);

        //Extra check
        if(isNetworkConnected()) {
            SyncAdapter.initializeSyncAdapter(this);
            SyncAdapter.syncImmediately(this);
        } else {
            onEventMainThread(new MessageEvent("Can't connect! Check connection!"));
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    private void setUpTabLayout(final TabLayout tabLayout, final ViewPager viewPager) {
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(final TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());

            }

            @Override
            public void onTabUnselected(final TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(final TabLayout.Tab tab) {

            }
        });
        tabLayout.setupWithViewPager(viewPager);
        viewPager.setCurrentItem(2);
    }

    public void onEventMainThread(MessageEvent messageEvent) {
        Toast.makeText(this, messageEvent.getMessageString(), Toast.LENGTH_SHORT).show();
    }

    private void setupViewPager(final ViewPager viewPager) {
        ScoresPageAdapter pageAdapter = ScoresPageAdapter.getInstance(this, getSupportFragmentManager());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        if (Utilities.isRTL())
            for (int i = NUM_PAGES-1; i >= 0; i--) {
                Date fragmentDate = new Date(System.currentTimeMillis() + ((i-2) * 86400000));
                ScoresFragment viewFragment = new ScoresFragment();
                viewFragment.setFragmentDate(format.format(fragmentDate));
                pageAdapter.addFrag(viewFragment);
            }
        else
            for (int i = 0; i < NUM_PAGES; i++) {
                Date fragmentDate = new Date(System.currentTimeMillis() + ((i - 2) * 86400000));
                ScoresFragment viewFragment = new ScoresFragment();

                viewFragment.setFragmentDate(format.format(fragmentDate));
                pageAdapter.addFrag(viewFragment);
            }
        viewPager.setAdapter(pageAdapter);
        viewPager.setCurrentItem(2);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            new MaterialDialog.Builder(this)
                    .title(R.string.about)
                    .content(R.string.about_text)
                    .negativeText(R.string.close)
                    .show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
