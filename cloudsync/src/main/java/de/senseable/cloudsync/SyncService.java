package de.senseable.cloudsync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by phil on 29.06.18.
 */

public class SyncService extends Service {
    private SyncAdapter mSyncAdapter = null;
    private Object syncLock = new Object();

    @Override
    public void onCreate() {
        super.onCreate();

        /* create a syncadapter singleton, with no parallel syncs */
        synchronized(syncLock) {
            if (mSyncAdapter == null)
                mSyncAdapter = new SyncAdapter(getApplicationContext(), true);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mSyncAdapter.getSyncAdapterBinder();
    }
}
