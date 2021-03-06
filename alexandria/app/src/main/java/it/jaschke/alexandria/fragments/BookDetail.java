package it.jaschke.alexandria.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.ShareActionProvider;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.jaschke.alexandria.BaseActivity;
import it.jaschke.alexandria.FragmentNavigation;
import it.jaschke.alexandria.MainActivity;
import it.jaschke.alexandria.R;
import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.services.BookService;


public class BookDetail extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String EAN_KEY = "EAN";
    private final int LOADER_ID = 10;
    private String ean;
    private ShareActionProvider shareActionProvider;

    @Bind(R.id.deleteButton)
     View deleteButton;
    @Bind(R.id.fullBookTitle)
     TextView fullBookTitle;
    @Bind(R.id.fullBookSubTitle)
     TextView fullBookSubTitle;
    @Bind(R.id.fullBookDesc)
     TextView fullBookDesc;
    @Bind(R.id.authors)
     TextView authors;
    @Bind(R.id.fullBookCover)
     ImageView fullBookCover;
    @Bind(R.id.categories)
     TextView categories;

    public BookDetail(){
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            ean = arguments.getString(BookDetail.EAN_KEY);
            getLoaderManager().restartLoader(LOADER_ID, null, this);
        }

        View rootView = inflater.inflate(R.layout.fragment_full_book, container, false);
        ButterKnife.bind(this,rootView);

        deleteButton.setOnClickListener(view -> {
            Intent bookIntent = new Intent(getActivity(), BookService.class);
            bookIntent.putExtra(BookService.EAN, ean);
            bookIntent.setAction(BookService.DELETE_BOOK);
            getActivity().startService(bookIntent);
            FragmentNavigation.removeDetailsFragment(((MainActivity) getActivity()).getBinding(),
                    (BaseActivity) getActivity(),this);
        });

        ActionBar supportActionBar = ((BaseActivity) getActivity()).getSupportActionBar();
        if (supportActionBar != null && !((MainActivity) getActivity()).tabletMode()) {
            supportActionBar
                    .setDisplayHomeAsUpEnabled(true);
            supportActionBar.setTitle(R.string.details);
        }

        return rootView;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.book_detail, menu);

        MenuItem menuItem = menu.findItem(R.id.action_share);
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(ean)),
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }

        String bookTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
        (fullBookTitle).setText(bookTitle);

        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text) + bookTitle);
            shareActionProvider.setShareIntent(shareIntent);
        }catch (NullPointerException e){

        }
        String bookSubTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
        ((fullBookSubTitle)).setText(bookSubTitle);

        String desc = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.DESC));
        ((fullBookDesc)).setText(desc);

        String authorsString = data.getString(data.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));
        if (authorsString != null) {
            String[] authorsArr = authorsString.split(",");
            ((authors)).setLines(authorsArr.length);
            ((authors)).setText(authorsString.replace(",","\n"));
        }

        String imgUrl = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
        if(Patterns.WEB_URL.matcher(imgUrl).matches()){
            if(!imgUrl.isEmpty()){
                Picasso.with(getActivity()).load(imgUrl).into(fullBookCover);
            } else
                fullBookCover.setImageResource(R.drawable.ic_not_available);
            fullBookCover.setVisibility(View.VISIBLE);
        } else {
            fullBookCover.setImageResource(R.drawable.ic_not_available);
        }

        String categoriesString = data.getString(data.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));
        ((categories)).setText(categoriesString);

    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {

    }

}