package android.lulius.com.placefinder;

import android.app.Activity;
import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragmentWithMap.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FragmentWithMap#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentWithMap extends android.support.v4.app.Fragment {
    private OnFragmentInteractionListener mListener;
    private static final double LAT = 32.0833;
    private static final double LON = 34.8000;
    Place place;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private View view;
    private Marker marker;

    public static FragmentWithMap newInstance(Place place) {
        Bundle args = new Bundle();
        if (place != null) {
            args.putInt("id", place.getId());
            args.putString("name", place.getName());
            args.putString("address", place.getAddress());
            args.putFloat("lat", place.getLat());
            args.putFloat("lng", place.getLng());
        }
        FragmentWithMap fragment = new FragmentWithMap();
        fragment.setArguments(args);
        return fragment;
    }

    public FragmentWithMap() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().getString("name") != null ) {
            place = new Place(getArguments().getInt("id"), getArguments().getString("name"),
                    getArguments().getString("address"), getArguments().getFloat("lat"),
                    getArguments().getFloat("lng"));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_fragment_with_map, container, false);
        }
        setUpMapIfNeeded();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        android.support.v4.app.Fragment f = getFragmentManager()
                .findFragmentById(R.id.fragmnet_container_map);
        if (f != null) {
            try {
                getFragmentManager().beginTransaction().remove(f).commit();
            }
            catch (IllegalStateException ise) {
                Log.d("FragmentWithMap", "Already closed");
            }
        }

        ViewGroup parentViewGroup = (ViewGroup) view.getParent();
        if (parentViewGroup != null) {
            parentViewGroup.removeAllViews();
        }
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void showPlace(Place place) {
        setPlace(place);
        setUpMap();
    }

    public void setPlace(Place place) {
        this.place = place;
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
        public void onFragmentInteraction(Uri uri);
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            android.support.v4.app.Fragment mmm = getChildFragmentManager().findFragmentById(R.id.fragment_map2);
            mMap = ((SupportMapFragment) mmm).getMap();

            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        double lat = LAT;
        double lng = LON;
        String name = "";
        if (place != null) {
            lat = place.getLat();
            lng = place.getLng();
            name = place.getName();
        }
        if (marker != null) {
            marker.remove();
        }
        LatLng position = new LatLng(lat, lng);
        MarkerOptions markerOptions = new MarkerOptions().
                position(position).
                title(name);
        marker = mMap.addMarker(markerOptions);
        mMap.setMyLocationEnabled(true);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(position, 15);
        mMap.animateCamera(cameraUpdate);
    }

}
