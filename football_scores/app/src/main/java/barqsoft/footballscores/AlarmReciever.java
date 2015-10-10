package barqsoft.footballscores;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import barqsoft.footballscores.activities.MainActivity;

/**
 * Created by ahmedrizwan on 10/10/2015.
 */
public class AlarmReciever extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (intent.getAction()
                .equals(MainActivity.FOOTBALL_ACTION)) {
            Log.e("AlarmReciever", "Yo got to the Football reciever!");
        }
    }
}
