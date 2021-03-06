package eu.senseable.cigarettetracker;

import android.Manifest;
import android.app.Dialog;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.joda.time.DateTime;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import eu.senseable.SQLiteDatabaseModule.SmokingEvent;
import eu.senseable.SQLiteDatabaseModule.SmokingEventListAdapter;
import eu.senseable.SQLiteDatabaseModule.SmokingEventViewModel;

public class Activity extends AppCompatActivity {

    private static final String CHANNEL_ID = Activity.class.getSimpleName();
    private SmokingEventListAdapter mAdapter;
    private SmokingEventViewModel mSEViewModel;
    private String startDateSmoke = "";
    private String startTimeSmoke = "";
    private String endDateSmoke = "";
    private String endTimeSmoke = "";
    private Synchronize dbSyncHandler;
    private static final String LOG_TAG = "ExternalStorageController";

    DateTimeFormatter dateYearFormatter = DateTimeFormatter.ofPattern("yy");
    DateTimeFormatter dateMonthFormatter = DateTimeFormatter.ofPattern("MM");
    DateTimeFormatter dateDayFormatter = DateTimeFormatter.ofPattern("dd");
    DateTimeFormatter timeHourFormatter = DateTimeFormatter.ofPattern("HH");
    DateTimeFormatter timeMinutesFormatter = DateTimeFormatter.ofPattern("mm");
    DateTimeFormatter exportFormatter = DateTimeFormatter.ofPattern("yyMMdd_hhmmss");
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyMMdd");
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HHmmss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /* Get permissions */
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

        setContentView(R.layout.activity_counter);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        dbSyncHandler = new Synchronize(this);

        /* Access Database: Get a new or existing viewModel from viewModelProvider */
        mSEViewModel = ViewModelProviders.of((FragmentActivity) this).get(SmokingEventViewModel.class);

        final RecyclerView recyclerView = findViewById(R.id.my_recycler_view);
        final SmokingEventListAdapter adapter = new SmokingEventListAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Add an observer on the LiveData returned by getAlphabetizedWords.
        // The onChanged() method fires when the observed data changes and the activity is
        // in the foreground.
        mSEViewModel.getAllValidEvents().observe((LifecycleOwner) this, new Observer<List<SmokingEvent>>() {
            @Override
            public void onChanged(@Nullable final List<SmokingEvent> events) {
                // Update the cached copy of the words in the adapter.
                adapter.setEvents(events);
            }
        });

        SwipeController swipe = new SwipeController(0, ItemTouchHelper.LEFT);
        ItemTouchHelper helper = new ItemTouchHelper(swipe);
        helper.attachToRecyclerView(recyclerView);

        swipe.setOnSwipedListener(new SwipeController.Listener() {
            @Override
            public void onSwiped(RecyclerView.ViewHolder vh, int direction) {
                final SmokingEvent ev = ((SmokingEventListAdapter.SmokingEventViewHolder) vh).getItem();

                mSEViewModel.removeEvent(ev.getId());

                Snackbar.make(findViewById(R.id.layout),
                        getString(R.string.removed),
                        Snackbar.LENGTH_LONG)
                        .setAction(getString(R.string.UNDO), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                mSEViewModel.restoreEvent(ev.getId());
                            }
                        })
                        .show();

            }
        });

        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(this, recyclerView ,new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        // do whatever
                    }

                    @Override public boolean onDoubleTap(RecyclerView.ViewHolder view, int position) {
                        // do whatever
                        final SmokingEvent ev = ((SmokingEventListAdapter.SmokingEventViewHolder) view).getItem();

                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd HHmmss");
                        LocalDateTime start = LocalDateTime.parse(ev.getStartDate() + " " + ev.getStartTime(), formatter);
                        LocalDateTime stop = LocalDateTime.parse(ev.getStopDate() + " " + ev.getStopTime(), formatter);
                        Duration duration = Duration.between(start, stop);
                        String durationMinutes = String.format("%02d", duration.getSeconds() / 60);
                        String durationSeconds = String.format("%02d", duration.getSeconds() % 60);
                        String day = String.format("%02d", start.getDayOfMonth());
                        String year = (String.format("%02d", start.getYear())).substring(2,4);
                        String month = String.format("%02d", start.getMonth().getValue());
                        String hours = String.format("%02d", start.getHour());
                        String minutes = String.format("%02d", start.getMinute());
                        final Dialog dia = new Dialog(Activity.this);
                        dia.setContentView(R.layout.add_smoke_event);
                        TextView title = dia.findViewById(R.id.txt_dia);
                        title.setText("Edit Cigarette");
                        LinearLayout verifiedLayout = dia.findViewById(R.id.verifiedLayout);
                        verifiedLayout.getLayoutParams().height = LinearLayout.LayoutParams.MATCH_PARENT;

                        setDefaultValuesForDialog(dia, year, month, day,
                                hours, minutes,
                                durationMinutes, durationSeconds);

                        final CheckBox verified = dia.findViewById(R.id.smokeEventVerified);
                        verified.setChecked(ev.getEventConfirmed());

                        dia.show();

                        Button addButton = (Button) dia.findViewById(R.id.okayButton);
                        addButton.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                boolean checkInputs = checkInputParameter(dia);
                                if (checkInputs) {
                                    convertInputDataToEventData(dia);

                                    boolean eventConfirmed = verified.isChecked();

                                    SmokingEvent editedEvent = new SmokingEvent("Smoking", startDateSmoke,
                                            startTimeSmoke, endDateSmoke, endTimeSmoke, eventConfirmed,
                                            false, false, UUID.randomUUID().toString());
                                    mSEViewModel.removeEvent(ev.getId());
                                    mSEViewModel.insert(editedEvent);
                                    dia.dismiss();
                                }
                            }
                        });

                        Button abortButton = (Button) dia.findViewById(R.id.abortButton);
                        abortButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dia.dismiss();
                            }
                        });
                        return true;
                    }
                })
        );
    }

    @Override
    protected void onResume() {
        super.onResume();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Dialog für das hinzufügen muss geöffnet werden.
                LocalDateTime defaultValuePopUp = LocalDateTime.now();
                String startDateYearDefault = defaultValuePopUp.format(dateYearFormatter);
                String startDateMonthDefault = defaultValuePopUp.format(dateMonthFormatter);
                String startDateDayDefault = defaultValuePopUp.format(dateDayFormatter);
                String startTimeHourDefault = defaultValuePopUp.format(timeHourFormatter);
                String startTimeMinutesDefault = defaultValuePopUp.format(timeMinutesFormatter);
                String durationTimeMinutesDefault = "05";
                String durationTimeSecondsDefault = "00";

                final Dialog dia = new Dialog(Activity.this);
                dia.setContentView(R.layout.add_smoke_event);
                CheckBox checkBox = dia.findViewById(R.id.smokeEventVerified);
                checkBox.setVisibility(View.INVISIBLE);
                TextView textView = dia.findViewById(R.id.verifiedText);
                textView.setVisibility(View.INVISIBLE);
                TextView title = dia.findViewById(R.id.txt_dia);
                title.setText("New Cigarette");
                LinearLayout verifiedLayout = dia.findViewById(R.id.verifiedLayout);
                verifiedLayout.getLayoutParams().height = 0;

                setDefaultValuesForDialog(dia, startDateYearDefault, startDateMonthDefault, startDateDayDefault,
                        startTimeHourDefault, startTimeMinutesDefault,
                        durationTimeMinutesDefault, durationTimeSecondsDefault);

                dia.show();
                Button addButton = (Button) dia.findViewById(R.id.okayButton);
                addButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        boolean checkInputs = checkInputParameter(dia);
                        if (checkInputs) {
                            convertInputDataToEventData(dia);

                            SmokingEvent ev = new SmokingEvent("Smoking", startDateSmoke,
                                    startTimeSmoke, endDateSmoke, endTimeSmoke, true,
                                    false, false, UUID.randomUUID().toString());
                            mSEViewModel.insert(ev);
                            dia.dismiss();
                        }
                    }
                });

                Button abortButton = (Button) dia.findViewById(R.id.abortButton);
                abortButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dia.dismiss();
                    }
                });
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
                    Cursor c = db.query("SELECT * FROM smoking_event_table WHERE removed = 0", null);
                    int rowcount = 0;
                    int colcount = 0;
                    String filename = "SmokeEvents_" + exportTime + ".csv";
                    // the name of the file to export with
                    File saveFile = new File(getPublicAppStorageDir().getAbsolutePath() + "/" + filename);
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
                    if (db.isOpen()) {
                        db.close();
                    }
                }
                Toast.makeText(Activity.this, "Data exported in folder Download", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbSyncHandler.unregisterReceivers();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_counter, menu);

        return true;
    }

    public File getPublicAppStorageDir() {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), "BeatSmoking");
        if (!file.exists()) {
            file.mkdirs();
        }
        if (!file.mkdirs()) {
            Log.i(LOG_TAG, "Directory not created");
        }
        return file;
    }

    private boolean checkInputParameter(Dialog dia) {
        EditText edit = (EditText) dia.findViewById(R.id.cigdateyear);
        int inputStartDateSmoke = Integer.parseInt(edit.getText().toString());
        if (inputStartDateSmoke < 18 || inputStartDateSmoke > 19) {
            Toast.makeText(this, "Incorrect year value", Toast.LENGTH_LONG).show();
            return false;
        }
        edit = (EditText) dia.findViewById(R.id.cigdatemonth);
        inputStartDateSmoke = Integer.parseInt(edit.getText().toString());
        if (inputStartDateSmoke < 1 || inputStartDateSmoke > 12) {
            Toast.makeText(this, "Incorrect month value", Toast.LENGTH_LONG).show();
            return false;
        }
        edit = (EditText) dia.findViewById(R.id.cigdateday);
        inputStartDateSmoke = Integer.parseInt(edit.getText().toString());
        if (inputStartDateSmoke < 1 || inputStartDateSmoke > 31) {
            Toast.makeText(this, "Incorrect day value", Toast.LENGTH_LONG).show();
            return false;
        }
        edit = (EditText) dia.findViewById(R.id.startTImeHour);
        int inputStartTimeData = Integer.parseInt(edit.getText().toString());
        if (inputStartTimeData < 0 || inputStartTimeData > 24) {
            Toast.makeText(this, "Incorrect hour value", Toast.LENGTH_LONG).show();
            return false;
        }
        edit = (EditText) dia.findViewById(R.id.startTImeMinute);
        inputStartTimeData = Integer.parseInt(edit.getText().toString());
        if (inputStartTimeData < 0 || inputStartTimeData > 59) {
            Toast.makeText(this, "Incorrect minutes value", Toast.LENGTH_LONG).show();
            return false;
        }
        edit = (EditText) dia.findViewById(R.id.durationminutes);
        int durationTime = Integer.parseInt(edit.getText().toString());
        if (durationTime < 0 || durationTime > 59) {
            Toast.makeText(this, "Incorrect duration minutes value", Toast.LENGTH_LONG).show();
            return false;
        }
        edit = (EditText) dia.findViewById(R.id.durationseconds);
        durationTime = Integer.parseInt(edit.getText().toString());
        if (durationTime < 0 || durationTime > 59) {
            Toast.makeText(this, "Incorrect duration seconds value", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void convertInputDataToEventData(Dialog dia) {
        DateTime dateTime = new DateTime();
        EditText edit = (EditText) dia.findViewById(R.id.cigdateyear);
        startDateSmoke = edit.getText().toString();
        if(startDateSmoke.length() == 1)
        {
            startDateSmoke = "0" + startDateSmoke;
        }
        edit = (EditText) dia.findViewById(R.id.cigdatemonth);
        String tmp = edit.getText().toString();
        if(tmp.length() == 1)
        {
            tmp = "0" + tmp;
        }
        startDateSmoke = startDateSmoke + tmp;
        edit = (EditText) dia.findViewById(R.id.cigdateday);
        tmp = edit.getText().toString();
        if(tmp.length() == 1)
        {
            tmp = "0" + tmp;
        }
        startDateSmoke = startDateSmoke + tmp;
        edit = (EditText) dia.findViewById(R.id.startTImeHour);
        startTimeSmoke = edit.getText().toString();
        if(startTimeSmoke.length() == 1)
        {
            startTimeSmoke = "0" + startTimeSmoke;
        }
        edit = (EditText) dia.findViewById(R.id.startTImeMinute);
        tmp = edit.getText().toString();
        if(tmp.length() == 1)
        {
            tmp = "0" + tmp;
        }
        startTimeSmoke = startTimeSmoke + tmp + "00"; // seconds are assumed as o seconds
        edit = (EditText) dia.findViewById(R.id.durationminutes);
        String durationString = edit.getText().toString();
        int durationMinutes = Integer.parseInt(durationString);
        edit = (EditText) dia.findViewById(R.id.durationseconds);
        int durationSeconds = Integer.parseInt(edit.getText().toString());
        durationString = durationString + edit.getText().toString();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd HHmmss");
        LocalDateTime stop = LocalDateTime.parse(startDateSmoke + " " + startTimeSmoke, formatter);
        stop = stop.plus(durationMinutes, ChronoUnit.MINUTES);
        stop = stop.plus(durationSeconds, ChronoUnit.SECONDS);
        int duration = Integer.parseInt(durationString);
        int startTimeInt = Integer.parseInt(startTimeSmoke);

        endDateSmoke = stop.format(dateFormatter);
        endTimeSmoke = stop.format(timeFormatter);


        if (endTimeSmoke.length() == 5) {
            endTimeSmoke = "0" + endTimeSmoke;
        }
    }


    private void setDefaultValuesForDialog(Dialog dia, String startDateYearDefault, String startDateMonthDefault,
                                           String startDateDayDefault, String startTimeHourDefault, String startTimeMinutesDefault,
                                           String durationTimeMinutesDefault, String durationTimeSecondsDefault) {
        EditText edit = (EditText) dia.findViewById(R.id.cigdateyear);
        edit.setText(startDateYearDefault);
        edit = (EditText) dia.findViewById(R.id.cigdatemonth);
        edit.setText(startDateMonthDefault);
        edit = (EditText) dia.findViewById(R.id.cigdateday);
        edit.setText(startDateDayDefault);
        edit = (EditText) dia.findViewById(R.id.startTImeHour);
        edit.setText(startTimeHourDefault);
        edit = (EditText) dia.findViewById(R.id.startTImeMinute);
        edit.setText(startTimeMinutesDefault);
        edit = (EditText) dia.findViewById(R.id.durationminutes);
        edit.setText(durationTimeMinutesDefault);
        edit = (EditText) dia.findViewById(R.id.durationseconds);
        edit.setText(durationTimeSecondsDefault);
    }
}

    class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener {
        private OnItemClickListener mListener;

        public interface OnItemClickListener {
            public void onItemClick(View view, int position);

            public boolean onDoubleTap(RecyclerView.ViewHolder view, int position);
        }

        GestureDetector mGestureDetector;

        public RecyclerItemClickListener(Context context, final RecyclerView recyclerView, OnItemClickListener listener) {
            mListener = listener;
            mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    RecyclerView.ViewHolder vh = recyclerView.findContainingViewHolder(child);
                    if (child != null && mListener != null) {
                        mListener.onDoubleTap(vh, recyclerView.getChildAdapterPosition(child));
                    }
                    return true;
                }
            });
        }

        @Override public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
            View childView = view.findChildViewUnder(e.getX(), e.getY());
            if (childView != null && mListener != null && mGestureDetector.onTouchEvent(e)) {
                mListener.onItemClick(childView, view.getChildAdapterPosition(childView));
                return true;
            }
            return false;
        }

        @Override public void onTouchEvent(RecyclerView view, MotionEvent motionEvent) { }

        @Override
        public void onRequestDisallowInterceptTouchEvent (boolean disallowIntercept){}
    }

