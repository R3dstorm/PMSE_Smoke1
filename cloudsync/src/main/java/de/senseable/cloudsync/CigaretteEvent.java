package de.senseable.cloudsync;

import android.content.ContentValues;
import android.database.Cursor;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.TimeZone;

/**
 * Created by phil on 28.05.18.
 */

public class CigaretteEvent {
    private static DateTimeFormatter human = DateTimeFormat.forPattern("EEEEE dd MMMMM, HH:mm:ss");
    private static DateTimeFormatter iso = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SZ");
    public DateTime mTimeStamp;
    public long mID;

    public CigaretteEvent() {
        mTimeStamp = new DateTime().withZone(DateTimeZone.UTC);
    }

    @Override
    public String toString() { return human.print(mTimeStamp); };


    public static CigaretteEvent fromCSV(String line) {
        String[] tokens = line.split(" ");
        CigaretteEvent ev = new CigaretteEvent();
        ev.mID = Integer.parseInt(tokens[0]);
        ev.mTimeStamp = iso.parseDateTime(tokens[2]);
        return ev;
    }

    public static CigaretteEvent fromCursor(Cursor cur) {
        CigaretteEvent cig = new CigaretteEvent();
        int i = cur.getColumnIndex(Contract.TIMESTAMP);
        cig.mTimeStamp = iso.parseDateTime(cur.getString(i));
        i = cur.getColumnIndex(Contract.ID);
        cig.mID = cur.getLong(i);
        return cig;
    }

    public String toIsoString() {
        return iso.print(mTimeStamp);
    }

    public ContentValues toContentValue() {
        ContentValues cv = new ContentValues(1);
        cv.put(Contract.TIMESTAMP, iso.print(mTimeStamp));
        return cv;
    }

    public DateTime getTimestamp() {
        return mTimeStamp;
    }
}
