package it.jaschke.alexandria.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import it.jaschke.alexandria.BaseActivity;
import it.jaschke.alexandria.R;
import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.databinding.FragmentAddBookBinding;
import it.jaschke.alexandria.services.BookService;
import it.jaschke.alexandria.services.DownloadImage;


public class AddBook extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "INTENT_TO_SCAN_ACTIVITY";

    private final int LOADER_ID = 1;

    private final String EAN_CONTENT = "eanContent";

    private static final String SCAN_FORMAT = "scanFormat";
    private static final String SCAN_CONTENTS = "scanContents";

    private String mScanFormat = "Format:";
    private String mScanContents = "Contents:";

    FragmentAddBookBinding mFragmentAddBookBinding;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mFragmentAddBookBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_book, container, false);

        ActionBar supportActionBar = ((BaseActivity) getActivity()).getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar
                    .setDisplayHomeAsUpEnabled(true);
            supportActionBar.setTitle(R.string.add_book);
        }

        mFragmentAddBookBinding.ean.addTextChangedListener(new TextWatcher() {
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
                AddBook.this.restartLoader();
            }
        });

        mFragmentAddBookBinding.scanButton
                .setOnClickListener(v -> {
                    if (checkCameraHardware(getActivity())) {
                        IntentIntegrator.forSupportFragment(this)
                                .initiateScan();
                    } else {
                        Toast.makeText(getActivity(), "No camera detected!", Toast.LENGTH_SHORT)
                                .show();
                    }
                });

        mFragmentAddBookBinding.saveButton
                .setOnClickListener(view -> mFragmentAddBookBinding.ean.setText(""));

        mFragmentAddBookBinding.deleteButton
                .setOnClickListener(view -> {
                    Intent bookIntent = new Intent(getActivity(), BookService.class);
                    bookIntent.putExtra(BookService.EAN, mFragmentAddBookBinding.ean.getText()
                            .toString());
                    bookIntent.setAction(BookService.DELETE_BOOK);
                    getActivity().startService(bookIntent);
                    mFragmentAddBookBinding.ean.setText("");
                });

        if (savedInstanceState != null) {
            mFragmentAddBookBinding.ean.setText(savedInstanceState.getString(EAN_CONTENT));
            mFragmentAddBookBinding.ean.setHint("");
        }

        return mFragmentAddBookBinding.getRoot();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mFragmentAddBookBinding.ean != null) {
            outState.putString(EAN_CONTENT, mFragmentAddBookBinding.ean.getText()
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

    private void restartLoader() {
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

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
                restartLoader();
            }
        } else {
            Log.d("MainActivity", "Weird");
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (mFragmentAddBookBinding.ean.getText()
                .length() == 0) {
            return null;
        }
        String eanStr = mFragmentAddBookBinding.ean.getText()
                .toString();
        if (eanStr.length() == 10 && !eanStr.startsWith("978")) {
            eanStr = "978" + eanStr;
        }
        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(eanStr)),
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
        mFragmentAddBookBinding.bookTitle.setText(bookTitle);

        String bookSubTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
        mFragmentAddBookBinding.bookSubTitle.setText(bookSubTitle);

        String authors = data.getString(data.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));
        String[] authorsArr = authors.split(",");
        (mFragmentAddBookBinding.authors).setLines(authorsArr.length);
        (mFragmentAddBookBinding.authors).setText(authors.replace(",", "\n"));
        String imgUrl = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
        if (Patterns.WEB_URL.matcher(imgUrl)
                .matches()) {
            new DownloadImage(mFragmentAddBookBinding.bookCover).execute(imgUrl);
            mFragmentAddBookBinding.bookCover
                    .setVisibility(View.VISIBLE);
        }

        String categories = data.getString(data.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));
        (mFragmentAddBookBinding.categories).setText(categories);

        mFragmentAddBookBinding.saveButton
                .setVisibility(View.VISIBLE);
        mFragmentAddBookBinding.deleteButton
                .setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
    }

    private void clearFields() {
        mFragmentAddBookBinding.bookTitle.setText("");
        mFragmentAddBookBinding.bookTitle.setText("");
        mFragmentAddBookBinding.authors.setText("");
        mFragmentAddBookBinding.categories.setText("");
        (mFragmentAddBookBinding.bookCover)
                .setVisibility(View.INVISIBLE);
        mFragmentAddBookBinding.saveButton
                .setVisibility(View.INVISIBLE);
        mFragmentAddBookBinding.deleteButton
                .setVisibility(View.INVISIBLE);
    }


}
