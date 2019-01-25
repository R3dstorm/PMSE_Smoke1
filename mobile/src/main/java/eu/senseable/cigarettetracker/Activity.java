package eu.senseable.cigarettetracker;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.view.Menu;

import de.senseable.cloudsync.CigaretteEvent;
import de.senseable.cloudsync.Contract;

public class Activity extends AppCompatActivity {

    private static final String CHANNEL_ID = Activity.class.getSimpleName();
    private CigaretteEventAdapter mAdapter;
    private Account mAccount;

    private ContentObserver mSync = new ContentObserver(new Handler()) {
        Bundle mExtras = null;
        public Bundle getExtras() {
            if (mExtras != null)
                return mExtras;

            mExtras = new Bundle();
            //mExtras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
            //mExtras.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
            return mExtras;
        }

        @Override
        public boolean deliverSelfNotifications() {
            return false;
        }

        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            ContentResolver.requestSync(getAccount(), Contract.AUTHORITY, getExtras());
        }
    };


    private static final String ACCOUNT = "dummyaccount";
    private static final String ACCOUNT_TYPE = "de.senseable.cloudsync";
    public Account getAccount() {
        if (mAccount != null)
            return mAccount;

        Account acc = new Account(ACCOUNT, ACCOUNT_TYPE);
        AccountManager mgr = (AccountManager) getApplicationContext().getSystemService(ACCOUNT_SERVICE);

        mgr.addAccountExplicitly(acc, null, null);
        mAccount = acc;

        return mAccount;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counter);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /** build a Notification for quick logging */
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(this, CigBroadcastReceiver.class);
        PendingIntent pintent = PendingIntent.getBroadcast(this, 0, intent, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "abc", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        Notification n = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.logcigtitle))
                .setContentText(getString(R.string.logcig))
                .setSmallIcon(R.drawable.ic_cig)
                .setOngoing(true)
                .setContentIntent(pintent)
                .build();

        notificationManager.notify(007, n);

        /** register a contentobserver to run a sync when data changes, and trigger a sync */
        ContentResolver.setSyncAutomatically(getAccount(), Contract.AUTHORITY, true);
        getContentResolver().registerContentObserver(Contract.EVENTSURI, true, mSync);
        mSync.onChange(false);

    }

    @Override
    protected void onResume() {
        super.onResume();

        RecyclerView view = findViewById(R.id.my_recycler_view);
        mAdapter = new CigaretteEventAdapter(this);

        view.setLayoutManager(new LinearLayoutManager(this));
        view.setAdapter(mAdapter);

        SwipeController swipe = new SwipeController(0, ItemTouchHelper.LEFT);
        ItemTouchHelper helper = new ItemTouchHelper(swipe);
        helper.attachToRecyclerView(view);

        swipe.setOnSwipedListener(new SwipeController.Listener() {
            @Override
            public void onSwiped(RecyclerView.ViewHolder vh, int direction) {
                final CigaretteEvent ev = ((CigaretteEventAdapter.ViewHolder) vh).getItem();
                getContentResolver().delete(Contract.EVENTURI(ev.mID),null,null);

                Snackbar.make(findViewById(R.id.layout),
                        getString(R.string.removed),
                        Snackbar.LENGTH_LONG)
                        .setAction(getString(R.string.UNDO), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                getContentResolver().insert(Contract.EVENTSURI, ev.toContentValue());
                            }
                        })
                        .show();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CigaretteEvent ev = new CigaretteEvent();
                getContentResolver().insert(Contract.EVENTSURI, ev.toContentValue());
            }
        });

        /** special case if started by the notification intent from the CigBroadcastReceiver */
        if (CigBroadcastReceiver.LOGCIG_ACTION.equals(getIntent().getAction())) {
            CigaretteEvent ev = new CigaretteEvent();
            getContentResolver().insert(Contract.EVENTSURI, ev.toContentValue());
            setIntent(new Intent());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_counter, menu);
        return true;
    }

}
