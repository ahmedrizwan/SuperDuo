package barqsoft.footballscores.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;

import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilities;
import barqsoft.footballscores.activities.MainActivity;
import barqsoft.footballscores.adapters.ScoresListAdapter;
import barqsoft.footballscores.adapters.ViewHolder;
import barqsoft.footballscores.data.DatabaseContract;
import barqsoft.footballscores.sync.SyncService;

/**
 * A placeholder fragment containing a simple view.
 */
public class ScoresFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, ScoresListAdapter.ClickCallback {
    public ScoresListAdapter mAdapter;
    public static final int SCORES_LOADER = 0;
    private String[] fragmentdate = new String[1];
    private int last_selected_item = -1;
    private ListView score_list;

    public ScoresFragment() {
        setArguments(new Bundle());
    }

    private void update_scores() {
        Intent service_start = new Intent(getActivity(), SyncService.class);
        getActivity().startService(service_start);
    }

    public void setFragmentDate(String date) {
        fragmentdate[0] = date;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        update_scores();
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.widget_layout, container, false);
        ViewCompat.setLayoutDirection(rootView, ViewCompat.LAYOUT_DIRECTION_LTR);
        score_list = (ListView) rootView.findViewById(R.id.scores_list);
        score_list.setEmptyView(rootView.findViewById(R.id.emptyView));
        mAdapter = new ScoresListAdapter(getActivity(), null, 0);
        mAdapter.setDetailMatchId(MainActivity.getSelectedMatchId());
        mAdapter.setmClickCallback(this);
        score_list.setAdapter(mAdapter);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        //Extra check
        final Bundle arguments = getArguments();
        if (arguments != null) {
            final String[] dates = arguments.getStringArray(getString(R.string.key_fragment_date));
            if (dates != null)
                fragmentdate = dates;
        }
        getLoaderManager().initLoader(SCORES_LOADER, null, this);
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        getArguments().putStringArray(getString(R.string.key_fragment_date), fragmentdate);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(getActivity(), DatabaseContract.scores_table.buildScoreWithDate(),
                null, null, fragmentdate, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mAdapter.swapCursor(null);
    }


    @Override
    public void itemClicked(final Cursor cursor, final ViewHolder viewHolder) {
        new MaterialDialog.Builder(getActivity())
                .title(R.string.title_share)
                .content(Utilities.getMatchDay(cursor.getInt(ScoresListAdapter.COL_MATCHDAY),
                        cursor.getInt(ScoresListAdapter.COL_LEAGUE)) + "\n" +
                        Utilities.getLeague(cursor.getInt(ScoresListAdapter.COL_LEAGUE)))
                .positiveText(R.string.share_text)
                .negativeText(R.string.close)
                .onPositive((materialDialog, dialogAction) -> {
                    startActivity(createShareForecastIntent(viewHolder.home_name.getText()
                            .toString() + " " +
                            viewHolder.score.getText() + " " + viewHolder.away_name.getText() + " "));
                })
                .show();
    }

    public Intent createShareForecastIntent(String shareText) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText + ScoresListAdapter.FOOTBALL_SCORES_HASHTAG);
        return shareIntent;
    }
}
