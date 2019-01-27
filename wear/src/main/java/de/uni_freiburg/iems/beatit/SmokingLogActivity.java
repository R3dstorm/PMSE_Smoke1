package de.uni_freiburg.iems.beatit;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.List;

public class SmokingLogActivity extends AppCompatActivity {

    private SmokingEventViewModel mSEViewModel2;

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
        mSEViewModel2 = ViewModelProviders.of(this).get(SmokingEventViewModel.class); /* TODO geht das?*/

        mSEViewModel2.getAllEvents().observe(this, new Observer<List<SmokingEvent>>() {
            @Override
            public void onChanged(@Nullable final List<SmokingEvent> events) {
                // Update the cached copy of the words in the adapter.
                adapter.setWords(events);
            }
        });
    }

    public void addNewEvent (View v){
        SmokingEvent event = new SmokingEvent("gedrueckt", "20190102",
                "1125", "20190102", "1200", true);
        mSEViewModel2.insert(event);
        LiveData<List<SmokingEvent>> blub = mSEViewModel2.getAllEvents();
        List dub = blub.getValue();
        if (dub != null) {
            if (dub.size() > 0) {
                String cub = ((SmokingEvent) dub.get(0)).getStartDate();
                /* TODO zugriff nur über Observer möglich?*/
            }
        }

    }
}
