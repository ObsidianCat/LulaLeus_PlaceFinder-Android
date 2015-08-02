package android.lulius.com.placefinder;

import android.net.Uri;

public class PlacesContract {

    // the provider's authority
    public final static String AUTHORITY = "android.lulius.com.placefinder.Provider";

    /**
     * Places table:<br>
     * holds the latest search results for places
     * @author Yak
     *
     */
    public static class Places {
        /**
         * table name
         * */
        public final static String TABLE_NAME = "places";

        /**
         * uri for the places table<br>
         * <b>content://com.example.places.provider/places</b>
         */
        public final static Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);


        // table columns names
        public final static String _ID = "_id";
        public final static String NAME = "name";
        public final static String ADDRESS = "address";
        public final static String LAT = "lat";
        public final static String LNG = "lng";
        public final static String ICON_URL = "iconURL";
        public final static String FAV = "isFavorite";

    }
}
