package android.lulius.com.placefinder;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import java.util.List;

public class AppProvider extends ContentProvider {
    private DbHelper dbHelper;

    @Override
    public boolean onCreate() {
        //create the dbHelper:
        dbHelper = new DbHelper(getContext());
        if (dbHelper != null) {
            //success
            return false;
        } else {
            //fail
            return true;
        }
    }

    // -- helper method
    // get the first part of the uri path - this is the table name
    // example:
    // content://com.example.places.provider/places
    // the table name is - "places"
    protected String getTableName(Uri uri){
        List<String> pathSegments = uri.getPathSegments();
        return pathSegments.get(0);
    }

    @Override
    public String getType(Uri uri) {
        //this is not really important...
        return null;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,String[] selectionArgs, String sortOrder) {

        //get a db :
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        //do the query:
        Cursor result = db.query(
                getTableName(uri),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);

        // register the cursor to track changes on the uri
        result.setNotificationUri(getContext().getContentResolver(), uri);

        return result;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        //get a db:
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //do the insert:
        long id = db.insertWithOnConflict(
                getTableName(uri),
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE);

        //notify the change
        getContext().getContentResolver().notifyChange(uri, null);

        if (id > 0) {
            //return a uri to the inserted row:
            // ( i.e content://com.example.places.provider/places/12587 )
            return ContentUris.withAppendedId(uri, id);
        }else{
            // or null if non inserted
            return null;
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        //get a db:
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // do the update
        int result = db.update(
                getTableName(uri),
                values,
                selection,
                selectionArgs);

        //notify the change
        getContext().getContentResolver().notifyChange(uri, null);
        return result;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        //get a db:
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //do the delete:
        int result = db.delete(
                getTableName(uri),
                selection,
                selectionArgs);

        //notify the change
        getContext().getContentResolver().notifyChange(uri, null);
        return result;
    }

}
