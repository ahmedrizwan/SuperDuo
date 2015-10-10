package barqsoft.footballscores.activities

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.format.Time
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import barqsoft.footballscores.AboutActivity
import barqsoft.footballscores.R
import barqsoft.footballscores.adapters.ScoresPageAdapter
import barqsoft.footballscores.fragments.ScoresFragment
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_actual)
        val toolbar = findViewById(R.id.mainToolbar) as Toolbar
        setSupportActionBar(toolbar)

        val viewPager = findViewById(R.id.viewPager) as ViewPager
        setupViewPager(viewPager)

        val tabLayout = findViewById(R.id.tabLayout) as TabLayout
        setUpTabLayout(tabLayout, viewPager)

        setUpAlarm()

    }

    private fun setUpAlarm() {
        val calendar = Calendar.getInstance();

        val now = Time();
        now.setToNow();
        calendar.set(Calendar.HOUR_OF_DAY, now.hour); // For 1 PM or 2 PM
        calendar.set(Calendar.MINUTE, 15);
        calendar.set(Calendar.SECOND, 0);
        Log.e("Time", calendar.time.toString() +" and Current time is "+ now.hour+":"+now.minute)
        val intent: Intent = Intent(FOOTBALL_ACTION)
        val pi = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        val am: AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager;
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pi)
    }

    private fun setUpTabLayout(tabLayout: TabLayout, viewPager: ViewPager) {
        tabLayout.setOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
            }
        })

        tabLayout.setupWithViewPager(viewPager)
        viewPager.currentItem = 2;
    }

    private fun setupViewPager(viewPager: ViewPager) {
        val mPagerAdapter = ScoresPageAdapter.getInstance(this, supportFragmentManager)
        val mformat = SimpleDateFormat("yyyy-MM-dd")
        for (i in 0..NUM_PAGES - 1) {
            val fragmentdate = Date(System.currentTimeMillis() + ((i - 2) * 86400000))
            val viewFragment = ScoresFragment()
            viewFragment.setFragmentDate(mformat.format(fragmentdate))
            mPagerAdapter!!.addFrag(viewFragment)
        }

        viewPager.adapter = mPagerAdapter
        viewPager.currentItem = current_fragment
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            val start_about = Intent(this, AboutActivity::class.java)
            startActivity(start_about)
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
    }

    companion object {
        private val NUM_PAGES = 5
        var selected_match_id: Int = 0
        var current_fragment = 2
        val FOOTBALL_ACTION = "barqsoft.footballscores.update";
    }
}
