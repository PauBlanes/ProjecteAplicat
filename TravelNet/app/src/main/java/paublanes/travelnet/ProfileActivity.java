package paublanes.travelnet;

import android.content.Intent;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener, RouteAdapter.ItemClicked {

    ArrayList<Route> routes;
    FloatingActionButton fab_add;

    ProfileRouteListFragment listFrag;

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
        startActivity(new Intent(ProfileActivity.this, RouteDetailActivity.class));
    }

    public ArrayList<Route> getRoutes() {
        return routes;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AddRouteActivity.ACTIVITY_KEY) {
            Route route = (Route)data.getSerializableExtra(AddRouteActivity.NEW_ROUTE_KEY);
            routes.add(route);
            listFrag.myAdapter.notifyDataSetChanged();
        }
    }

    void openAddPopup() {
        startActivityForResult(new Intent(ProfileActivity.this, AddRouteActivity.class), AddRouteActivity.ACTIVITY_KEY);
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
