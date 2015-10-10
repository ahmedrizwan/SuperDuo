import android.support.v4.view.ViewPager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import barqsoft.footballscores.BuildConfig;
import barqsoft.footballscores.R;
import barqsoft.footballscores.activities.MainActivity;

import static junit.framework.Assert.assertEquals;

/**
 * Created by ahmedrizwan on 10/10/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21, manifest = "src/main/AndroidManifest.xml")
public class MainActivityTests {

    @Test
    public void viewPager_CurrentPage() {
        MainActivity mainActivity = Robolectric.setupActivity(MainActivity.class);
        ViewPager viewPager = (ViewPager) mainActivity.findViewById(R.id.viewPager);
        assertEquals(viewPager.getCurrentItem(), 2);
        assertEquals(viewPager.getAdapter()
                .getPageTitle(2)
                .toString(), "Today");

    }


}
