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

    private ArrayList<MoneyInfo> moneyInfos;

    private ArrayList<String> imageUrls;

    public Route(String name, Calendar startDate) {
        this.name = name;
        this.startDate = startDate;

        locations = new ArrayList<>();
    }
    public Route(String name, Calendar startDate, RoutePoint rp) {
        this.name = name;
        this.startDate = startDate;

        locations = new ArrayList<>();
        addLocation(rp);

        moneyInfos = new ArrayList<>();
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

    //dates
    public void setEndDate(Calendar endDate) {
        this.endDate = endDate;
    }
    public String getStartDateString() {
        if (startDate == null){
            return "";
        }

        return DateFormat.format("dd/MM/yyyy", startDate).toString();
    }
    public String getEndDateString() {
        if (endDate == null){
            return "";
        }

        return DateFormat.format("dd/MM/yyyy", endDate).toString();
    }

    //Money
    public void addMoneyInfo(MoneyInfo mI) {moneyInfos.add(mI);}
    public ArrayList<MoneyInfo> getMoneyInfo() {return moneyInfos;}
}
