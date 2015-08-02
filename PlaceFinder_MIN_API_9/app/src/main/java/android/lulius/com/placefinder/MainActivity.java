package android.lulius.com.placefinder;

import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends ActionBarActivity implements FragmentWithDetails.OnFragmentInteractionListener, FragmentWithMap.OnFragmentInteractionListener,
        FragmentWithDetails.ListFragmentListener, TextView.OnEditorActionListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        SettingsFragment.OnFragmentInteractionListener {

    private static final String TAG = "PlaceFounder";
    public static final String TAG_FAVORITES = "frag_favorites";
    private static final String TAG_MAP = "map";
    private static final String TAG_DETAILS = "details";

    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;
    /**
     * Represents a geographical location.
     */
    protected Location mLastLocation;

    private Bundle currentLocationBundle = new Bundle();
    android.support.v4.app.FragmentTransaction fragmentTransaction;
    private android.support.v4.app.FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buildGoogleApiClient();

        Context context = getApplicationContext();
        BroadcastReceiver receiver = new PowerConnectionReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        context.registerReceiver(receiver, filter);

        fragmentManager = getSupportFragmentManager();

        FragmentWithDetails fragmentDetails;
        if(isSingleFragment()){
            if(savedInstanceState==null){
                fragmentDetails = FragmentWithDetails.newInstance();
                fragmentDetails.setArguments(currentLocationBundle);
                fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.add(R.id.fragmnet_container, fragmentDetails, TAG_DETAILS);
                fragmentTransaction.commit();
            }
        }//end if we at small screen
        else{
            if(savedInstanceState==null){
                fragmentDetails = FragmentWithDetails.newInstance();
                FragmentWithMap fragmentWithMap = FragmentWithMap.newInstance(null);
                fragmentDetails.setArguments(currentLocationBundle);

                fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.add(R.id.fragmnet_container_details, fragmentDetails, TAG_DETAILS);
                fragmentTransaction.add(R.id.fragmnet_container_map, fragmentWithMap, TAG_MAP);
                fragmentTransaction.commit();
            }
        }//end if big screen
    }

    /**
     * Show favorites fragment
     */
    private void showFavorites(){
        currentLocationBundle.putInt("isShowFav", 1);

        FragmentWithDetails fragmentFavorites = (FragmentWithDetails) fragmentManager.findFragmentByTag(TAG_FAVORITES);
        if (fragmentFavorites == null) {
            fragmentFavorites = FragmentWithDetails.newInstance();
            fragmentFavorites.setArguments(currentLocationBundle);
        }

        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmnet_container, fragmentFavorites, TAG_FAVORITES);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.show(fragmentFavorites);

        handleLargeLayout();

        fragmentTransaction.commit();
    }

    /**
     * Hide details and map only on large screen
     * On small screen we reuse the same container
     */
    private void handleLargeLayout() {
        if (!isSingleFragment()) {
            fragmentTransaction.hide(getDetailsFragment());
            fragmentTransaction.hide(getMapFragment());
        }
    }

    private FragmentWithMap getMapFragment() {
        return (FragmentWithMap) fragmentManager.findFragmentByTag(TAG_MAP);
    }

    private FragmentWithDetails getDetailsFragment() {
        return (FragmentWithDetails) fragmentManager.findFragmentByTag(TAG_DETAILS);
    }

    private FragmentWithDetails getFavoritesFragment() {
        return (FragmentWithDetails) fragmentManager.findFragmentByTag(TAG_FAVORITES);
    }

    /**
     * Show settings fragment
     */
    private void showSettings(){
        SettingsFragment settingsFragment = SettingsFragment.newInstance();

        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmnet_container, settingsFragment, "frag_settings");
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.show(settingsFragment);
        handleLargeLayout();
        fragmentTransaction.commit();
    }

    /**
     * Display current location on map, if location service works
     */
    private void getCurrentLocation() {
        String currentLat = null;
        String currentLong = null ;
        if(mLastLocation != null){
            currentLat = String.valueOf(mLastLocation.getLatitude());
            currentLong = String.valueOf(mLastLocation.getLongitude());
            if (getMapFragment() != null) {
                getMapFragment().setPlace(new Place(0, "Current location", "", (float) mLastLocation.getLatitude(), (float)mLastLocation.getLongitude()));
            }

        }
        currentLocationBundle.putString("currentLat", currentLat);
        currentLocationBundle.putString("currentLong", currentLong);
    }

    /**
     * Builds a GoogleApiClient. Uses the addApi() method to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                showSettings();
                return true;
            case R.id.action_favorites:
                showFavorites();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected boolean isSingleFragment(){
        return findViewById(R.id.layout_single_fragment) != null;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onPlaceSelected(long placeId) {
        fragmentManager = getSupportFragmentManager();
        Place place = getPlace(placeId);

        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if(isSingleFragment()){

            FragmentWithMap fragmentWithMap = FragmentWithMap.newInstance(place);

            fragmentTransaction.replace(R.id.fragmnet_container, fragmentWithMap, TAG_MAP).addToBackStack(null);
        }
        else{
            if( getMapFragment() == null){
                android.support.v4.app.Fragment fragmentWithMap = FragmentWithMap.newInstance(place);
                fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.add(R.id.fragmnet_container_map, fragmentWithMap, TAG_MAP);
                fragmentTransaction.show(fragmentWithMap);
                fragmentTransaction.show(getDetailsFragment());

            }
            else{

                FragmentWithMap fragmentWithMap = getMapFragment();
                fragmentTransaction.show(fragmentWithMap);
                fragmentTransaction.show(getDetailsFragment());
                if (getFavoritesFragment() != null) {
                    fragmentTransaction.hide(getFavoritesFragment());
                }

                fragmentWithMap.showPlace(place);
            }

        }
        fragmentTransaction.commit();
    }

    /**
     * Returns place object from DB
     * @param placeId
     * @return
     */
    private Place getPlace(long placeId) {
        Cursor cursor = null;
        Place place = null;
        try {
            cursor = getContentResolver().query(PlacesContract.Places.CONTENT_URI, null, "_id=" + placeId, null, "name DESC");
            cursor.moveToNext();
            place = new Place( cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getFloat(3), cursor.getFloat(4));
        }
        finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return place;
    }

    @Override
    public void onBackPressed() {
        if (isSingleFragment() && getMapFragment() != null) {
            fragmentManager.popBackStack(null,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
        else if(fragmentManager.findFragmentByTag("frag_favorites") != null){
            fragmentManager.popBackStack(null,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
        else if(fragmentManager.findFragmentByTag("frag_settings") != null){
            fragmentManager.popBackStack(null,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
        else {
            super.onBackPressed();
        }

    }


    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();

        //start google analytics
        EasyTracker.getInstance(this).activityStart(this);  // Add this method.
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

        //stop google analytics
        EasyTracker.getInstance(this).activityStop(this);  // Add this method.
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        // Provides a simple way of getting a device's location and is well suited for
        // applications that do not require a fine-grained location and that do not need location
        // updates. Gets the best and most recent location currently available, which may be null
        // in rare cases when a location is not available.
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation == null) {
            Toast.makeText(this, "Location not found", Toast.LENGTH_LONG).show();
        }
        getCurrentLocation();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        toast("No Google Service");
//        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }


    /**
     * Displays Toast message
     * @param message
     */
    private void toast(String message) {
        Toast.makeText(this, message,
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
//        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }


}
