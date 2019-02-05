/* Credits: Major parts of sources from https://codelabs.developers.google.com/codelabs/android-room-with-a-view */

package SQLiteDatabaseModule;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface SmokingEventDao {
    @Insert
    void insert(SmokingEvent event);

    @Query("DELETE FROM smoking_event_table")
    void deleteAll();

    /* Get all events */
    @Query("SELECT * from smoking_event_table ORDER BY Start_Date, Start_Time ASC")
    LiveData<List<SmokingEvent>> getAllEvents();

    /* Get all valid events (not removed) */
    @Query("SELECT * from smoking_event_table WHERE removed = 0 ORDER BY Start_Date, Start_Time ASC")
    LiveData<List<SmokingEvent>> getAllValidEvents();

    /* Query to get latest syncLabel */
    @Query("SELECT id from smoking_event_table WHERE Is_Sync_Label = 1 ORDER BY id DESC LIMIT 1")
    int getLatestSyncLabelId();

    /* Query to get latest element id */
    @Query("SELECT id from smoking_event_table ORDER BY id DESC LIMIT 1")
    int getLatestEventId();

    /* Query to get all elements created after last syncLabel */
    @Query("SELECT * from smoking_event_table WHERE id > :lastSyncLabelId ORDER BY id DESC")
    LiveData<List<SmokingEvent>> getNewSyncEvents(int lastSyncLabelId);

    /* Update the sync label */
    @Query("UPDATE smoking_event_table SET Is_Sync_Label = 1 WHERE id = :tid")
    int setSyncLabel(int tid);
}
