package barqsoft.footballscores.service

import android.app.IntentService
import android.content.ContentValues
import android.content.Intent
import android.util.Log
import barqsoft.footballscores.DatabaseContract
import barqsoft.footballscores.R
import barqsoft.footballscores.models.FootballAPI
import com.google.gson.Gson
import com.squareup.okhttp.HttpUrl
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by yehya khaled on 3/2/2015.
 */
class FetchService : IntentService("myFetchService") {

    internal val MATCH_LINK = "http://api.football-data.org/alpha/fixtures/"
    internal val SEASON_LINK = "http://api.football-data.org/alpha/soccerseasons/"

    val mOkHttpClient = OkHttpClient()
    val gson = Gson()

    override fun onHandleIntent(intent: Intent) {
        getData("n2")
        getData("p2")
    }

    private fun getData(timeFrame: String) {
        //Creating fetch URL
        val BASE_URL = "http://api.football-data.org/alpha/fixtures" //Base URL
        val QUERY_TIME_FRAME = "timeFrame" //Time Frame parameter to determine days
        try {
            val url = HttpUrl.Builder().scheme("http").host("api.football-data.org").addPathSegment("alpha").addPathSegment("fixtures").addQueryParameter(QUERY_TIME_FRAME, timeFrame).build()
            val JSON_data = get(url)
            Log.e("Main Response", JSON_data)
            //This bit is to check if the data contains any matches. If not, we call processJson on the dummy data
            //Here I can serialize the data
            val footballAPI = gson.fromJson(JSON_data, FootballAPI::class.java)
            processData(footballAPI, true)
        } catch (e: Exception) {
            Log.e(LOG_TAG, e.getMessage())
        }
    }

    private fun processData(footballAPI: FootballAPI, isReal: Boolean) {
        val fixtures = footballAPI.fixtures

        val values = arrayOfNulls<ContentValues>(fixtures.size())
        var i = 0
        for (fixture in fixtures) {
            val League = fixture.links.soccerseason.href.replace(SEASON_LINK, "")

            val match_values = ContentValues()
            match_values.put(DatabaseContract.scores_table.MATCH_ID,
                    fixture.links.self.href.replace(MATCH_LINK, ""))
            var mDate = fixture.date
            var mTime = mDate.substring(mDate.indexOf("T") + 1, mDate.indexOf("Z"))
            mDate = mDate.substring(0, mDate.indexOf("T"))
            val match_date = SimpleDateFormat("yyyy-MM-ddHH:mm:ss")
            match_date.timeZone = TimeZone.getTimeZone("UTC")
            try {
                val parseddate = match_date.parse(mDate + mTime)
                val new_date = SimpleDateFormat("yyyy-MM-dd:HH:mm")
                new_date.timeZone = TimeZone.getDefault()
                mDate = new_date.format(parseddate)
                mTime = mDate.substring(mDate.indexOf(":") + 1)
                mDate = mDate.substring(0, mDate.indexOf(":"))

                if (!isReal) {
                    //This if statement changes the dummy data's date to match our current date range.
                    val fragmentdate = Date(System.currentTimeMillis() + ((fixtures.indexOf(fixture) - 2) * 86400000))
                    val mformat = SimpleDateFormat("yyyy-MM-dd")
                    mDate = mformat.format(fragmentdate)
                }
            } catch (e: Exception) {
                Log.e(LOG_TAG, "error here!")
                Log.e(LOG_TAG, e.getMessage())
            }

            match_values.put(DatabaseContract.scores_table.DATE_COL, mDate)
            match_values.put(DatabaseContract.scores_table.TIME_COL, mTime)
            val homeTeamName = fixture.homeTeamName
            match_values.put(DatabaseContract.scores_table.HOME_COL, homeTeamName)
            val awayTeamName = fixture.awayTeamName
            match_values.put(DatabaseContract.scores_table.AWAY_COL, awayTeamName)
            match_values.put(DatabaseContract.scores_table.HOME_GOALS_COL, fixture.result.goalsHomeTeam.toString())
            match_values.put(DatabaseContract.scores_table.AWAY_GOALS_COL, fixture.result.goalsAwayTeam.toString())
            match_values.put(DatabaseContract.scores_table.LEAGUE_COL, League)
            match_values.put(DatabaseContract.scores_table.MATCH_DAY, fixture.matchday.toString())

            values[i++] = match_values
        }

        this@FetchService.contentResolver.bulkInsert(DatabaseContract.BASE_CONTENT_URI, values)
    }

    @Throws(IOException::class)
    private fun get(url: HttpUrl): String {
        val request = Request.Builder().header("X-Auth-Token", getString(R.string.api_key)).url(url).build()

        val response = mOkHttpClient.newCall(request).execute()
        return response.body().string()
    }

    companion object {
        val LOG_TAG = "myFetchService"
    }

}

