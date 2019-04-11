package paublanes.travelnet;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity
        implements View.OnClickListener, RouteAdapter.ItemClicked {

    ArrayList<Route> routes;
    FloatingActionButton fab_add;

    ProfileRouteListFragment listFrag;

    final static String K_ROUTE_NAME = "name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //Create array of predefined routes
        routes = new ArrayList<>();
        /*if(Build.VERSION.SDK_INT >= 26){

        }*/

        //View references
        findViewById(R.id.fab_add).setOnClickListener(this);

        listFrag = (ProfileRouteListFragment) getSupportFragmentManager().findFragmentById(R.id.listFrag);
    }

    @Override
    public void OnTap(int index) {
        Intent i = new Intent(ProfileActivity.this, RouteDetailActivity.class);
        i.putExtra(K_ROUTE_NAME, routes.get(index));
        startActivity(i);
    }

    public ArrayList<Route> getRoutes() {
        return routes;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){
            if (requestCode == AddRouteActivity.ADDROUTE_KEY){
                Route route = (Route)data.getSerializableExtra(AddRouteActivity.NEW_ROUTE_KEY);
                routes.add(route);
                listFrag.myAdapter.notifyDataSetChanged();
            }
        }
    }

    void openAddPopup() {
        startActivityForResult(new Intent(ProfileActivity.this, AddRouteActivity.class), AddRouteActivity.ADDROUTE_KEY);
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
