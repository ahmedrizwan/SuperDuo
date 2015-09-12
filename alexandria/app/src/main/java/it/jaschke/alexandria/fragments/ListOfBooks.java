package it.jaschke.alexandria.fragments;

import android.app.SearchManager;
import android.content.Context;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import it.jaschke.alexandria.BaseActivity;
import it.jaschke.alexandria.R;
import it.jaschke.alexandria.api.BookListAdapter;
import it.jaschke.alexandria.api.Callback;
import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.databinding.FragmentListOfBooksBinding;


public class ListOfBooks extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private BookListAdapter bookListAdapter;
    private int position = ListView.INVALID_POSITION;
    private final int LOADER_ID = 10;
    FragmentListOfBooksBinding mFragmentListOfBooksBinding;
    Cursor cursor;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cursor = getActivity().getContentResolver()
                .query(
                        AlexandriaContract.BookEntry.CONTENT_URI,
                        null, // leaving "columns" null just returns all the columns.
                        null, // cols for "where" clause
                        null, // values for "where" clause
                        null  // sort order
                      );

        bookListAdapter = new BookListAdapter(getActivity(), cursor, 0);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mFragmentListOfBooksBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_list_of_books, container, false);

        mFragmentListOfBooksBinding.listOfBooks.setAdapter(bookListAdapter);
        mFragmentListOfBooksBinding.listOfBooks.setEmptyView(mFragmentListOfBooksBinding.empty);
        //Item click listener for the listView
        mFragmentListOfBooksBinding.listOfBooks.setOnItemClickListener((adapterView, view, position1, l) -> {
            if (cursor != null && cursor.moveToPosition(position1)) {
                ((Callback) getActivity())
                        .onItemSelected(cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry._ID)));
            }
        });

        mFragmentListOfBooksBinding.fab.setOnClickListener(view -> {
            //launch the fragment for scan
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.includeContainer, new AddBook())
                    .addToBackStack(null)
                    .commit();
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
        //        searchText = (EditText) rootView.findViewById(R.id.searchText);
//        rootView.findViewById(R.id.searchButton)
//                .setOnClickListener(
//                        v -> ListOfBooks.this.restartLoader()
//                                   );
        return mFragmentListOfBooksBinding.getRoot();
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setIconifiedByDefault(false);
    }

    private void restartLoader() {
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        final String selection = AlexandriaContract.BookEntry.TITLE + " LIKE ? OR " + AlexandriaContract.BookEntry.SUBTITLE + " LIKE ? ";
//        String searchString = searchText.getText()
//                .toString();

//        if (searchString.length() > 0) {
//            searchString = "%" + searchString + "%";
//            return new CursorLoader(
//                    getActivity(),
//                    AlexandriaContract.BookEntry.CONTENT_URI,
//                    null,
//                    selection,
//                    new String[]{searchString, searchString},
//                    null
//            );
//        }

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
            mFragmentListOfBooksBinding.listOfBooks.smoothScrollToPosition(position);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        bookListAdapter.swapCursor(null);
    }

    @Override
    public void onResume() {
        super.onResume();

    }
}
