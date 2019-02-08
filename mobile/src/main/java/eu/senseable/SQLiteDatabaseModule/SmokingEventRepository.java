/* Credits: Major parts of sources from https://codelabs.developers.google.com/codelabs/android-room-with-a-view */

package eu.senseable.SQLiteDatabaseModule;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

public class SmokingEventRepository {

    private SmokingEventDao mEventDao;
    private LiveData<List<SmokingEvent>> mAllEvents;

    SmokingEventRepository(Application application) {
        SmokingEventRoomDatabase db = SmokingEventRoomDatabase.getDatabase(application);
        mEventDao = db.smokingEventDao();
        mAllEvents = mEventDao.getAllEvents();
    }

    LiveData<List<SmokingEvent>> getAllEvents() {
        return mAllEvents;
    }


    public void insert (SmokingEvent event) {
        new insertAsyncTask(mEventDao).execute(event);
    }

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

}
