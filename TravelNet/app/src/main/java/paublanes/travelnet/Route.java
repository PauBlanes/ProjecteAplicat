package paublanes.travelnet;

import android.net.Uri;
import android.text.format.DateFormat;
import android.util.Log;

import com.google.firebase.firestore.Exclude;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;

public class Route implements Serializable {

    private String ownerID;
    private int listOrder;
    private String ID;

    private String name;
    private String profileImageUrl;
    private Calendar startDate, endDate;
    private String startDateString, endDateString;

    private ArrayList<RoutePoint> locations;

    private ArrayList<MoneyInfo> moneyInfo;

    private ArrayList<String> imageUrls;

    public Route(){}
    public Route(String name, Calendar startDate, RoutePoint rp) {
        this.name = name;
        this.startDate = startDate;
        this.startDateString = dateToString(startDate);

        locations = new ArrayList<>();
        addLocation(rp);

        moneyInfo = new ArrayList<>();

        imageUrls = new ArrayList<>();
    }

    //Owner and order
    public void setOwnerID(String ownerID) {
        this.ownerID = ownerID;
    }
    public void setListOrder(int listOrder) {
        this.listOrder = listOrder;
    }
    public String getOwnerID() {
        return ownerID;
    }
    public int getListOrder() {
        return listOrder;
    }

    //name
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    //Route Points
    public ArrayList<RoutePoint> getLocations() {return locations;}
    public RoutePoint getRoutePoint(int index) {
        return locations.get(index);
    }
    public void addLocation (RoutePoint rP) {
        locations.add(rP);
    }
    public void setLocations(ArrayList<RoutePoint> locations) {
        this.locations = locations;
    }
    public void deleteLocation(int index) {
        locations.remove(index);
    }

    //dates
    @Exclude
    public void setEndDate(Calendar endDate) {
        this.endDate = endDate;
        this.endDateString = dateToString(endDate);
    }
    public String getStartDateString() {
        if (startDate == null){
            return "";
        }
        return dateToString(startDate);
    }
    public String getEndDateString() {
        if (endDate == null){
            return "";
        }

        return dateToString(endDate);
    }
    @Exclude
    public Calendar getStartDate() {
        return startDate;
    }
    @Exclude
    public void setStartDate(Calendar startDate) {
        this.startDate = startDate;
    }
    @Exclude
    public Calendar getEndDate() {
        return endDate;
    }
    @Exclude
    public String dateToString(Calendar date){
        return DateFormat.format("dd/MM/yyyy", date).toString();
    }

    //Money
    public void addMoneyInfo(MoneyInfo mI) {
        moneyInfo.add(mI);}
    public ArrayList<MoneyInfo> getMoneyInfo() {return moneyInfo;}
    public void setMoneyInfo(ArrayList<MoneyInfo> moneyInfo) {
        this.moneyInfo = moneyInfo;
    }
    public void deleteMoneyCategory(int index) {
        moneyInfo.remove(index);
    }

    //Profile image
    public String getProfileImageUrl() {
        return profileImageUrl;
    }
    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    //ID
    public String getID() {
        return ID;
    }
    public void setID(String ID) {
        this.ID = ID;
    }

    //Images
    public ArrayList<String> getImageUrls() {
        return imageUrls;
    }
    public void setImageUrls(ArrayList<String> imageUrls) {
        this.imageUrls = imageUrls;
    }
    public void addImageUrl(String imageUrl) {imageUrls.add(imageUrl);}
    void deleteImage(int index) {
        imageUrls.remove(index);
    }
}
