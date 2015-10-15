package barqsoft.footballscores.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.appwidget.AppWidgetManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ComponentName;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import barqsoft.footballscores.R;
import barqsoft.footballscores.activities.MessageEvent;
import barqsoft.footballscores.data.DatabaseContract;
import barqsoft.footballscores.models.Fixture;
import barqsoft.footballscores.models.FootballAPI;
import barqsoft.footballscores.widget.WidgetProvider;
import de.greenrobot.event.EventBus;

/**
 * Created by ahmedrizwan on 14/10/2015.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String LOG_TAG = "SyncAdapter";
    Gson mGson = new Gson();
    OkHttpClient mOkHttpClient = new OkHttpClient();
    final static int SYNC_INTERVAL = 60 * 60 * 12; // 12 hours interval for sync
    final static int SYNC_FLEXTIME = SYNC_INTERVAL / 3;
    final String MATCH_LINK = "http://api.football-data.org/alpha/fixtures/";
    final String SEASON_LINK = "http://api.football-data.org/alpha/soccerseasons/";

    public SyncAdapter(final Context context, final boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(final Account account, final Bundle extras, final String authority, final ContentProviderClient provider, final SyncResult syncResult) {
        //delete the dummy data
        final FootballAPI dummyFootballAPI = mGson.fromJson(getContext().getString(R.string.dummy_data), FootballAPI.class);
        deleteDummyData(dummyFootballAPI);
        //Extra check
        getData("n2");
        getData("p2");
        //insert dummy data as we didn't get the data from server
        //first check if there is data already inserted
        //query
        try {
            final Cursor query = getContext().getContentResolver()
                    .query(DatabaseContract.BASE_CONTENT_URI, null, null, null, null);
            if (query.getCount() == 0) {
                EventBus.getDefault()
                        .post(new MessageEvent("Loading Dummy Data!"));
                processData(dummyFootballAPI, false);
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, e.toString());
        }

        Intent intent = new Intent(getContext(), WidgetProvider.class);
        intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
        int ids[] = AppWidgetManager.getInstance(getContext())
                .getAppWidgetIds(new ComponentName(getContext(), WidgetProvider.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        getContext().sendBroadcast(intent);
    }

    /***
     * loads the data from the API and processes it
     *
     * @param timeFrame
     */
    private void getData(final String timeFrame) {
        //Creating fetch URL
        String QUERY_TIME_FRAME = "timeFrame"; //Time Frame parameter to determine days
        try {
            final HttpUrl url = new HttpUrl.Builder()
                    .scheme("http")
                    .host("api.football-data.org")
                    .addPathSegment("alpha")
                    .addPathSegment("fixtures")
                    .addQueryParameter(QUERY_TIME_FRAME, timeFrame)
                    .build();
            String JSON_data = get(url);

            FootballAPI footballAPI = mGson.fromJson(JSON_data, FootballAPI.class);

            processData(footballAPI, true);

        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
            EventBus.getDefault()
                    .post(new MessageEvent("Couldn't get the the scores! Please check connection!"));
        }
    }

    /***
     * Deletes the dummy data from db using content resolver
     *
     * @param footballAPI
     */
    private void deleteDummyData(final FootballAPI footballAPI) {
        for (int i = 0; i < footballAPI.getCount(); i++) {
            getContext().getContentResolver()
                    .delete(DatabaseContract.BASE_CONTENT_URI, DatabaseContract.scores_table.MATCH_ID + "=" +
                            footballAPI.getFixtures()
                                    .get(i)
                                    .getLinks()
                                    .getSelf()
                                    .getHref()
                                    .replace(MATCH_LINK, ""), null);
        }
    }

    private void processData(final FootballAPI footballAPI, final boolean isReal) {
        final List<Fixture> fixtures = footballAPI.getFixtures();

        ContentValues[] values = new ContentValues[fixtures.size()];

        int i = 0;
        for (Fixture fixture : fixtures) {
            final String League = fixture.getLinks()
                    .getSoccerseason()
                    .getHref()
                    .replace(SEASON_LINK, "");
            final ContentValues match_values = new ContentValues();
            match_values.put(DatabaseContract.scores_table.MATCH_ID,
                    fixture.getLinks()
                            .getSelf()
                            .getHref()
                            .replace(MATCH_LINK, ""));
            String mDate = fixture.getDate();
            String mTime = mDate.substring(mDate.indexOf("T") + 1, mDate.indexOf("Z"));
            mDate = mDate.substring(0, mDate.indexOf("T"));
            SimpleDateFormat match_date = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");
            match_date.setTimeZone(TimeZone.getTimeZone("UTC"));
            try {
                final Date parseddate = match_date.parse(mDate + mTime);
                SimpleDateFormat new_date = new SimpleDateFormat("yyyy-MM-dd:HH:mm");
                new_date.setTimeZone(TimeZone.getDefault());
                mDate = new_date.format(parseddate);
                mTime = mDate.substring(mDate.indexOf(":") + 1);
                mDate = mDate.substring(0, mDate.indexOf(":"));

                if (!isReal) {
                    //This if statement changes the dummy data's date to match our current date range.
                    Date fragmentdate = new Date(System.currentTimeMillis() + ((fixtures.indexOf(fixture) - 2) * 86400000));
                    SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");
                    mDate = mformat.format(fragmentdate);

                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "error here!");
                Log.e(LOG_TAG, e.getMessage());
            }

            match_values.put(DatabaseContract.scores_table.DATE_COL, mDate);
            match_values.put(DatabaseContract.scores_table.TIME_COL, mTime);
            final String homeTeamName = fixture.getHomeTeamName();
            match_values.put(DatabaseContract.scores_table.HOME_COL, homeTeamName);
            final String awayTeamName = fixture.getAwayTeamName();
            match_values.put(DatabaseContract.scores_table.AWAY_COL, awayTeamName);

            match_values.put(DatabaseContract.scores_table.HOME_GOALS_COL, fixture.getResult()
                    .getGoalsHomeTeam());
            match_values.put(DatabaseContract.scores_table.AWAY_GOALS_COL, fixture.getResult()
                    .getGoalsAwayTeam());
            match_values.put(DatabaseContract.scores_table.LEAGUE_COL, League);
            match_values.put(DatabaseContract.scores_table.MATCH_DAY, fixture.getMatchday());

            values[i++] = match_values;

        }

        getContext().getContentResolver()
                .bulkInsert(DatabaseContract.BASE_CONTENT_URI, values);

    }

    private String get(final HttpUrl url) throws IOException {
        Request request = new Request.Builder().header("X-Auth-Token", getContext().getString(R.string.api_key))
                .url(url)
                .build();

        Response response = mOkHttpClient.newCall(request)
                .execute();
        return response.body()
                .string();
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     *                *
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if (null == accountManager.getPassword(newAccount)) {

                /*
             * Add the account and account type, no password or user data
             * If successful, return the Account object, otherwise report an error.
             */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
                /*
                 * If you don't set android:syncable="true" in
                 * in your <provider> element in the manifest,
                 * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
                 * here.
                 */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
            /*
             * Since we've created an account
             */
        configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

            /*
             * Without calling setSyncAutomatically, our periodic sync will not be enabled.
             */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

            /*
             * Finally, let's do a sync to get things started
             */
        syncImmediately(context);
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            final SyncRequest request = new SyncRequest.Builder()
                    .syncPeriodic(syncInterval, flexTime)
                    .setSyncAdapter(account, authority)
                    .setExtras(new Bundle())
                    .build();
            if (request != null)
                ContentResolver.requestSync(request);
        } else {

            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     *
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    public static void initializeSyncAdapter(final Context context) {
        getSyncAccount(context);
    }
}
