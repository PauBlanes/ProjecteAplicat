package paublanes.travelnet;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;
import java.util.Calendar;

public class ProfileActivity extends AppCompatActivity
        implements View.OnClickListener, RouteAdapter.ItemClicked {

    ArrayList<Route> routes;
    int selectedRoute = 0;

    FloatingActionButton fab_add;

    ProfileRouteListFragment listFrag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //Create array of predefined routes
        routes = new ArrayList<>();

        //View references
        findViewById(R.id.fab_add).setOnClickListener(this);

        //DEBUG
        RoutePoint testRoutePoint = new RoutePoint(41.390205, 2.154007, "Barcelona");
        Calendar testStartDate = Calendar.getInstance();
        testStartDate.set(2019, 2, 5);
        Route testRoute = new Route("Ruta 1", testStartDate, testRoutePoint);
        testRoute.addMoneyInfo(new MoneyInfo("Transport", 0));
        testRoute.addMoneyInfo(new MoneyInfo("Housing", 0));
        testRoute.addMoneyInfo(new MoneyInfo("Cash", 0));
        routes.add(testRoute);
        //END DEBUG

        listFrag = (ProfileRouteListFragment) getSupportFragmentManager().findFragmentById(R.id.listFrag);
    }

    @Override
    public void OnTap(int index) {
        selectedRoute = index;

        Intent i = new Intent(ProfileActivity.this, RouteDetailActivity.class);
        i.putExtra(Keys.SELECTED_ROUTE, routes.get(selectedRoute));
        startActivityForResult(i, Keys.K_ROUTE_DETAIL);
    }

    public ArrayList<Route> getRoutes() {
        return routes;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){
            if (requestCode == Keys.K_ADD_ROUTE){
                Route route = (Route)data.getSerializableExtra(Keys.NEW_ROUTE);
                routes.add(route);
                listFrag.myAdapter.notifyDataSetChanged();
            }
            else if (requestCode == Keys.K_ROUTE_DETAIL) {
                Route route = (Route)data.getSerializableExtra(Keys.SELECTED_ROUTE);
                routes.set(selectedRoute,route);
            }
        }
    }

    void openAddPopup() {
        startActivityForResult(new Intent(ProfileActivity.this, AddRouteActivity.class), Keys.K_ADD_ROUTE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fab_add:
                openAddPopup();
                break;
        }

    }
}
