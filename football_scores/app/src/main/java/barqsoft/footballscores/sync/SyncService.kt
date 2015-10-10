package barqsoft.footballscores.sync

import android.app.Service
import android.content.Intent
import android.os.IBinder

class SyncService : Service() {

    override fun onCreate() {
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = SyncAdapter(applicationContext, true)
            }
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return sSyncAdapter!!.syncAdapterBinder
    }

    companion object {
        private val sSyncAdapterLock = Object()
        private var sSyncAdapter: SyncAdapter? = null
    }
}