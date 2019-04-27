package paublanes.travelnet;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity
        implements View.OnClickListener, RouteAdapter.ItemClicked {

    //Data
    ArrayList<Route> routes;
    int selectedRoute = 0;

    //Fragments
    ProfileRouteListFragment listFrag;
    ProfileMapFragment mapFrag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //Create array of predefined routes
        routes = new ArrayList<>();

        //Get view references
        findViewById(R.id.fab_add).setOnClickListener(this);
        findViewById(R.id.btn_signout).setOnClickListener(this);

        //Get fragment references
        listFrag = (ProfileRouteListFragment) getSupportFragmentManager().findFragmentById(R.id.listFrag);
        mapFrag = (ProfileMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFrag);

        //Start firebase listener
        FirebaseManager.getInstance().initLister(routes, this::updateUI);
    }

    //TAP EVENTS
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fab_add:
                openAddPopup();
                break;
            case R.id.btn_signout:
                Intent i = new Intent(ProfileActivity.this, BlankActivity.class);
                FirebaseManager.getInstance().logOut(this, () -> startActivity(i));
                break;
        }

    }
    @Override
    public void OnTap(int index) {
        selectedRoute = index;

        Intent i = new Intent(ProfileActivity.this, RouteDetailActivity.class);
        i.putExtra(Keys.SELECTED_ROUTE, routes.get(selectedRoute));
        startActivity(i);
    }
    //ADD ROUTE
    void openAddPopup() {
        Intent i = new Intent(ProfileActivity.this, AddRouteActivity.class);
        i.putExtra(Keys.ROUTE_ORDER, routes.size());
        startActivity(i);
    }

    //UTILS
    public ArrayList<Route> getRoutes() {
        return routes;
    }
    void updateUI() {
        listFrag.myAdapter.notifyDataSetChanged();
        mapFrag.showRoutePoints();
    }
}
