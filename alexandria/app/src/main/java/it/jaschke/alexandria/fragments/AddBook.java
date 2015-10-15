package it.jaschke.alexandria.fragments;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.greenrobot.event.EventBus;
import it.jaschke.alexandria.BaseActivity;
import it.jaschke.alexandria.FragmentNavigation;
import it.jaschke.alexandria.MainActivity;
import it.jaschke.alexandria.R;
import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.services.BookService;

public class AddBook extends DialogFragment {
    private static final String TAG = "INTENT_TO_SCAN_ACTIVITY";

    private final String EAN_CONTENT = "eanContent";

    BookService.BookEvent mBookEvent;
    BookService.AuthorEvent mAuthorEvent;
    BookService.CategoryEvent mCategoryEvent;

    EditText mEditText;
    View addButton;
    View cancelButton;
    TextView bookTitle;
    TextView bookSubTitle;
    ImageView bookCover;
    TextView authors;
    TextView categories;
    TextView mTextViewError;


    public static AddBook getInstance(Bundle bundle) {
        final AddBook addBook = new AddBook();
        addBook.setArguments(bundle);
        return addBook;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_add_book, container, false);
        mEditText = (EditText) rootView.findViewById(R.id.ean);
        addButton = rootView.findViewById(R.id.addButton);
        cancelButton = rootView.findViewById(R.id.cancelButton);
        bookTitle = (TextView) rootView.findViewById(R.id.bookTitle);
        bookSubTitle = (TextView) rootView.findViewById(R.id.bookSubTitle);
        bookCover = (ImageView) rootView.findViewById(R.id.bookCover);
        authors = (TextView) rootView.findViewById(R.id.authors);
        categories = (TextView) rootView.findViewById(R.id.categories);
        mTextViewError = (TextView) rootView.findViewById(R.id.textViewError);

        EventBus.getDefault()
                .register(this);
        ActionBar supportActionBar = ((BaseActivity) getActivity()).getSupportActionBar();
        if (supportActionBar != null && !((MainActivity) getActivity()).tabletMode()) {
            supportActionBar
                    .setDisplayHomeAsUpEnabled(true);
            supportActionBar.setTitle(R.string.add_book);
        }

        mEditText.addTextChangedListener(new TextWatcher() {
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
                //Hide keyboard
                ((BaseActivity) getActivity()).hideKeyboard(mEditText);

                //Once we have an ISBN, start a book intent
                getBookThroughService(ean);
            }
        });

        addButton.setOnClickListener(view -> {
            //save the book
            saveBook();
            Snackbar.make(addButton, "Book Added...", Snackbar.LENGTH_SHORT)
                    .show();
            //reset the fields
            mEditText.setText("");
            closeTheFragment();
        });


        cancelButton.setOnClickListener(view -> {
           closeTheFragment();
        });

        if (savedInstanceState != null) {
            mEditText.setText(savedInstanceState.getString(EAN_CONTENT));
            mEditText.setHint("");
        } else {
            //Extra check
            final Bundle arguments = getArguments();
            if (arguments != null) {
                final boolean scan = arguments.getBoolean(getString(R.string.key_scan), false);
                if (scan) {
                    if (checkCameraHardware(getActivity())) {
                        IntentIntegrator.forSupportFragment(this)
                                .initiateScan();
                    } else {
                        Toast.makeText(getActivity(), "No camera detected!", Toast.LENGTH_SHORT)
                                .show();
                    }
                }
            }
        }

        return rootView;
    }

    private void closeTheFragment() {
        dismiss();
        if (!FragmentNavigation.tabletMode(((MainActivity) getActivity()).getBinding()))
            getActivity().getSupportFragmentManager()
                    .popBackStack();
    }

    private void saveBook() {
        ContentResolver contentResolver = getActivity().getContentResolver();
        contentResolver.insert(AlexandriaContract.BookEntry.CONTENT_URI, mBookEvent.getBookValues());
        try {
            for (ContentValues contentValues : mAuthorEvent.getAuthorValues()) {
                contentResolver.insert(AlexandriaContract.AuthorEntry.CONTENT_URI, contentValues);
            }
        } catch (NullPointerException e) {
        }
        try {
            for (ContentValues contentValues : mCategoryEvent.getCategoryValues()) {
                contentResolver.insert(AlexandriaContract.CategoryEntry.CONTENT_URI, contentValues);
            }
        } catch (NullPointerException e) {
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault()
                .unregister(this);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mEditText != null) {
            outState.putString(EAN_CONTENT, mEditText.getText()
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Barcode scan result
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
                mEditText.setText(ean);
                getBookThroughService(ean);
            }
        } else {
            Log.d("MainActivity", "Weird");
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void getBookThroughService(final String ean) {
        mTextViewError.setVisibility(View.VISIBLE);
        mTextViewError.setText(R.string.message_wait);
        Intent bookIntent = new Intent(getActivity(), BookService.class);
        bookIntent.putExtra(BookService.EAN, ean);
        bookIntent.setAction(BookService.FETCH_BOOK);
        getActivity().startService(bookIntent);
    }

    public void onEventMainThread(BookService.BookEvent bookEvent) {
        mTextViewError.setVisibility(View.GONE);
        mBookEvent = bookEvent;
        ContentValues data = bookEvent.getBookValues();
        String bookTitleString = data.getAsString((AlexandriaContract.BookEntry.TITLE));

        bookTitle.setText(bookTitleString);

        String bookSubTitleString = data.getAsString((AlexandriaContract.BookEntry.SUBTITLE));
        bookSubTitle.setText(bookSubTitleString);

        String imgUrl = data.getAsString((AlexandriaContract.BookEntry.IMAGE_URL));
        if (Patterns.WEB_URL.matcher(imgUrl)
                .matches()) {
            if (!imgUrl.isEmpty()) {
                Picasso.with(getActivity())
                        .load(imgUrl)
                        .into(bookCover);
            } else
                bookCover.setImageResource(R.drawable.ic_not_available);

            bookCover.setVisibility(View.VISIBLE);
        }

        addButton
                .setVisibility(View.VISIBLE);
        cancelButton
                .setVisibility(View.VISIBLE);
    }


    public void onEventMainThread(BookService.AuthorEvent authorEvent) {
        mAuthorEvent = authorEvent;
        List<ContentValues> listAuthorContentValues = authorEvent.getAuthorValues();
        String authorNames = "";
        for (ContentValues authorValues : listAuthorContentValues) {
            authorNames += authorValues.getAsString((AlexandriaContract.AuthorEntry.AUTHOR)) + ",";
        }

        String[] authorsArr = authorNames.split(",");
        (authors).setLines(authorsArr.length);
        (authors).setText(authorNames.replace(",", "\n"));
    }


    public void onEventMainThread(BookService.CategoryEvent categoryEvent) {
        mCategoryEvent = categoryEvent;
        List<ContentValues> listCategoryContentValues = categoryEvent.getCategoryValues();
        String categoryNames = "";
        for (ContentValues categoryValues : listCategoryContentValues) {
            categoryNames += categoryValues.getAsString((AlexandriaContract.CategoryEntry.CATEGORY)) + ",";
        }

        (categories).setText(categoryNames.substring(0, categoryNames.length() - 1));
    }

    public void onEventMainThread(Exception exception) {
        mTextViewError.setText(exception.getMessage());
        mTextViewError.setVisibility(View.VISIBLE);
    }

    private void clearFields() {
        bookTitle.setText("");
        bookSubTitle.setText("");
        authors.setText("");
        categories.setText("");
        (bookCover)
                .setVisibility(View.INVISIBLE);
        addButton
                .setVisibility(View.INVISIBLE);
        cancelButton
                .setVisibility(View.INVISIBLE);
    }

}
