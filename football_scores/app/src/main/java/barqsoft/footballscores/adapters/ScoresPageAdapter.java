package barqsoft.footballscores.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.text.format.Time;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilities;

public class ScoresPageAdapter extends FragmentStatePagerAdapter {

    private final List<Fragment> mFragmentList = new ArrayList<>();
    private Context mContext;

    private ScoresPageAdapter(final FragmentManager fm) {
        super(fm);
    }

    public static ScoresPageAdapter getInstance(Context context, FragmentManager fragmentManager) {
        ScoresPageAdapter scores = new ScoresPageAdapter(fragmentManager);
        scores.setContext(context);
        return scores;
    }

    @Override
    public Fragment getItem(int i) {
        return mFragmentList.get(i);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }


    // Returns the page title for the top indicator
    @Override
    public CharSequence getPageTitle(int position) {
        if(Utilities.isRTL())
            return getDayName(mContext, System.currentTimeMillis() + ((2-position) * 86400000));
        return getDayName(mContext, System.currentTimeMillis() + ((position - 2) * 86400000));
    }

    public String getDayName(Context context, long dateInMillis) {
        // If the date is today, return the localized version of "Today" instead of the actual
        // day name.
        Time t = new Time();
        t.setToNow();
        int julianDay = Time.getJulianDay(dateInMillis, t.gmtoff);
        int currentJulianDay = Time.getJulianDay(System.currentTimeMillis(), t.gmtoff);
        if (julianDay == currentJulianDay) {
            return context.getString(R.string.today);
        } else if (julianDay == currentJulianDay + 1) {
            return context.getString(R.string.tomorrow);
        } else if (julianDay == currentJulianDay - 1) {
            return context.getString(R.string.yesterday);
        } else {
            Time time = new Time();
            time.setToNow();
            // Otherwise, the format is just the day of the week (e.g "Wednesday".
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
            return dayFormat.format(dateInMillis);
        }
    }

    public void addFrag(Fragment fragment) {
        mFragmentList.add(fragment);
    }

    public void setContext(final Context context) {
        mContext = context;
    }
}