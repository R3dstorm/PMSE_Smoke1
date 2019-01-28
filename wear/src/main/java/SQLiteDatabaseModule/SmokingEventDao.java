/* Credits: Major parts of sources from https://codelabs.developers.google.com/codelabs/android-room-with-a-view */

package SQLiteDatabaseModule;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface SmokingEventDao {
    @Insert
    void insert(SmokingEvent event);

    @Query("DELETE FROM smoking_event_table")
    void deleteAll();

    @Query("SELECT * from smoking_event_table ORDER BY Start_Date, Start_Time ASC")
    LiveData<List<SmokingEvent>> getAllEvents();
}
