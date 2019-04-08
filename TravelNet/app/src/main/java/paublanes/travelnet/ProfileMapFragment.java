package paublanes.travelnet;


import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileMapFragment extends Fragment {


    public ProfileMapFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_profile_map, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.frg);  //use SuppoprtMapFragment for using in fragment instead of activity  MapFragment = activity   SupportMapFragment = fragment
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {

                try {
                    // Customise the styling of the base map using a JSON object defined in a raw resource file.
                    boolean success = mMap.setMapStyle(
                            MapStyleOptions.loadRawResourceStyle(
                                    getContext(), R.raw.my_map_style));

                    if (!success) {
                        Log.e("MapActivity", "Style parsing failed.");
                    }
                } catch (Resources.NotFoundException e) {
                    Log.e("MapActivity", "Can't find style. Error: ", e);
                }

                // Posem el mapa a prop de barcelona.
                //To DO -> Posar a prop de la ubicació del telefon si està activada
                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(41.3, 2.18)));
            }
        });

        return rootView;
    }

}
