package barqsoft.footballscores

import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService

/**
 * Created by ahmedrizwan on 10/5/15.
 */
class ListRemoteViews : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsService.RemoteViewsFactory? {
        return null
    }

    inner class MyRemoteViewFactory : RemoteViewsService.RemoteViewsFactory {

        override fun onCreate() {

        }

        override fun onDataSetChanged() {

        }

        override fun onDestroy() {

        }

        override fun getCount(): Int {
            return 0
        }

        override fun getViewAt(position: Int): RemoteViews? {
            return null
        }

        override fun getLoadingView(): RemoteViews? {
            return null
        }

        override fun getViewTypeCount(): Int {
            return 0
        }

        override fun getItemId(position: Int): Long {
            return 0
        }

        override fun hasStableIds(): Boolean {
            return false
        }
    }
}
