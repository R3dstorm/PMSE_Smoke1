/* Credits: Tutorial from https://codelabs.developers.google.com/codelabs/android-room-with-a-view */

package SQLiteDatabaseModule;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.List;

public class SmokingEventViewModel extends AndroidViewModel {

    private SmokingEventRepository mRepository;
    private LiveData<List<SmokingEvent>> mAllEvents;
    private LiveData<List<SmokingEvent>> mAllValidEvents;
    private LiveData<List<SmokingEvent>> mLatestSyncLabelId;
    private LiveData<List<SmokingEvent>> mLatestEventId;

    public SmokingEventViewModel (Application application) {
        super(application);
        mRepository = new SmokingEventRepository(application);
        mAllEvents = mRepository.getAllEvents();
        mAllValidEvents = mRepository.getAllValidEvents();
        mLatestSyncLabelId = mRepository.getLatestSyncLabelId();
        mLatestEventId = mRepository.getLatestEventId();
    }

    public LiveData<List<SmokingEvent>> getAllEvents() { return mAllEvents; }

    public LiveData<List<SmokingEvent>> getAllValidEvents() { return mAllValidEvents; }

    public  LiveData<List<SmokingEvent>> getLatestSyncLabelId() { return mLatestSyncLabelId ;}

    public LiveData<List<SmokingEvent>> getLatestEventId() { return mLatestEventId;}

    public void insert(SmokingEvent event) { mRepository.insert(event); }

    public void deleteAll () {mRepository.deleteAll();}

    public LiveData<List<SmokingEvent>> getNewSyncEvents(int lastSyncLabelId){
        return mRepository.getNewSyncEvents(lastSyncLabelId);
    }

    public int setSyncLabel(int tid){
        return mRepository.setSyncLabel(tid);
    }
}
