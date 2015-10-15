package it.jaschke.alexandria.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.jaschke.alexandria.BaseActivity;
import it.jaschke.alexandria.FragmentNavigation;
import it.jaschke.alexandria.MainActivity;
import it.jaschke.alexandria.R;
import it.jaschke.alexandria.SettingsActivity;
import it.jaschke.alexandria.api.BookListAdapter;
import it.jaschke.alexandria.api.Callback;
import it.jaschke.alexandria.data.AlexandriaContract;


public class ListOfBooks extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, Callback {

    private BookListAdapter bookListAdapter;
    private int position = ListView.INVALID_POSITION;
    private final int LOADER_ID = 10;
    Cursor cursor;
    private String mSearchQuery = "";

    @Bind(R.id.listOfBooks)
    ListView listOfBooks;
    @Bind(R.id.empty)
    TextView empty;
    @Bind(R.id.fab)
    FloatingActionMenu fab;
    @Bind(R.id.editTextSearch)
    EditText mEditTextSearch;
    @Bind(R.id.addBookFab)
    FloatingActionButton addBookFab;
    @Bind(R.id.scanBookFab)
    FloatingActionButton scanBookFab;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_list_of_books, container, false);
        ButterKnife.bind(this, rootView);

        addBookFab.setOnClickListener(v -> {
            FragmentNavigation.launchAddBooksFragment((BaseActivity) getActivity(),
                    ((MainActivity) getActivity()).getBinding());
        });

        scanBookFab.setOnClickListener(v -> {
            //launch the scanner
            FragmentNavigation.launchAddBooksFragmentForScan((BaseActivity) getActivity(),
                    ((MainActivity) getActivity()).getBinding());
        });

        //Hide the Up button
        ActionBar supportActionBar = ((BaseActivity) getActivity()).getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar
                    .setDisplayHomeAsUpEnabled(false);
            supportActionBar.setTitle(getString(R.string.app_name));
        }

        /*
         * Initializes the CursorLoader. The URL_LOADER value is eventually passed
         * to onCreateLoader().
         */
        getLoaderManager().initLoader(0, null, this);

        mEditTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                mSearchQuery = s.toString();
                ListOfBooks.this.restartLoader();
            }

            @Override
            public void afterTextChanged(final Editable s) {

            }
        });

        bookListAdapter = new BookListAdapter(getActivity(), cursor, 0);

        listOfBooks.setAdapter(bookListAdapter);
        listOfBooks.setEmptyView(empty);
        listOfBooks.setOnItemClickListener((parent, view, position, id) -> {
            //Bug fix, cursor was querying after data-change or rotation
            cursor = getActivity().getContentResolver()
                    .query(
                            AlexandriaContract.BookEntry.CONTENT_URI,
                            null, // leaving "columns" null just returns all the columns.
                            null, // cols for "where" clause
                            null, // values for "where" clause
                            null  // sort order
                          );
            if (cursor != null && cursor.moveToPosition(position)) {
                onItemSelected(cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry._ID)));
            }
        });


        setHasOptionsMenu(true);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                //launch the settings activity from here
                startActivity(new Intent(getActivity(), SettingsActivity.class));

                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void restartLoader() {
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        final String selection = AlexandriaContract.BookEntry.TITLE + " LIKE ? OR " +
                AlexandriaContract.BookEntry.SUBTITLE + " LIKE ? ";
        String searchString = mSearchQuery;

        if (searchString.length() > 0) {
            searchString = "%" + searchString + "%";
            return new CursorLoader(
                    getActivity(),
                    AlexandriaContract.BookEntry.CONTENT_URI,
                    null,
                    selection,
                    new String[]{searchString, searchString},
                    null
            );
        }

        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        bookListAdapter.swapCursor(data);
        if (position != ListView.INVALID_POSITION) {
            listOfBooks.smoothScrollToPosition(position);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        bookListAdapter.swapCursor(null);
    }

    @Override
    public void onItemSelected(String ean) {
        Bundle args = new Bundle();
        args.putString(BookDetail.EAN_KEY, ean);

        BookDetail fragment = new BookDetail();
        fragment.setArguments(args);

        FragmentNavigation.launchBookDetailFragment(((BaseActivity) getActivity()),
                ((MainActivity) getActivity()).getBinding(), fragment);

    }


}
