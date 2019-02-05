/* Credits: Major parts of sources from https://codelabs.developers.google.com/codelabs/android-room-with-a-view */

package SQLiteDatabaseModule;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

@Database(entities = {SmokingEvent.class}, version = 2)
public abstract class SmokingEventRoomDatabase extends RoomDatabase {

    public abstract SmokingEventDao smokingEventDao();

    // marking the instance as volatile to ensure atomic access to the variable
    private static volatile SmokingEventRoomDatabase INSTANCE;

    static SmokingEventRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (SmokingEventRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            SmokingEventRoomDatabase.class, "event_database")
                            // Wipes and rebuilds instead of migrating if no Migration object.
                            // Migration is not part of this codelab.
                            .fallbackToDestructiveMigration()
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Override the onOpen method to populate the database.
     * For this sample, we clear the database every time it is created or opened.
     *
     * If you want to populate the database only when the database is created for the 1st time,
     * override RoomDatabase.Callback()#onCreate
     */
    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {

        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            // If you want to keep the data through app restarts,
            // comment out the following line.
            new PopulateDbAsync(INSTANCE).execute();
        }
    };

    /**
     * Populate the database in the background.
     * If you want to start with more words, just add them.
     */
    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

        private final SmokingEventDao mDao;

        PopulateDbAsync(SmokingEventRoomDatabase db) {
            mDao = db.smokingEventDao();
        }

        @Override
        protected Void doInBackground(final Void... params) {
            // Start the app with a clean database every time.
            // Not needed if you only populate on creation.
            mDao.deleteAll();

            SmokingEvent event = new SmokingEvent("firstEvent", "20190101",
                    "1125", "20190102", "1200", false, false,false);
            mDao.insert(event);
            return null;
        }
    }
}
