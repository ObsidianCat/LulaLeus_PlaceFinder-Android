package android.lulius.com.placefinder;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * The service created as explicit, and not implicit as required, for supporting API 21
 */
public class SearchPlacesService extends IntentService {
    private static final String TAG = "SearchService";

    public SearchPlacesService() {

        super("SearchPlacesService");
    }

    /**
     * This method is invoked on the worker thread with a request to process.
     * Only one Intent is processed at a time, but the processing happens on a
     * worker thread that runs independently from other application logic.
     * So, if this code takes a long time, it will hold up other requests to
     * the same IntentService, but it will not hold up anything else.
     * When all requests have been handled, the IntentService stops itself,
     * so you should not call {@link #stopSelf}.
     *
     * @param intent The value passed to {@link
     *               android.content.Context#startService(android.content.Intent)}.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        boolean isNearbySearch = false;
        String result = null;
        String searchType = intent.getStringExtra("searchType");
        if(searchType.equals(FragmentWithDetails.SEARCH_TYPE_NEARBY)){
            String currentLat = intent.getStringExtra("currentLat");
            String currentLong = intent.getStringExtra("currentLong");
            String keyword = intent.getStringExtra("keyword");
            String radius = "500";
            result = GoogleAccess.searchPlace(searchType, currentLat, currentLong,keyword, radius);
            isNearbySearch = true;
        }
        else{
            String query = intent.getStringExtra("keyword");
            result = GoogleAccess.searchPlace(query, searchType);
        }
        // search:
        // (we're using the GoogleAccess class to do the actual call to the API)
        if (result != null) {
            // first - delete all data from the provider (places table):
            getContentResolver().delete(PlacesContract.Places.CONTENT_URI, PlacesContract.Places.FAV+"=:fav", new String[] {"0"});
        }
        else {
            // This will display message to user
            Handler h = new Handler(getBaseContext().getMainLooper());

            h.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getBaseContext(), "No Internet connection", Toast.LENGTH_LONG).show();
                }
            });
            return;
        }

        // parse the json response:
        try {

            //the response object:
            JSONObject jsonResult = new JSONObject(result);

            //get the "results" array
            JSONArray resultsArray = jsonResult.getJSONArray("results");

            for (int i = 0; i < resultsArray.length(); i++) {
                //the current item's object:
                JSONObject place = resultsArray.getJSONObject(i);
                JSONObject placeGeometry = place.getJSONObject("geometry");
                JSONObject placeGeometryLocation = placeGeometry.getJSONObject("location");

                //get the relevant fields:
                String lat = placeGeometryLocation.getString("lat");
                String lng = placeGeometryLocation.getString("lng");
                String address = null;
                if (isNearbySearch) {
                    address = place.getString("vicinity");
                }
                else {
                    address = place.getString("formatted_address");
                }

                String name = place.getString("name");
                String iconURL = place.getString("icon");

                // prepare an ContentValues to insert:
                ContentValues values = new ContentValues();

                values.put(PlacesContract.Places.NAME, name);
                values.put(PlacesContract.Places.ADDRESS, address);
                values.put(PlacesContract.Places.LAT, lat);
                values.put(PlacesContract.Places.LNG, lng);
                values.put(PlacesContract.Places.ICON_URL, iconURL);
                values.put(PlacesContract.Places.FAV, 0);

                //insert into the provider (places table)
                getContentResolver().insert(PlacesContract.Places.CONTENT_URI, values);
            }
        } catch (JSONException je) {
            je.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
