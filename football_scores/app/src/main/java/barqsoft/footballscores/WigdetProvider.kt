package barqsoft.footballscores

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import barqsoft.footballscores.adapters.ScoresListAdapter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by ahmedrizwan on 10/5/15.
 *
 */
class WigdetProvider : AppWidgetProvider() {

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
    }

    override fun onUpdate(context: Context?, appWidgetManager: AppWidgetManager?, appWidgetIds: IntArray?) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        appWidgetIds?.forEach {
            val mformat = SimpleDateFormat("yyyy-MM-dd")
            val fragmentdate = Date(System.currentTimeMillis())
            val selectionArgs: Array<String> = Array(1, { i -> mformat.format(fragmentdate) })
            val cursor = context?.contentResolver?.query(DatabaseContract.scores_table.buildScoreWithDate(),
                    null, null, selectionArgs, null)

            val views = RemoteViews(context?.packageName, R.layout.widget_layout)

            if (cursor != null && cursor.count > 0) {
                val scoreAdapter: ScoresListAdapter = ScoresListAdapter(context!!, cursor, 0)
            }

            appWidgetManager?.updateAppWidget(it, views)
        }

    }

}