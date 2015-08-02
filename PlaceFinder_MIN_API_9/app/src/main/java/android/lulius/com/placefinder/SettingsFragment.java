package android.lulius.com.placefinder;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SettingsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends android.support.v4.app.Fragment implements View.OnClickListener {
    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }
    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }
    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_settings, container, false);
        RadioGroup radioGroup = (RadioGroup) v.findViewById(R.id.radio_group);

        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        String currentDistanceUnits = sharedPref.getString("distanceUnits", "kilometers");

        if(currentDistanceUnits.equals("miles")){
              radioGroup.check(R.id.radio_ml);
        }
        else{
            radioGroup.check(R.id.radio_kl);
        }

        Button buttonOk = (Button) v.findViewById(R.id.buttonOk);
        Button buttonCancel = (Button) v.findViewById(R.id.buttonCancel);
        Button buttonDeleteFavorites = (Button) v.findViewById(R.id.removeFavorites);

        buttonOk.setOnClickListener(this);
        buttonCancel.setOnClickListener(this);
        buttonDeleteFavorites.setOnClickListener(this);

        return v;
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

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonOk: {
                savePreferences();
            }
            case R.id.buttonCancel: {
                closePreferences();
                break;
            }
            case R.id.removeFavorites: {
                deleteFavorites();
                break;
            }
        }
    }

    private void deleteFavorites() {
        getActivity().getContentResolver().delete(PlacesContract.Places.CONTENT_URI, PlacesContract.Places.FAV+"=1", new String[]{});
        Toast.makeText(getActivity(), "Favorites deleted", Toast.LENGTH_LONG).show();
    }

    private void closePreferences() {
        getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    private void savePreferences() {
        RadioGroup radioGroup = (RadioGroup) getView().findViewById(R.id.radio_group);
        int selectedId = radioGroup.getCheckedRadioButtonId();
        RadioButton radioButton = (RadioButton) getView().findViewById(selectedId);

        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("distanceUnits", (String) radioButton.getText());
        editor.commit();
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

}
