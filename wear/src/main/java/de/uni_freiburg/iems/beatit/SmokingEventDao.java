package de.uni_freiburg.iems.beatit;

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

    @Query("SELECT * from smoking_event_table ORDER BY word ASC")
    LiveData<List<SmokingEvent>> getAllEvents();
}
