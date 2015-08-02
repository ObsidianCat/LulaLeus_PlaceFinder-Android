package android.lulius.com.placefinder;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {
    private static final int DB_VERSION = 4;
    private static final String DB_NAME = "places.db";
    public DbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql =
                "CREATE TABLE " + PlacesContract.Places.TABLE_NAME
                        +"("
                        + PlacesContract.Places._ID + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                        + PlacesContract.Places.NAME + " TEXT ,"
                        + PlacesContract.Places.ADDRESS + " TEXT ,"
                        + PlacesContract.Places.LAT + " FLOAT ,"
                        + PlacesContract.Places.LNG + " FLOAT ,"
                        + PlacesContract.Places.ICON_URL + " TEXT ,"
                        + PlacesContract.Places.FAV + " INTEGER"
                        +")";
        db.execSQL(sql);
    }

    public void setFavorite(int id) {
        String sql = "UPDATE " + PlacesContract.Places.TABLE_NAME + " SET " + PlacesContract.Places.FAV + " = 1 "+
                " WHERE " + PlacesContract.Places._ID + " = " + id;
        this.getWritableDatabase().execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "DROP TABLE IF EXISTS " + PlacesContract.Places.TABLE_NAME;
        db.execSQL(sql);
        onCreate(db);
    }
}
