package barqsoft.footballscores.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.NumberFormat;
import java.util.Locale;

import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilities;

/**
 * Created by ahmedrizwan on 14/10/2015.
 */
public class ScoresListAdapter extends CursorAdapter {
    public static final int COL_HOME = 3;
    public static final int COL_AWAY = 4;
    public static final int COL_HOME_GOALS = 6;
    public static final int COL_AWAY_GOALS = 7;
    public static final int COL_DATE = 1;
    public static final int COL_LEAGUE = 5;
    public static final int COL_MATCHDAY = 9;
    public static final int COL_ID = 8;
    public static final int COL_MATCHTIME = 2;

    public static final String FOOTBALL_SCORES_HASHTAG = "#Football_Scores";
    private int selectedMatchId;

    public static int getDrawableByName(Context context, String name) {
        return context.getResources()
                .getIdentifier(name, "drawable",
                        context.getPackageName());
    }

    public static String getNameForDrawable(String teamName) {
        return teamName.trim()
                .toLowerCase()
                .replace(" ", "_")
                .replace('é', 'e')
                .replace('ó', 'o')
                .replace('ö', 'o')
                .replace('ü', 'u')
                .replace('ß', 'b')
                .replace('-', '_')
                .replace("bor.", "bor")
                .replace('ç', 'c')
                .replace("_st.", "_st")
                .replace('á', 'a')
                .replace('î', 'i')
                .replace("f.c..", "fc.")
                .replace('ú', 'u')
                .replace("1._", "");
    }

    int detail_match_id = 0;

    private SharedPreferences mSharedPreferences;
    ClickCallback mClickCallback = null;
    NumberFormat mNumberFormat = NumberFormat.getInstance(Locale.getDefault());


    public ScoresListAdapter(final Context context, final Cursor c, final int flags) {
        super(context, c, flags);
        mSharedPreferences = Utilities.getSharedPreferences(context);
    }

    @Override
    public View newView(final Context context, final Cursor cursor, final ViewGroup parent) {
        View mItem = LayoutInflater.from(context).inflate(R.layout.item_score, parent, false);
        ViewCompat.setLayoutDirection(mItem,Utilities.isRTL()?ViewCompat.LAYOUT_DIRECTION_RTL:ViewCompat.LAYOUT_DIRECTION_LTR);
        ViewHolder mHolder = new ViewHolder(mItem);
        mItem.setTag(mHolder);
        //Log.v(FetchScoreTask.LOG_TAG,"new View inflated");
        return mItem;
    }

    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {
        ViewHolder mHolder = (ViewHolder) view.getTag();
        String homeTeam = cursor.getString(COL_HOME);
        String awayTeam = cursor.getString(COL_AWAY);
        mHolder.home_name.setText(homeTeam);
        mHolder.away_name.setText(awayTeam);
        mHolder.date.setText(cursor.getString(COL_MATCHTIME));
        mHolder.score.setText(Utilities.getScores(mNumberFormat,cursor.getInt(COL_HOME_GOALS), cursor.getInt(COL_AWAY_GOALS)));
        mHolder.match_id = cursor.getDouble(COL_ID);
        try {
            final String awayPng = getNameForDrawable(awayTeam);
            mHolder.away_crest.setImageDrawable(ContextCompat.getDrawable(context, getDrawableByName(context,
                    awayPng)));

        } catch (Exception e) {
            mHolder.away_crest.setImageResource(R.drawable.no_icon);
        }

        try {
            final String homePng = getNameForDrawable(homeTeam);
            mHolder.home_crest.setImageDrawable(ContextCompat.getDrawable(context, getDrawableByName(context,
                    homePng)));

        } catch (Exception e) {
            mHolder.home_crest.setImageResource(R.drawable.no_icon);
        }

        view.setOnClickListener(v1 -> mClickCallback.itemClicked(cursor, mHolder));
        android.support.v4.view.ViewCompat.setHasTransientState(view, true);
    }

    public void setmClickCallback(final ClickCallback clickCallback) {
        mClickCallback = clickCallback;
    }

    public void setDetailMatchId(final int detailMatchId) {
        selectedMatchId = detailMatchId;
    }

    public interface ClickCallback {
        public void itemClicked( Cursor cursor, ViewHolder viewHolder);
    }
}
