package SQLiteDatabaseModule;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "smoking_event_table")
public class SmokingEvent {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    @ColumnInfo(name = "word")
    private String mTest;

    @NonNull
    @ColumnInfo(name = "Start_Date")
    private String startDate;

    @NonNull
    @ColumnInfo(name = "Start_Time")
    private String startTime;

    @NonNull
    @ColumnInfo(name = "Stop_Date")
    private String stopDate;

    @NonNull
    @ColumnInfo(name = "Stop_Time")
    private String stopTime;

    @NonNull
    @ColumnInfo(name = "Event_Confirmed")
    private boolean eventConfirmed;

    public SmokingEvent(@NonNull String test, @NonNull String startDate, @NonNull String startTime,
                        @NonNull String stopDate, @NonNull String stopTime,
                        @NonNull boolean eventConfirmed) {
        this.mTest = test;
        this.startDate = startDate;
        this.startTime = startTime;
        this.stopDate = stopDate;
        this.stopTime = stopTime;
        this.eventConfirmed = eventConfirmed;
    }

    public String getMTest(){return mTest;}
    public int getId(){return id;}
    public void setId(int id) {this.id = id;}
    public String getStartDate(){return startDate;}
    public String getStartTime(){return startTime;}
    public String getStopDate(){return stopDate;}
    public String getStopTime(){return stopTime;}
    public boolean getEventConfirmed(){return eventConfirmed;}
}
