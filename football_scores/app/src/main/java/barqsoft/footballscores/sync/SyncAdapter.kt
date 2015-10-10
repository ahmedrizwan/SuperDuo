package barqsoft.footballscores.sync

import android.accounts.Account
import android.accounts.AccountManager
import android.content.*
import android.os.Build
import android.os.Bundle
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
 * Created by ahmedrizwan on 10/10/2015.
 */
class SyncAdapter(context: Context, autoInitialize: Boolean) : AbstractThreadedSyncAdapter(context, autoInitialize) {
    val LOG_TAG = SyncAdapter::class.java.simpleName
    internal val MATCH_LINK = "http://api.football-data.org/alpha/fixtures/"
    internal val SEASON_LINK = "http://api.football-data.org/alpha/soccerseasons/"

    val mOkHttpClient = OkHttpClient()
    val gson = Gson()

    override fun onPerformSync(account: Account, extras: Bundle,
                               authority: String, provider: ContentProviderClient,
                               syncResult: SyncResult) {
        Log.e(LOG_TAG, "Starting sync")
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

        context.contentResolver.bulkInsert(DatabaseContract.BASE_CONTENT_URI, values)
    }

    @Throws(IOException::class)
    private fun get(url: HttpUrl): String {
        val request = Request.Builder().header("X-Auth-Token", context.getString(R.string.api_key)).url(url).build()

        val response = mOkHttpClient.newCall(request).execute()
        return response.body().string()
    }



    companion object {
        // Interval at which to sync with the weather, in seconds.
        // 60 seconds (1 minute) * 180 = 3 hours
        val SYNC_INTERVAL = 60
        val SYNC_FLEXTIME = SYNC_INTERVAL/3

        fun initializeSyncAdapter(context: Context) {
            getSyncAccount(context)
        }

        /**
         * Helper method to get the fake account to be used with SyncAdapter, or make a new one
         * if the fake account doesn't exist yet.  If we make a new account, we call the
         * onAccountCreated method so we can initialize things.

         * @param context The context used to access the account service
         * *
         * @return a fake account.
         */
        fun getSyncAccount(context: Context): Account? {
            // Get an instance of the Android account manager
            val accountManager = context.getSystemService(Context.ACCOUNT_SERVICE) as AccountManager

            // Create the account type and default account
            val newAccount = Account(
                    context.getString(R.string.app_name), context.getString(R.string.sync_account_type))

            // If the password doesn't exist, the account doesn't exist
            if (null == accountManager.getPassword(newAccount)) {

                /*
             * Add the account and account type, no password or user data
             * If successful, return the Account object, otherwise report an error.
             */
                if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                    return null
                }
                /*
                 * If you don't set android:syncable="true" in
                 * in your <provider> element in the manifest,
                 * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
                 * here.
                 */

                onAccountCreated(newAccount, context)
            }
            return newAccount
        }

        private fun onAccountCreated(newAccount: Account, context: Context) {
            /*
             * Since we've created an account
             */
            configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME)

            /*
             * Without calling setSyncAutomatically, our periodic sync will not be enabled.
             */
            ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true)

            /*
             * Finally, let's do a sync to get things started
             */
            syncImmediately(context)
        }

        /**
         * Helper method to schedule the sync adapter periodic execution
         */
        fun configurePeriodicSync(context: Context, syncInterval: Int, flexTime: Int) {
            val account = getSyncAccount(context)
            val authority = context.getString(R.string.content_authority)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                // we can enable inexact timers in our periodic sync
                val request = SyncRequest.Builder().syncPeriodic(syncInterval.toLong(), flexTime.toLong()).setSyncAdapter(account, authority).setExtras(Bundle()).build()
                ContentResolver.requestSync(request)
            } else {
                ContentResolver.addPeriodicSync(account,
                        authority, Bundle(), syncInterval.toLong())
            }
        }

        /**
         * Helper method to have the sync adapter sync immediately
         * @param context The context used to access the account service
         */
        fun syncImmediately(context: Context) {
            val bundle = Bundle()
            bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true)
            bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true)
            ContentResolver.requestSync(getSyncAccount(context),
                    context.getString(R.string.content_authority), bundle)
        }
    }
}
