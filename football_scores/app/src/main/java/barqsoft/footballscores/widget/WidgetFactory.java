package barqsoft.footballscores.widget;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import barqsoft.footballscores.R;
import barqsoft.footballscores.activities.MainActivity;
import barqsoft.footballscores.adapters.ScoresListAdapter;
import barqsoft.footballscores.data.DatabaseContract;

public class WidgetFactory implements RemoteViewsService.RemoteViewsFactory {

    private final Context mContext;
    private final Intent mIntent;

    private ContentResolver mContentResolver;
    private Cursor mCursor;


    public WidgetFactory(Context context, Intent intent) {
        mContext = context;
        mIntent = intent;
        mContentResolver = context.getContentResolver();
    }

    @Override
    public void onCreate() {
        mCursor = mContentResolver.query(DatabaseContract.BASE_CONTENT_URI,
                null, null, null, null);
    }

    @Override
    public void onDataSetChanged() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return mCursor != null ? mCursor.getCount() : 0;
    }

    @Override
    public RemoteViews getViewAt(final int position) {
        mCursor.moveToPosition(position);
        // Create a new Remote Views object using the appropriate // item layout
        RemoteViews rv = new RemoteViews(mContext.getPackageName(),
                R.layout.item_widget);

        final String homeGoals = mCursor.getString(6);
        final String homeName = mCursor.getString(3);

        rv.setTextViewText(R.id.textViewHome,   homeName);
        rv.setTextViewText(R.id.homeScore, (homeGoals.equals("-1") ? "-" : homeGoals));
        final String awayName = mCursor.getString(4);
        final String awayGoals = mCursor.getString(7);
        rv.setTextViewText(R.id.textViewAway,  awayName);
        rv.setTextViewText(R.id.awayScore, (awayGoals.equals("-1") ? "-" : awayGoals));
        final String homePng = ScoresListAdapter.getNameForDrawable(homeName);
        final String awayPng = ScoresListAdapter.getNameForDrawable(awayName);
        final int homeDrawableId = ScoresListAdapter.getDrawableByName(mContext, homePng);
        final int awayDrawableId = ScoresListAdapter.getDrawableByName(mContext, awayPng);
        rv.setImageViewResource(R.id.imageViewHome, homeDrawableId == 0 ? R.drawable.no_icon : homeDrawableId);
        rv.setImageViewResource(R.id.imageViewAway, awayDrawableId == 0 ? R.drawable.no_icon : awayDrawableId);

        Intent launchActivity = new Intent(mContext, MainActivity.class);
        rv.setOnClickFillInIntent(R.id.list_item, launchActivity);

        return rv;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(final int position) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
}