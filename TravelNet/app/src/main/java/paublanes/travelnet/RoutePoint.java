package paublanes.travelnet;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.Date;

public class RoutePoint implements Serializable {
    private Double latitude; //pq no es serializable el LatLang
    private Double longitude;

    private String placeName;
    private int numNights;

    public RoutePoint(Double latitude, Double longitude, String placeName) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.placeName = placeName;
    }

    public String getPlaceName() {return placeName;}

    public int getNumNights() {
        return numNights;
    }
    public void setNumNights(int numNights) {
        this.numNights = numNights;
    }

    public LatLng getCoordinates() {return new LatLng(latitude, longitude);}
}
