package de.uni_freiburg.iems.beatit;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.List;

public class SmokingLogActivity extends AppCompatActivity {

    private SmokingEventViewModel mSEViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        final SmokingEventListAdapter adapter = new SmokingEventListAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        // Add an observer on the LiveData returned by getAlphabetizedWords.
        // The onChanged() method fires when the observed data changes and the activity is
        // in the foreground.
        mSEViewModel = ViewModelProviders.of(this).get(SmokingEventViewModel.class);

        mSEViewModel.getAllEvents().observe(this, new Observer<List<SmokingEvent>>() {
            @Override
            public void onChanged(@Nullable final List<SmokingEvent> events) {
                // Update the cached copy of the words in the adapter.
                adapter.setEvents(events);
            }
        });
    }

    public void delAllEvents (View v){

        mSEViewModel.deleteAll();
    }
}
