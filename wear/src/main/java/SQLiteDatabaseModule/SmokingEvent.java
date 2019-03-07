package SQLiteDatabaseModule;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.example.commondataobjects.SmokingEventDTO;

@Entity(indices = {@Index(value = "Unique_ID")},tableName = "smoking_event_table")
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

    @NonNull
    @ColumnInfo(name = "Is_Sync_Label")
    private boolean isSyncLabel;

    @NonNull
    @ColumnInfo(name = "Removed")
    private boolean removed;

    @NonNull
    @ColumnInfo(name = "Unique_ID")
    private String uniqueID;

    public SmokingEvent(@NonNull String test, @NonNull String startDate, @NonNull String startTime,
                        @NonNull String stopDate, @NonNull String stopTime,
                        @NonNull boolean eventConfirmed, @NonNull boolean isSyncLabel,
                        @NonNull boolean removed, @NonNull String uniqueID) {
        this.mTest = test;
        this.startDate = startDate;
        this.startTime = startTime;
        this.stopDate = stopDate;
        this.stopTime = stopTime;
        this.eventConfirmed = eventConfirmed;
        this.isSyncLabel = isSyncLabel;
        this.removed = removed;
        this.uniqueID = uniqueID;
    }

    public String getMTest(){return mTest;}
    public int getId(){return id;}
    public void setId(int id) {this.id = id;}
    public String getStartDate(){return startDate;}
    public String getStartTime(){return startTime;}
    public String getStopDate(){return stopDate;}
    public String getStopTime(){return stopTime;}
    public boolean getEventConfirmed(){return eventConfirmed;}
    public boolean getIsSyncLabel(){return isSyncLabel;}
    public boolean getRemoved(){return removed;}
    public String getUniqueID(){return uniqueID;}
    public SmokingEventDTO getTransferObject(){
        return new SmokingEventDTO(id, mTest, startDate, startTime, stopDate, stopTime, eventConfirmed, isSyncLabel, removed, uniqueID);
    }
    public SmokingEvent setTransferObject(SmokingEventDTO eventDto){
        this.mTest = eventDto.getTest();
        this.startDate = eventDto.getStartDate();
        this.startTime = eventDto.getStartTime();
        this.stopDate = eventDto.getStopDate();
        this.stopTime = eventDto.getStopTime();
        this.eventConfirmed = eventDto.isEventConfirmed();
        this.isSyncLabel = eventDto.isSyncLabel();
        this.removed = eventDto.isRemoved();
        this.uniqueID = eventDto.getUniqueID();
        return this;
    }

}
