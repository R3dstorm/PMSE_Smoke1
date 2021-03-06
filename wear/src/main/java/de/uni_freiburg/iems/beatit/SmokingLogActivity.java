package de.uni_freiburg.iems.beatit;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.WindowManager;

import java.util.List;

import SQLiteDatabaseModule.SmokingEvent;
import SQLiteDatabaseModule.SmokingEventListAdapter;
import SQLiteDatabaseModule.SmokingEventViewModel;

public class SmokingLogActivity extends AppCompatActivity {

    final public boolean syncModuleTestEnabled = false;

    private SmokingEventViewModel mSEViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        final SmokingEventListAdapter adapter = new SmokingEventListAdapter(this);

        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        if(Globals.getInstance().isDebugMode()) {
            findViewById(R.id.buttonDelete).setVisibility(View.VISIBLE);
        }

        // Add an observer on the LiveData returned by getAlphabetizedWords.
        // The onChanged() method fires when the observed data changes and the activity is
        // in the foreground.
        mSEViewModel = ViewModelProviders.of(this).get(SmokingEventViewModel.class);

        mSEViewModel.getAllValidEvents().observe(this, new Observer<List<SmokingEvent>>() {
            @Override
            public void onChanged(@Nullable final List<SmokingEvent> events) {
                // Update the cached copy of the words in the adapter.
                adapter.setEvents(events);
            }
        });
    }

    /* TODO delete button using this functionality (DEBUG only) */
    public void delAllEvents (View v){

        /* For Test not only deleting but setting a test-set*/
        mSEViewModel.deleteAll();

        if (syncModuleTestEnabled) {
            SmokingEvent event1 = new SmokingEvent("manualEvent", "190224",
                    "000000", "190224", "000001",
                    true, false, false, "2");
            mSEViewModel.insert(event1);
            SmokingEvent event2 = new SmokingEvent("manualEvent", "190224",
                    "000100", "190224", "000101",
                    true, false, false, "3");
            mSEViewModel.insert(event2);
            SmokingEvent event3 = new SmokingEvent("manualEvent", "190224",
                    "010100", "190224", "010100",
                    true, false, false, "4");
            mSEViewModel.insert(event3);
        }
    }
}
