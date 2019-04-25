package paublanes.travelnet;


import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileMapFragment extends Fragment implements OnMapReadyCallback {

    GoogleMap mMap;


    public ProfileMapFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_profile_map, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.frg);  //use SuppoprtMapFragment for using in fragment instead of activity  MapFragment = activity   SupportMapFragment = fragment
        mapFragment.getMapAsync(this);

        return rootView;
    }

    public void showRoutePoints() {

        mMap.clear();

        for (Route route: ((ProfileActivity)this.getActivity()).getRoutes()) {
            
            PolylineOptions plOptions = new PolylineOptions()
                    .width(5)
                    .color(Color.BLACK);

            mMap.addMarker(new MarkerOptions()
                    .position(route.getRoutePoint(0).getCoordinates()));
            for (RoutePoint rp: route.getLocations()){
                plOptions.add(rp.getCoordinates());
            }
            mMap.addPolyline(plOptions);
        }
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

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
}
