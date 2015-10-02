package it.jaschke.alexandria;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import it.jaschke.alexandria.databinding.ActivityMainBinding;
import it.jaschke.alexandria.fragments.AddBook;
import it.jaschke.alexandria.fragments.BookDetail;
import it.jaschke.alexandria.fragments.ListOfBooks;

/**
 * Created by ahmedrizwan on 9/18/15.
 * Holds methods that handle the tablet-mobile fragment loading (according to navigation model)
 */
public class FragmentNavigation {

    public static boolean tabletMode(final ActivityMainBinding activityMainBinding) {
        return activityMainBinding.listContainer != null;
    }

    public static void launchAddBooksFragment(final AppCompatActivity context, final ActivityMainBinding activityMainBinding) {
        if (tabletMode(activityMainBinding)) {
            //Show dialog fragment for the AddBook
            AddBook.getInstance().show(context.getSupportFragmentManager(),"AddBook");
        } else {
            context.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(activityMainBinding.includeContainer.getId(), new AddBook())
                    .addToBackStack(null)
                    .commit();
        }
    }

    public static void launchBookListFragment(final AppCompatActivity context, final ActivityMainBinding activityMainBinding) {
        //check if listContainer isn't null (tablet mode)
        if (tabletMode(activityMainBinding)) {
            //it's tablet mode
            //add the book list fragment in listContainer
            context.getSupportFragmentManager().beginTransaction()
                    .replace(activityMainBinding.listContainer.getId(), new ListOfBooks())
                    .addToBackStack(null)
                    .commit();
        } else {
            context.getSupportFragmentManager().beginTransaction()
                    .replace(activityMainBinding.includeContainer.getId(), new ListOfBooks())
                    .addToBackStack(null)
                    .commit();
        }
    }

    public static void launchBookDetailFragment(final BaseActivity activity,
                                                final ActivityMainBinding activityMainBinding, final BookDetail fragment) {
        Log.e("Fragment", "Here");
        if(tabletMode(activityMainBinding)) {

            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(activityMainBinding.detailsContainer.getId(), fragment)
                    .commit();
            //hide the "Select book to show details" message
            activityMainBinding.textViewSelectBook.setVisibility(View.GONE);
        }else {
            activity.getSupportFragmentManager().beginTransaction()
                .replace(activityMainBinding.includeContainer.getId(), fragment)
                .addToBackStack("Book Detail")
                .commit();
        }
    }

    public static void removeDetailsFragment(final ActivityMainBinding activityMainBinding, final BaseActivity activity, BookDetail fragment){
        if(tabletMode(activityMainBinding)) {
            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .remove(fragment)
                    .commit();
            //hide the "Select book to show details" message
            activityMainBinding.textViewSelectBook.setVisibility(View.VISIBLE);
        } else{
            activity.getSupportFragmentManager().popBackStack();
        }
    }
}
