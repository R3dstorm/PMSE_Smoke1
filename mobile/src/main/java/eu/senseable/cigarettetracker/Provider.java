package eu.senseable.cigarettetracker;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.OperationCanceledException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.senseable.cloudsync.Contract;

/**
 * Created by phil on 29.06.18.
 */

public class Provider extends ContentProvider {
    private static final int ALLEVENTS = 1;
    private static final int SINGLEEVENT = 2;
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private static final String TAG = Provider.class.getSimpleName();

    static {
        uriMatcher.addURI(Contract.AUTHORITY, Contract.EVENTS, ALLEVENTS);
        uriMatcher.addURI(Contract.AUTHORITY, Contract.EVENTS+"/#", SINGLEEVENT);
    }

    private SQLiteHelper mDb;
    public class SQLiteHelper extends SQLiteOpenHelper {
        public SQLiteHelper(Context c) {
            super(c, Contract.EVENTS, null, 2);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE EVENTS " +
                       "(id INTEGER PRIMARY KEY AUTOINCREMENT," +
                       Contract.TIMESTAMP + " DATETIME)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int v1, int v2) {
            db.execSQL("DROP TABLE events");
            this.onCreate(db);
        }
    }

    @Override
    public boolean onCreate() {
        mDb = new SQLiteHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri,
                        @Nullable String[] projection,
                        @Nullable String selection,
                        @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {
        Cursor cursor = null;
        SQLiteDatabase db = mDb.getReadableDatabase();

        if (sortOrder == null)
            sortOrder = Contract.TIMESTAMP + " DESC";

        switch(uriMatcher.match(uri)) {
            case ALLEVENTS:
                cursor = db.query(Contract.EVENTS, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case SINGLEEVENT:
                List<String> where = selectionArgs != null ?
                        Arrays.asList(selectionArgs) :
                        new ArrayList<String>();
                where.add(uri.getLastPathSegment());

                selection = selection == null ? "id = ?" : selection + " id = ?";

                cursor = db.query(Contract.EVENTS, projection, selection, where.toArray(new String[]{}), null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("invalid uri: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch(uriMatcher.match(uri)) {
            case ALLEVENTS:
                return "vnd.android.cursor.dir/vnd.de.senseable.manual.events";
            case SINGLEEVENT:
                return "vnd.android.cursor.item/vnd.de.senseable.manual.events";
            default:
                return null;
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        if (uriMatcher.match(uri) != ALLEVENTS)
            throw new IllegalArgumentException("unknown uri: " + uri);

        SQLiteDatabase db = mDb.getWritableDatabase();
        long id = db.insert(Contract.EVENTS, null, contentValues);

        if (id > 0) {
            uri = ContentUris.withAppendedId(uri, id);
            getContext().getContentResolver().notifyChange(uri, null);
            return uri;
        }

        throw new OperationCanceledException("unable to insert");
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] args) {
        SQLiteDatabase db = mDb.getWritableDatabase();
        int count = 0;

        switch (uriMatcher.match(uri)) {
            case SINGLEEVENT:
                List<String> where = args != null ? Arrays.asList(args) : new ArrayList<String>();
                where.add(uri.getLastPathSegment());

                selection = selection == null ? "" : selection;
                selection += "id = ?";

                count = db.delete(Contract.EVENTS, selection, where.toArray(new String[]{}));
                break;
            case ALLEVENTS:
                count = db.delete(Contract.EVENTS, selection, args);
                break;
            default:
                throw new IllegalArgumentException("unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(@NonNull Uri uri,
                      @Nullable ContentValues contentValues,
                      @Nullable String selection,
                      @Nullable String[] args) {
        throw new IllegalArgumentException("not implemented");
    }
}
