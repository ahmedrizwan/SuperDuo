package barqsoft.footballscores.adapters

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.support.v4.widget.CursorAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import barqsoft.footballscores.R
import barqsoft.footballscores.Utilities
import barqsoft.footballscores.ViewHolder

/**
 * Created by yehya khaled on 2/26/2015.
 */
class ScoresListAdapter(context: Context, cursor: Cursor, flags: Int) : CursorAdapter(context, cursor, flags) {
    var detail_match_id = 0.0
    private val FOOTBALL_SCORES_HASHTAG = "#Football_Scores"

    private val mSharedPreferences: SharedPreferences

    init {
        mContext = context
        mSharedPreferences = Utilities.getSharedPreferences(context)
    }

    override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View {
        val mItem = LayoutInflater.from(context).inflate(R.layout.score_item, parent, false)
        val mHolder = ViewHolder(mItem)
        mItem.tag = mHolder
        //Log.v(FetchScoreTask.LOG_TAG,"new View inflated");
        return mItem
    }

    override fun bindView(view: View, context: Context, cursor: Cursor) {
        val mHolder = view.tag as ViewHolder
        val homeTeam = cursor.getString(COL_HOME)
        val awayTeam = cursor.getString(COL_AWAY)
        mHolder.home_name.text = homeTeam
        mHolder.away_name.text = awayTeam
        mHolder.date.text = cursor.getString(COL_MATCHTIME)
        mHolder.score.text = Utilities.getScores(cursor.getInt(COL_HOME_GOALS), cursor.getInt(COL_AWAY_GOALS))
        mHolder.match_id = cursor.getDouble(COL_ID)
        try {
            val awayPng = awayTeam.trim().toLowerCase().replace(" ", "_").replace('é', 'e').replace('ó', 'o').replace('ö', 'o').replace('ü', 'u').replace('ß', 'b').replace('-', '_').replace("bor.", "bor").replace('ç', 'c').replace("_st.", "_st").replace('á', 'a').replace('î', 'i').replace("f.c..", "fc.").replace('ú', 'u').replace("1._", "")
            mHolder.away_crest.setImageDrawable(ContextCompat.getDrawable(context, getDrawableByName(context,
                    awayPng)))

        } catch (e: Exception) {
            mHolder.away_crest.setImageResource(R.drawable.no_icon)
        }

        try {
            val homePng = homeTeam.trim().toLowerCase().replace(" ", "_").replace('é', 'e').replace('ó', 'o').replace('ö', 'o').replace('ü', 'u').replace('ß', 'b').replace('-', '_').replace("bor.", "bor").replace('ç', 'c').replace("_st.", "_st").replace('á', 'a').replace('î', 'i').replace("f.c..", "fc.").replace('ú', 'u').replace("1._", "")
            mHolder.home_crest.setImageDrawable(ContextCompat.getDrawable(context, getDrawableByName(context,
                    homePng)))

        } catch (e: Exception) {
            mHolder.home_crest.setImageResource(R.drawable.no_icon)
        }

        val vi = context.applicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val v = vi.inflate(R.layout.detail_fragment, null)
        val container = view.findViewById(R.id.details_fragment_container) as ViewGroup
        if (mHolder.match_id == detail_match_id) {
            //Log.v(FetchScoreTask.LOG_TAG,"will insert extraView");
            container.addView(v, 0, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
            val match_day = v.findViewById(R.id.matchday_textview) as TextView
            match_day.text = Utilities.getMatchDay(cursor.getInt(COL_MATCHDAY),
                    cursor.getInt(COL_LEAGUE))
            val league = v.findViewById(R.id.league_textview) as TextView
            league.text = Utilities.getLeague(cursor.getInt(COL_LEAGUE))
            val share_button = v.findViewById(R.id.share_button) as Button
            share_button.setOnClickListener { v1 -> //add Share Action
//                context.startActivity(createShareForecastIntent(mHolder.home_name.text + " " +
//                        mHolder.score.text + " " + mHolder.away_name.text + " "))
            }
        } else {
            container.removeAllViews()
        }
        ViewCompat.setHasTransientState(view, true)
    }

    fun createShareForecastIntent(ShareText: String): Intent {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
        shareIntent.setType("text/plain")
        shareIntent.putExtra(Intent.EXTRA_TEXT, ShareText + FOOTBALL_SCORES_HASHTAG)
        return shareIntent
    }

    fun getDrawableByName(context: Context, name: String): Int {
        val resources = context.resources
        return resources.getIdentifier(name, "drawable",
                context.packageName)
    }

    companion object {
        val COL_HOME = 3
        val COL_AWAY = 4
        val COL_HOME_GOALS = 6
        val COL_AWAY_GOALS = 7
        val COL_DATE = 1
        val COL_LEAGUE = 5
        val COL_MATCHDAY = 9
        val COL_ID = 8
        val COL_MATCHTIME = 2
    }

}
