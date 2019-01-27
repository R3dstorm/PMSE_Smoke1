package de.uni_freiburg.iems.beatit;

/* Credits: Sources from https://codelabs.developers.google.com/codelabs/android-room-with-a-view */

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.List;

public class SmokingEventViewModel extends AndroidViewModel {

    private SmokingEventRepository mRepository;

    private LiveData<List<SmokingEvent>> mAllEvents;

    public SmokingEventViewModel (Application application) {
        super(application);
        mRepository = new SmokingEventRepository(application);
        mAllEvents = mRepository.getAllEvents();
    }

    LiveData<List<SmokingEvent>> getAllEvents() { return mAllEvents; }

    public void insert(SmokingEvent event) { mRepository.insert(event); }

}
