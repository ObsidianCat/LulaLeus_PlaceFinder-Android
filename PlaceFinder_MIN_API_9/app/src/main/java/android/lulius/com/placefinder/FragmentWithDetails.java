package android.lulius.com.placefinder;

import android.app.Activity;
import android.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.support.v4.content.CursorLoader;
import android.content.Intent;
import android.support.v4.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.support.v4.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragmentWithDetails.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FragmentWithDetails#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentWithDetails extends android.support.v4.app.Fragment implements AdapterView.OnItemClickListener, TextView.OnEditorActionListener,
        View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor>{


    private static final String TAG = "PlaceFounderSearch";
    public static final String SEARCH_TYPE_NEARBY = "nearbysearch/";
    public static final String SEARCH_TYPE_TEXT = "textsearch/";
    public static final int MIN_QUERY_LENGTH = 2;
    ListFragmentListener listener;

    private EditText editTextSearch;

    private SimpleCursorAdapter adapter;
    private boolean isShowFav;
    private String currentLat;
    private String currentLong;

    private boolean isFirstLaunch = true;
    private Button buttonSearchWithLocation;
    private Button buttonSearch;

    public View v;
    private ListView list;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FragmentWithDetails.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentWithDetails newInstance() {
        FragmentWithDetails fragment = new FragmentWithDetails();

        return fragment;
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (ListFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("activity " + activity.toString()
                    + "must implement ListFragmentListener!");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        currentLat = this.getArguments().getString("currentLat");
        currentLong = this.getArguments().getString("currentLong");

        if (isFirstLaunch) {
            isShowFav = this.getArguments().getInt("isShowFav") > 0;
        }

        if (isShowFav) {
            v = inflater.inflate(R.layout.fragment_fragment_favorites, container, false);
        }
        else {
            v = inflater.inflate(R.layout.fragment_fragment_with_search, container, false);
        }

        // create the adapter:
        // no data at start (cursor = null)
        String[] from = {PlacesContract.Places.NAME,PlacesContract.Places.ADDRESS,PlacesContract.Places.LNG, PlacesContract.Places.ICON_URL, PlacesContract.Places._ID, PlacesContract.Places.FAV};
        int[] to = {R.id.name, R.id.address, R.id.distance, R.id.placeIcon, R.id.id, R.id.iconGoldStar};

        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        String currentDistanceUnits = sharedPref.getString("distanceUnits", "kilometers");

        adapter = new SimpleCursorAdapter(
                getActivity().getApplicationContext(), //context
                android.lulius.com.placefinder.R.layout.item_element,
                null, // no cursor at start
                from, // from - cursor's columns
                to ,  // to - view ids
                0); //flags

        adapter.setViewBinder(new LocationItemViewBinder(currentLat, currentLong, getActivity().getPreferences(Context.MODE_PRIVATE)));

        //the list view
        list = (ListView) v.findViewById(R.id.list);

        //connect list-adapter:
        list.setAdapter(adapter);

        list.setOnItemClickListener(this);

        registerForContextMenu(list);


        //init a cursor loader:
        getActivity().getSupportLoaderManager().initLoader(isShowFav?1:2, null, this);


        if (!isShowFav) {
            buttonSearchWithLocation = (Button) v.findViewById(R.id.buttonSearchWithLocation);

            buttonSearch = (Button) v.findViewById(R.id.buttonSearch);
            editTextSearch = (EditText) v.findViewById(R.id.editTextSearch);

            editTextSearch.setOnEditorActionListener(this);
            buttonSearch.setOnClickListener(this);
            buttonSearchWithLocation.setOnClickListener(this);
        }
        else {
            Cursor favorites = getFavorites();
            if (favorites == null || favorites.getCount() == 0) {
                toast("You don't have any favorites, please go back and add some");
            }
            adapter.swapCursor(favorites);
        }
        if (isFirstLaunch && !isShowFav) {
            isFirstLaunch = false;
            this.searchAroundLocation("", currentLat, currentLong);
        }
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    private Cursor getFavorites() {
        return getActivity().getContentResolver().query(PlacesContract.Places.CONTENT_URI, null, PlacesContract.Places.FAV+"=:fav", new String[] {"1"}, "");
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        searchByName(getSearchKeyword());
        return true;
    }

    private String getSearchKeyword() {
        String query = "";
        if (editTextSearch != null) {
            try {
                query = editTextSearch.getText().toString();
                query = query.trim();
                query = URLEncoder.encode(query, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return query;
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        String keyword = getSearchKeyword();
        switch (v.getId()){
            case R.id.buttonSearch :{
                searchByName(keyword);
                break;
            }
            case R.id.buttonSearchWithLocation :{
                searchAroundLocation(keyword, currentLat, currentLong);
                break;
            }
        }//end of switch
    }

    /**
     * Instantiate and return a new Loader for the given ID.
     *
     * @param id   The ID whose loader is to be created.
     * @param args Any arguments supplied by the caller.
     * @return Return a new Loader instance that is ready to start loading.
     */
    @Override
    public synchronized Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //the uri - places table in the provider
        Uri uri = PlacesContract.Places.CONTENT_URI;

        // all columns
        String[] projection = null;

        String selection = null;

        if(isShowFav){
            selection = PlacesContract.Places.FAV + " = 1";
        }
        else {
            selection = PlacesContract.Places.FAV + " = 0";
        }

        // sort by name
        String sortOrder =PlacesContract.Places.NAME + " asc";

        // create the loader:
        return new CursorLoader(getActivity().getApplicationContext(), uri, projection, selection, null, sortOrder);
    }

    /**
     *
     * @param loader The Loader that has finished.
     * @param cursor   The data generated by the Loader.
     */
    @Override
    public synchronized void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        //cursor in ready
        //swap it into the adapter:
        adapter.swapCursor(cursor);
    }

    /**
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable.  The application should at this point
     * remove any references it has to the Loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public synchronized void onLoaderReset(Loader<Cursor> loader) {
        // cursor is going to be reset
        // remove it from the adapter:
        adapter.swapCursor(null);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onItemClick(AdapterView<?> listView, View itemView, int pos,
                            long id) {
       // get the clicked place:
       long placeId = listView.getAdapter().getItemId(pos);

        // Bubble up to the activity.
        listener.onPlaceSelected(placeId);
    }

    // -- the fragment listener interface
    // -- the containing activity must implement this
    public interface ListFragmentListener {
        void onPlaceSelected(long placeId);
    }

    private void searchByName(String keyword) {
        if(keyword.length()> MIN_QUERY_LENGTH){
            toast("Searching for: " + keyword);
            getActivity().startService(prepareIntent(null, null, keyword, SEARCH_TYPE_TEXT));
        }
        else{
            toast(String.format("Please type at least %s characters", MIN_QUERY_LENGTH));
        }
    }


    public void searchAroundLocation(String keyword, String currentLat, String currentLong) {
        if(keyword.equals("")){
            if (currentLat == null || currentLong == null) {
                toast("Please enable location or enter a keyword to search");
            }
            else {
                toast("Searching places around current location");
                getActivity().startService(prepareIntent(currentLat, currentLong, "", SEARCH_TYPE_NEARBY));
            }
        }
        else{

            if(keyword.length() > MIN_QUERY_LENGTH){
                toast("Searching places around current location with: " + keyword);
                getActivity().startService(prepareIntent(currentLat, currentLong, keyword, SEARCH_TYPE_NEARBY));
            }
            else{
                toast("Please type at least two characters");
            }
        }
    }

    private Intent prepareIntent(String currentLat, String currentLong, String keyword, String searchType) {
        Intent intent = new Intent(getActivity(),SearchPlacesService.class);
        intent.putExtra("currentLat", currentLat);
        intent.putExtra("currentLong", currentLong);
        intent.putExtra("keyword", keyword);
        intent.putExtra("searchType", searchType);
        return intent;
    }

    /**
     * Displays Toast message
     * @param message
     */
    private void toast(String message) {
        Toast.makeText(getActivity(), message,
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);

        //hidden textView that contains id of every item in the list
        TextView idTextView = (TextView) v.findViewById(R.id.id);
        String id = idTextView.getText().toString();

        if (isInFavorites(id)) {
            menu.add(0, ContextMenuItem.REMOVE_FROM_FAVORITES.ordinal(), 1, "Remove from favorites");
        }
        else {
            menu.add(0, ContextMenuItem.ADD_TO_FAVORITES.ordinal(), 2, "Add to favorites");
        }
    }

    private enum ContextMenuItem {
        ADD_TO_FAVORITES,
        REMOVE_FROM_FAVORITES
    }

    private boolean isInFavorites(String id) {
        Activity activity = getActivity();
        ContentResolver contentResolver  = activity.getContentResolver();
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(PlacesContract.Places.CONTENT_URI, new String[] {PlacesContract.Places.FAV},
                    PlacesContract.Places._ID + "=?", new String[] {id}, "");
            cursor.moveToNext();
            return cursor.getInt(0) > 0;
        }
        finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        MainActivity activity = (MainActivity) this.getActivity();
        ContentResolver contentResolver = activity.getContentResolver();
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        String id = getItemIdFromInfo(info);
        if (item.getItemId() == ContextMenuItem.ADD_TO_FAVORITES.ordinal()) {
            toast("Place added to your favorites");
            ContentValues values = new ContentValues();
            values.put(PlacesContract.Places.FAV, 1);
            contentResolver.update(PlacesContract.Places.CONTENT_URI, values, PlacesContract.Places._ID + "=?", new String[] {id});
        }
        else if (item.getItemId() == ContextMenuItem.REMOVE_FROM_FAVORITES.ordinal()) {
            ContentValues values = new ContentValues();
            values.put(PlacesContract.Places.FAV, 0);
            contentResolver.update(PlacesContract.Places.CONTENT_URI, values, PlacesContract.Places._ID + "=?", new String[] {id});

            toast("Place removed from your favorites");
        }
        else {
            Cursor cursor = null;
            try {
                cursor = contentResolver.query(PlacesContract.Places.CONTENT_URI, new String[] {PlacesContract.Places.NAME, PlacesContract.Places.ADDRESS},
                        PlacesContract.Places._ID + "=?", new String[] {id}, "");
                cursor.moveToNext();
                String placeName = cursor.getString(0);
                String placeAddress = cursor.getString(1);
                String placeShareFormatted = placeName+": "+placeAddress;
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(android.content.Intent.EXTRA_TEXT, placeShareFormatted);
                startActivity(intent);
            }
            finally {
                closeCursor(cursor);
            }
        }
        refreshList();
        return true;
    }

    private void closeCursor(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

    private String getItemIdFromInfo(AdapterView.AdapterContextMenuInfo info) {
        View view = info.targetView;
        TextView idTextView = (TextView) view.findViewById(R.id.id);
        return idTextView.getText().toString();
    }

    private void refreshList() {
//        adapter.notifyDataSetChanged();
        MainActivity activity = (MainActivity) this.getActivity();
        adapter.swapCursor(activity.getContentResolver().query(PlacesContract.Places.CONTENT_URI, new String[] {}, PlacesContract.Places.FAV + "=0", new String[] {}, "name asc"));
    }

}
