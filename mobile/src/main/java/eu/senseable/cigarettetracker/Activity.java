package eu.senseable.cigarettetracker;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Dialog;
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
import android.widget.Button;
import android.widget.EditText;

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
    private String startDateSmoke = "";
    private String startTimeSmoke = "";
    private String endDateSmoke = "";
    private String endTimeSmoke = "";
    private Synchronize dbSyncHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counter);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        dbSyncHandler = new Synchronize(this);

        /* Access Database: Get a new or existing viewModel from viewModelProvider */
        mSEViewModel = ViewModelProviders.of((FragmentActivity) this).get(SmokingEventViewModel.class); /* TODO geht daS?*/

        RecyclerView recyclerView = findViewById(R.id.my_recycler_view);
        final SmokingEventListAdapter adapter = new SmokingEventListAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

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

    }

    @Override
    protected void onResume() {
        super.onResume();

//        RecyclerView view = findViewById(R.id.my_recycler_view);
//        mAdapter = new SmokingEventListAdapter(this);
//
//        view.setLayoutManager(new LinearLayoutManager(this));
//        view.setAdapter(mAdapter);
//
//        SwipeController swipe = new SwipeController(0, ItemTouchHelper.LEFT);
//        ItemTouchHelper helper = new ItemTouchHelper(swipe);
//        helper.attachToRecyclerView(view);
//
//        swipe.setOnSwipedListener(new SwipeController.Listener() {
//            @Override
//            public void onSwiped(RecyclerView.ViewHolder vh, int direction) {
///*
//                final CigaretteEvent ev = ((SmokingEventListAdapter.SmokingEventViewHolder) vh).getItem();
//                getContentResolver().delete(Contract.EVENTURI(ev.mID),null,null);
//
//                Snackbar.make(findViewById(R.id.layout),
//                        getString(R.string.removed),
//                        Snackbar.LENGTH_LONG)
//                        .setAction(getString(R.string.UNDO), new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//                                getContentResolver().insert(Contract.EVENTSURI, ev.toContentValue());
//                            }
//                        })
//                        .show();
//*/
//            }
//        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Dialog für das hinzufügen muss geöffnet werden.
                final Dialog dia = new Dialog(Activity.this);
                dia.setContentView(R.layout.add_smoke_event);
                dia.show();
                Button button = (Button) dia.findViewById(R.id.okayButton);
                button.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {

                        EditText edit=(EditText)dia.findViewById(R.id.cigdate);
                        startDateSmoke=edit.getText().toString();
                        endDateSmoke = startDateSmoke;
                        edit = (EditText)dia.findViewById(R.id.startTIme);
                        startTimeSmoke= edit.getText().toString();
                        edit = (EditText)dia.findViewById(R.id.duration);
                        int duration = Integer.parseInt(edit.getText().toString());
                        int startTimeInt = Integer.parseInt(startTimeSmoke);
                        int endTime = startTimeInt + duration;
                        endTimeSmoke = Integer.toString(endTime);

                        SmokingEvent ev = new SmokingEvent("Smoking", startDateSmoke, startTimeSmoke, endDateSmoke, endTimeSmoke, true, false, false);
                        mSEViewModel.insert(ev);
                    }
                });

                /*
                CigaretteEvent ev = new CigaretteEvent();
                getContentResolver().insert(Contract.EVENTSURI, ev.toContentValue());
                */
            }
        });

        FloatingActionButton fabDel = (FloatingActionButton) findViewById(R.id.fabDel);
        fabDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSEViewModel.deleteAll();
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
