package eu.senseable.cigarettetracker;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.View;

import java.util.List;

import de.senseable.cloudsync.Contract;
import eu.senseable.SQLiteDatabaseModule.SmokingEvent;
import eu.senseable.SQLiteDatabaseModule.SmokingEventListAdapter;
import eu.senseable.SQLiteDatabaseModule.SmokingEventViewModel;

public class Activity extends AppCompatActivity {

    private static final String CHANNEL_ID = Activity.class.getSimpleName();
    private SmokingEventListAdapter mAdapter;
    private Account mAccount;
    private SmokingEventViewModel mSEViewModel;

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

        /* Access Database: Get a new or existing viewModel from viewModelProvider */
        final SmokingEventListAdapter adapter = new SmokingEventListAdapter(this);
        mSEViewModel = ViewModelProviders.of((FragmentActivity) this).get(SmokingEventViewModel.class); /* TODO geht daS?*/

        // Add an observer on the LiveData returned by getAlphabetizedWords.
        // The onChanged() method fires when the observed data changes and the activity is
        // in the foreground.
        mSEViewModel.getAllEvents().observe((LifecycleOwner) this, new Observer<List<SmokingEvent>>() {
            @Override
            public void onChanged(@Nullable final List<SmokingEvent> events) {
                // Update the cached copy of the words in the adapter.
                adapter.setEvents(events);
            }
        });
/*
        /** build a Notification for quick logging
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
*/
        /** register a contentobserver to run a sync when data changes, and trigger a sync */
        ContentResolver.setSyncAutomatically(getAccount(), Contract.AUTHORITY, true);
        getContentResolver().registerContentObserver(Contract.EVENTSURI, true, mSync);
        mSync.onChange(false);

    }

    @Override
    protected void onResume() {
        super.onResume();

        RecyclerView view = findViewById(R.id.my_recycler_view);
        mAdapter = new SmokingEventListAdapter(this);

        view.setLayoutManager(new LinearLayoutManager(this));
        view.setAdapter(mAdapter);

        SwipeController swipe = new SwipeController(0, ItemTouchHelper.LEFT);
        ItemTouchHelper helper = new ItemTouchHelper(swipe);
        helper.attachToRecyclerView(view);

        swipe.setOnSwipedListener(new SwipeController.Listener() {
            @Override
            public void onSwiped(RecyclerView.ViewHolder vh, int direction) {
/*
                final CigaretteEvent ev = ((SmokingEventListAdapter.SmokingEventViewHolder) vh).getItem();
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
*/
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Dialog für das hinzufügen muss geöffnet werden.
                /*
                CigaretteEvent ev = new CigaretteEvent();
                getContentResolver().insert(Contract.EVENTSURI, ev.toContentValue());
                */
            }
        });
/*
        /** special case if started by the notification intent from the CigBroadcastReceiver
        if (CigBroadcastReceiver.LOGCIG_ACTION.equals(getIntent().getAction())) {
            CigaretteEvent ev = new CigaretteEvent();
            getContentResolver().insert(Contract.EVENTSURI, ev.toContentValue());
            setIntent(new Intent());
        }
*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_counter, menu);
        return true;
    }

}
