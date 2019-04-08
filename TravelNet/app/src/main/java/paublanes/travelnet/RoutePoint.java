package paublanes.travelnet;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.Date;

public class RoutePoint implements Serializable {
    private Double latitude; //pq no es serializable el LatLang
    private Double longitude;

    private String placeName;
    private Date startDate, endDate;

    public RoutePoint(Double latitude, Double longitude, String placeName) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.placeName = placeName;

    }
}
