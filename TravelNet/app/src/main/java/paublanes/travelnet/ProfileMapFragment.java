package paublanes.travelnet;


import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


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

            Marker marker1 = mMap.addMarker(new MarkerOptions().position(route.getLocations().get(0).getCoordinates()).title("Start"));
            marker1.setIcon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("route_point", 250, 250)));

            for (int i = 0; i < route.getLocations().size()-1; i++) {
                //Make curved line
                showCurvedPolyline(
                        route.getRoutePoint(i).getCoordinates(),
                        route.getRoutePoint(i+1).getCoordinates());
            }
        }
    }

    private void showCurvedPolyline (LatLng latLng1, LatLng latLng2) {


        Marker marker1 = mMap.addMarker(new MarkerOptions().position(latLng1).title("Start"));
        marker1.setIcon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("route_point", 250, 250)));
        Marker marker2 = mMap.addMarker(new MarkerOptions().position(latLng2).title("End"));
        marker2.setIcon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("route_point", 250, 250)));

        List<PatternItem> pattern = Arrays.<PatternItem>asList(new Dash(30), new Gap(20));
        PolylineOptions popt = new PolylineOptions().add(latLng1).add(latLng2)
                .width(5).color(Color.BLACK).pattern(pattern)
                .geodesic(true);
        mMap.addPolyline(popt);

        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        builder.include(marker1.getPosition());
        builder.include(marker2.getPosition());
    }
    public Bitmap resizeMapIcons(String iconName,int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier(iconName, "drawable", getActivity().getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
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

        showRoutePoints();
    }
}
