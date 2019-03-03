package eu.senseable.cigarettetracker;

import android.Manifest;
import android.accounts.Account;
import android.app.Dialog;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.arch.persistence.room.RoomDatabase;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.wearable.Wearable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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

    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyMMdd");
    DateTimeFormatter dateYearFormatter = DateTimeFormatter.ofPattern("yy");
    DateTimeFormatter dateMonthFormatter = DateTimeFormatter.ofPattern("MM");
    DateTimeFormatter dateDayFormatter = DateTimeFormatter.ofPattern("dd");
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HHmmss");
    DateTimeFormatter timeHourFormatter = DateTimeFormatter.ofPattern("HH");
    DateTimeFormatter timeMinutesFormatter = DateTimeFormatter.ofPattern("mm");
    DateTimeFormatter exportFormatter = DateTimeFormatter.ofPattern("yyMMdd_hhmmss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /* Get permissions */
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);

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
                LocalDateTime defaultValuePopUp = LocalDateTime.now();
                String startDateDefault = defaultValuePopUp.format(dateFormatter);
                String startTimeDefault = defaultValuePopUp.format(timeFormatter);
                String startDateYearDefault = defaultValuePopUp.format(dateYearFormatter);
                String startDateMonthDefault = defaultValuePopUp.format(dateMonthFormatter);
                String startDateDayDefault = defaultValuePopUp.format(dateDayFormatter);
                String startTimeHourDefault = defaultValuePopUp.format(timeHourFormatter);
                String startTimeMinutesDefault = defaultValuePopUp.format(timeMinutesFormatter);
                String durationTimeMinutesDefault = "05";
                String durationTimeSecondsDefault = "00";

                final Dialog dia = new Dialog(Activity.this);
                dia.setContentView(R.layout.add_smoke_event);
                EditText edit=(EditText)dia.findViewById(R.id.cigdateyear);
                edit.setText(startDateYearDefault);
                edit=(EditText)dia.findViewById(R.id.cigdatemonth);
                edit.setText(startDateMonthDefault);
                edit=(EditText)dia.findViewById(R.id.cigdateday);
                edit.setText(startDateDayDefault);
                edit = (EditText) dia.findViewById(R.id.startTImeHour);
                edit.setText(startTimeHourDefault);
                edit = (EditText) dia.findViewById(R.id.startTImeMinute);
                edit.setText(startTimeMinutesDefault);
                edit = (EditText)dia.findViewById(R.id.durationminutes);
                edit.setText(durationTimeMinutesDefault);
                edit = (EditText)dia.findViewById(R.id.durationseconds);
                edit.setText(durationTimeSecondsDefault);
                dia.show();
                Button addButton = (Button) dia.findViewById(R.id.okayButton);
                addButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {

                        EditText edit=(EditText)dia.findViewById(R.id.cigdateyear);
                        startDateSmoke=edit.getText().toString();
                        edit = (EditText)dia.findViewById(R.id.cigdatemonth);
                        String tmp = edit.getText().toString();
                        startDateSmoke = startDateSmoke + tmp;
                        edit = (EditText)dia.findViewById(R.id.cigdateday);
                        tmp = edit.getText().toString();
                        startDateSmoke = startDateSmoke + tmp;
                        endDateSmoke = startDateSmoke;
                        edit = (EditText)dia.findViewById(R.id.startTImeHour);
                        startTimeSmoke= edit.getText().toString();
                        edit = (EditText)dia.findViewById(R.id.startTImeMinute);
                        tmp = edit.getText().toString();
                        startTimeSmoke = startTimeSmoke + tmp + "00"; // seconds are assumed as o seconds
                        edit = (EditText)dia.findViewById(R.id.durationminutes);
                        String durationString = edit.getText().toString();
                        edit = (EditText)dia.findViewById(R.id.durationseconds);
                        durationString = durationString + edit.getText().toString();
                        int duration = Integer.parseInt(durationString);
                        int startTimeInt = Integer.parseInt(startTimeSmoke);
                        int endTime = startTimeInt + duration;
                        endTimeSmoke = Integer.toString(endTime);
                        if(endTimeSmoke.length() == 5) {
                            endTimeSmoke = "0" + endTimeSmoke;
                        }

                        SmokingEvent ev = new SmokingEvent("Smoking", startDateSmoke, startTimeSmoke, endDateSmoke, endTimeSmoke, true, false, false);
                        mSEViewModel.insert(ev);
                        dia.dismiss();
                    }
                });

                Button abortButton = (Button) dia.findViewById(R.id.abortButton);
                abortButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dia.dismiss();
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

        FloatingActionButton fabDbExp = (FloatingActionButton) findViewById(R.id.fabDbExp);
        fabDbExp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RoomDatabase db = mSEViewModel.getDatabase();
                LocalDateTime defaultValuePopUp = LocalDateTime.now();
                String exportTime = defaultValuePopUp.format(exportFormatter);
                try {
                    Cursor c = db.query("SELECT * FROM smoking_event_table", null);
                    int rowcount = 0;
                    int colcount = 0;
                    File sdCardDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                    String filename = "SmokeEvents_" + exportTime + ".csv";
                    // the name of the file to export with
                    File saveFile = new File(sdCardDir, filename);
                    FileWriter fw = new FileWriter(saveFile);

                    BufferedWriter bw = new BufferedWriter(fw);
                    rowcount = c.getCount();
                    colcount = c.getColumnCount();
                    if (rowcount > 0) {
                        c.moveToFirst();

                        for (int i = 0; i < colcount; i++) {
                            if (i != colcount - 1) {

                                bw.write(c.getColumnName(i) + ",");

                            } else {

                                bw.write(c.getColumnName(i));

                            }
                        }
                        bw.newLine();

                        for (int i = 0; i < rowcount; i++) {
                            c.moveToPosition(i);

                            for (int j = 0; j < colcount; j++) {
                                if (j != colcount - 1)
                                    bw.write(c.getString(j) + ",");
                                else
                                    bw.write(c.getString(j));
                            }
                            bw.newLine();
                        }
                        bw.flush();
                        bw.close();
                    }
                } catch (IOException ex) {
                    if(db.isOpen()){
                        db.close();
                    }
                }
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
    protected void onStop(){
        super.onStop();
    }

    @Override
    protected void onPause() {
      super.onPause();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        dbSyncHandler.unregisterReceivers();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_counter, menu);

        return true;
    }
}
