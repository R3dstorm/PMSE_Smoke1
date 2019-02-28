/* Credits: Tutorial from https://codelabs.developers.google.com/codelabs/android-room-with-a-view */

package eu.senseable.SQLiteDatabaseModule;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

public class SmokingEventRepository {

    private SmokingEventDao mEventDao;
    private LiveData<List<SmokingEvent>> mAllEvents;
    private LiveData<List<SmokingEvent>> mAllValidEvents;
    private LiveData<List<SmokingEvent>> mLatestSyncLabelID;
    private LiveData<List<SmokingEvent>> mLatestEventId;
    private SmokingEventRoomDatabase mSmokeEventDatabase;


    public SmokingEventRepository(Application application) {
        mSmokeEventDatabase = SmokingEventRoomDatabase.getDatabase(application);
        mEventDao = mSmokeEventDatabase.smokingEventDao();
        mAllEvents = mEventDao.getAllEvents();
        mAllValidEvents = mEventDao.getAllValidEvents();
        mLatestSyncLabelID = mEventDao.getLatestSyncLabelId();
        mLatestEventId = mEventDao.getLatestEventId();
    }

    /* TODO Remove/Refactor ...Test- Methods*/

    LiveData<List<SmokingEvent>> getAllEvents() {
        return mAllEvents;
    }

    public List<SmokingEvent> getAllEventsList(){ return mEventDao.getAllEventsList();}

    LiveData<List<SmokingEvent>> getAllValidEvents(){
        return mAllValidEvents;
    }

    LiveData<List<SmokingEvent>> getLatestSyncLabelId(){
        return mLatestSyncLabelID;
    }

    public List<SmokingEvent> getLatestSyncLabelIdTest() {return mEventDao.getLatestSyncLabelIdTest();}

    LiveData<List<SmokingEvent>> getLatestEventId(){ return mLatestEventId; }

    public List<SmokingEvent> getLatestEventIdTest() {return mEventDao.getLatestEventIdTest();}

    LiveData<List<SmokingEvent>> getNewSyncEvents(int lastSyncLabelId){
        return mEventDao.getNewSyncEvents(lastSyncLabelId);
    }

    public List<SmokingEvent> getNewSyncEventsTest(int lastSyncLabelId){
        return mEventDao.getNewSyncEventsTest(lastSyncLabelId);
    }

    public int setSyncLabel(int tid){
        return mEventDao.setSyncLabel(tid);
    }

    public void insert (SmokingEvent event) {
        new insertAsyncTask(mEventDao).execute(event);
    }

    public void insertBlocking (SmokingEvent event) { mEventDao.insert(event);}

    private static class insertAsyncTask extends AsyncTask<SmokingEvent, Void, Void> {

        private SmokingEventDao mAsyncTaskDao;

        insertAsyncTask(SmokingEventDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final SmokingEvent... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    /* TODO parameters are not necessary -> remove*/
    public void deleteAll() {new deleteAsyncTask(mEventDao).execute();}

    private static class deleteAsyncTask extends AsyncTask<SmokingEvent, Void, Void> {

        private SmokingEventDao mAsyncTaskDao;

        deleteAsyncTask(SmokingEventDao dao) { mAsyncTaskDao = dao;}

        @Override
        protected  Void doInBackground(final SmokingEvent... params){
            mAsyncTaskDao.deleteAll();
            return null;
        }
    }

    public SmokingEventRoomDatabase getDatabase() {
        return mSmokeEventDatabase;
    }

}