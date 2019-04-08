package paublanes.travelnet;

import android.text.format.DateFormat;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

public class Route implements Serializable {

    private String ownerID;

    private String name;
    private String profileImageUrl;
    private Calendar startDate, endDate;

    private ArrayList<RoutePoint> locations;

    private Map<String, Integer> money;

    private ArrayList<String> imageUrls;

    public Route(String name, Calendar startDate) {
        this.name = name;
        this.startDate = startDate;

        locations = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public Calendar getStartDate() {
        return startDate;
    }

    public void setStartDate(Calendar startDate) {
        this.startDate = startDate;
    }

    public Calendar getEndDate() {
        return endDate;
    }

    public void setEndDate(Calendar endDate) {
        this.endDate = endDate;
    }

    public ArrayList<RoutePoint> getLocations() {
        return locations;
    }

    public void addLocation (RoutePoint rP) {
        locations.add(rP);
    }

    public Map<String, Integer> getMoney() {
        return money;
    }

    public void setMoney(Map<String, Integer> money) {
        this.money = money;
    }

    public ArrayList<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(ArrayList<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public String getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(String ownerID) {
        this.ownerID = ownerID;
    }

    public String stringStartDate() {
        if (startDate == null){
            return "";
        }

        return DateFormat.format("dd/MM/yyyy", startDate).toString();
    }
    public String stringEndDate() {
        if (endDate == null){
            return "";
        }

        return DateFormat.format("dd/MM/yyyy", endDate).toString();
    }
}
