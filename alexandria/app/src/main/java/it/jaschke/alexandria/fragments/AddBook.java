package it.jaschke.alexandria.fragments;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import it.jaschke.alexandria.BaseActivity;
import it.jaschke.alexandria.R;
import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.services.BookService;
import it.jaschke.alexandria.services.DownloadImage;


public class AddBook extends DialogFragment {
    private static final String TAG = "INTENT_TO_SCAN_ACTIVITY";

    private final int LOADER_ID = 1;

    private final String EAN_CONTENT = "eanContent";

    private static final String SCAN_FORMAT = "scanFormat";
    private static final String SCAN_CONTENTS = "scanContents";

    private String mScanFormat = "Format:";
    private String mScanContents = "Contents:";

    BookService.BookEvent mBookEvent;
    BookService.AuthorEvent mAuthorEvent;
    BookService.CategoryEvent mCategoryEvent;

    @Bind(R.id.ean)
     TextView ean;
    @Bind(R.id.scan_button)
     View scanButton;
    @Bind(R.id.save_button)
     View saveButton;
    @Bind(R.id.delete_button)
     View deleteButton;
    @Bind(R.id.bookTitle)
     TextView bookTitle;
    @Bind(R.id.bookSubTitle)
     TextView bookSubTitle;
    @Bind(R.id.bookCover)
     ImageView bookCover;
    @Bind(R.id.authors)
     TextView authors;
    @Bind(R.id.categories)
     TextView categories;


    public static AddBook getInstance() {
        AddBook addBook = new AddBook();
        return addBook;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_add_book, container, false);
        ButterKnife.bind(this,rootView);
        EventBus.getDefault()
                .register(this);
        ActionBar supportActionBar = ((BaseActivity) getActivity()).getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar
                    .setDisplayHomeAsUpEnabled(true);
            supportActionBar.setTitle(R.string.add_book);
        }

        ean.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //no need
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //no need
            }

            @Override
            public void afterTextChanged(Editable s) {
                String ean = s.toString();
                //catch isbn10 numbers
                if (ean.length() == 10 && !ean.startsWith("978")) {
                    ean = "978" + ean;
                }
                if (ean.length() < 13) {
                    clearFields();
                    return;
                }
                //Once we have an ISBN, start a book intent
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, ean);
                bookIntent.setAction(BookService.FETCH_BOOK);
                getActivity().startService(bookIntent);
//                AddBook.this.restartLoader();
            }
        });

        scanButton
                .setOnClickListener(v -> {
                    if (checkCameraHardware(getActivity())) {
                        IntentIntegrator.forSupportFragment(this)
                                .initiateScan();
                    } else {
                        Toast.makeText(getActivity(), "No camera detected!", Toast.LENGTH_SHORT)
                                .show();
                    }
                });

        saveButton
                .setOnClickListener(view -> {
                    //save the book
                    saveBook();
                    //show snackBar that book is saved

                    //reset the fields
                    ean.setText("");
                });


        deleteButton
                .setOnClickListener(view -> {
                    Intent bookIntent = new Intent(getActivity(), BookService.class);
                    bookIntent.putExtra(BookService.EAN, ean.getText()
                            .toString());
                    bookIntent.setAction(BookService.DELETE_BOOK);
                    getActivity().startService(bookIntent);
                    ean.setText("");
                });

        if (savedInstanceState != null) {
            ean.setText(savedInstanceState.getString(EAN_CONTENT));
            ean.setHint("");
        }

        return rootView;
    }

    private void saveBook() {
        ContentResolver contentResolver = getActivity().getContentResolver();
        contentResolver.insert(AlexandriaContract.BookEntry.CONTENT_URI, mBookEvent.getBookValues());
        for (ContentValues contentValues : mAuthorEvent.getAuthorValues()) {
            contentResolver.insert(AlexandriaContract.AuthorEntry.CONTENT_URI, contentValues);
        }
        for (ContentValues contentValues : mCategoryEvent.getCategoryValues()) {
            contentResolver.insert(AlexandriaContract.CategoryEntry.CONTENT_URI, contentValues);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (ean != null) {
            outState.putString(EAN_CONTENT, ean.getText()
                    .toString());
        }
    }

    /**
     * Check if this device has a camera
     */
    private boolean checkCameraHardware(Context context) {
        return context.getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

//    private void restartLoader() {
//        getLoaderManager().restartLoader(LOADER_ID, null, this);
//    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Log.d("MainActivity", "Cancelled scan");
                Toast.makeText(getActivity(), "Cancelled", Toast.LENGTH_LONG)
                        .show();
            } else {
                //Once we have an ISBN, start a book intent
                String ean = result.getContents();
                Log.e(TAG, "onActivityResult " + "Scanned: " + ean);
                //catch isbn10 numbers
                if (ean.length() == 10 && !ean.startsWith("978")) {
                    ean = "978" + ean;
                }
                if (ean.length() < 13) {
                    clearFields();
                    return;
                }
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, ean);
                bookIntent.setAction(BookService.FETCH_BOOK);
                getActivity().startService(bookIntent);
//                restartLoader();
            }
        } else {
            Log.d("MainActivity", "Weird");
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

//    @Override
//    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
//        if (ean.getText()
//                .length() == 0) {
//            return null;
//        }
//        String eanStr = ean.getText()
//                .toString();
//        if (eanStr.length() == 10 && !eanStr.startsWith("978")) {
//            eanStr = "978" + eanStr;
//        }
//        return new CursorLoader(
//                getActivity(),
//                AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(eanStr)),
//                null,
//                null,
//                null,
//                null
//        );
//    }
    
    public void onEventMainThread(BookService.BookEvent bookEvent){
        mBookEvent = bookEvent;
        ContentValues data = bookEvent.getBookValues();
        String bookTitleString = data.getAsString((AlexandriaContract.BookEntry.TITLE));

        bookTitle.setText(bookTitleString);

        String bookSubTitleString = data.getAsString((AlexandriaContract.BookEntry.SUBTITLE));
        bookSubTitle.setText(bookSubTitleString);

        String imgUrl = data.getAsString((AlexandriaContract.BookEntry.IMAGE_URL));
        if (Patterns.WEB_URL.matcher(imgUrl)
                .matches()) {
            new DownloadImage(bookCover).execute(imgUrl);
            bookCover
                    .setVisibility(View.VISIBLE);
        }

        saveButton
                .setVisibility(View.VISIBLE);
        deleteButton
                .setVisibility(View.VISIBLE);
    }

    
    public void onEventMainThread(BookService.AuthorEvent authorEvent){
        mAuthorEvent = authorEvent;
        List<ContentValues> listAuthorContentValues = authorEvent.getAuthorValues();
        String authorNames = "";
        for (ContentValues authorValues : listAuthorContentValues) {
            authorNames+=authorValues.getAsString((AlexandriaContract.AuthorEntry.AUTHOR))+",";
        }
       
        String[] authorsArr = authorNames.split(",");
        (authors).setLines(authorsArr.length);
        (authors).setText(authorNames.replace(",", "\n"));
    }
    
    public void onEventMainThread(BookService.CategoryEvent categoryEvent){
        mCategoryEvent = categoryEvent;
        List<ContentValues> listCategoryContentValues = categoryEvent.getCategoryValues();
        String categoryNames = "";
        for (ContentValues categoryValues : listCategoryContentValues) {
            categoryNames+=categoryValues.getAsString((AlexandriaContract.CategoryEntry.CATEGORY))+",";
        }

        (categories).setText(categoryNames.substring(0,categoryNames.length()-1));

    }

//    @Override
//    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loaders, Cursor data) {
//        if (!data.moveToFirst()) {
//            return;
//        }
//
//        String bookTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
//        bookTitle.setText(bookTitle);
//
//        String bookSubTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
//        bookSubTitle.setText(bookSubTitle);
//
//        String authors = data.getString(data.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));
//        String[] authorsArr = authors.split(",");
//        (authors).setLines(authorsArr.length);
//        (authors).setText(authors.replace(",", "\n"));
//        String imgUrl = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
//        if (Patterns.WEB_URL.matcher(imgUrl)
//                .matches()) {
//            new DownloadImage(bookCover).execute(imgUrl);
//            bookCover
//                    .setVisibility(View.VISIBLE);
//        }
//
//        String categories = data.getString(data.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));
//        (categories).setText(categories);
//
//        saveButton
//                .setVisibility(View.VISIBLE);
//        deleteButton
//                .setVisibility(View.VISIBLE);
//    }

//    @Override
//    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
//    }

    private void clearFields() {
        bookTitle.setText("");
        bookSubTitle.setText("");
        authors.setText("");
        categories.setText("");
        (bookCover)
                .setVisibility(View.INVISIBLE);
        saveButton
                .setVisibility(View.INVISIBLE);
        deleteButton
                .setVisibility(View.INVISIBLE);
    }

}
