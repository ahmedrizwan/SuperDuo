package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViewsService;

/**
 * Created by ahmedrizwan on 12/10/2015.
 *
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class WidgetViewsService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(final Intent intent) {
        Log.e("RemoteService", "onGetViewFactory");
        return new WidgetFactory(this.getApplicationContext(), intent);
    }
}


