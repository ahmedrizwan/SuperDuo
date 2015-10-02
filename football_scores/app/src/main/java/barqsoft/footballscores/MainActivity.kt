package barqsoft.footballscores

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private val save_tag = "Save Test"
    private var my_main: PagerFragment? = null
    private var mPagerAdapter: ScoresPageAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_actual)
        val toolbar = findViewById(R.id.mainToolbar) as Toolbar
        setSupportActionBar(toolbar)

        val viewPager = findViewById(R.id.viewPager) as ViewPager
        setupViewPager(viewPager)

        val tabLayout = findViewById(R.id.tabLayout) as TabLayout
        setUpTabLayout(tabLayout, viewPager)
        viewPager.currentItem = 2;
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
    }

    private fun setupViewPager(viewPager: ViewPager) {
        mPagerAdapter = ScoresPageAdapter.getInstance(this, supportFragmentManager)
        val mformat = SimpleDateFormat("yyyy-MM-dd")
        for (i in 0..NUM_PAGES - 1) {
            val fragmentdate = Date(System.currentTimeMillis() + ((i - 2) * 86400000))
            val viewFragments = MainScreenFragment()
            viewFragments.setFragmentDate(mformat.format(fragmentdate))
            mPagerAdapter!!.addFrag(viewFragments)
        }

        viewPager.adapter = mPagerAdapter
        viewPager.currentItem = MainActivity.current_fragment
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
//        Log.v(save_tag, "will save")
//        Log.v(save_tag, "fragment: " + (my_main!!.mPagerHandler.currentItem))
//        Log.v(save_tag, "selected id: " + selected_match_id)
//        outState.putInt("Pager_Current", my_main!!.mPagerHandler.currentItem)
//        outState.putInt("Selected_match", selected_match_id)
        supportFragmentManager.putFragment(outState, "my_main", my_main)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        Log.v(save_tag, "will retrive")
        Log.v(save_tag, "fragment: " + (savedInstanceState.getInt("Pager_Current")))
        Log.v(save_tag, "selected id: " + savedInstanceState.getInt("Selected_match"))
        current_fragment = savedInstanceState.getInt("Pager_Current")
        selected_match_id = savedInstanceState.getInt("Selected_match")
        my_main = supportFragmentManager.getFragment(savedInstanceState, "my_main") as PagerFragment
        super.onRestoreInstanceState(savedInstanceState)
    }

    companion object {
        private val NUM_PAGES = 5
        var selected_match_id: Int = 0
        var current_fragment = 2
    }
}
