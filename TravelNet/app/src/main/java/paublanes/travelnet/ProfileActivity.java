package paublanes.travelnet;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity
        implements View.OnClickListener, RouteAdapter.ItemClicked {

    //Data
    ArrayList<Route> routes;
    int selectedRoute = 0;

    //Fragments
    ProfileRouteListFragment listFrag;
    ProfileMapFragment mapFrag;

    //SEARCH
    MaterialSearchView searchView;
    Toolbar toolbar;

    //Views
    FloatingActionButton btn_add_route;

    //Is my profile?
    boolean isMyProfile = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //Create array of predefined routes
        routes = new ArrayList<>();

        //Get view references
        btn_add_route = findViewById(R.id.fab_add);
        btn_add_route.setOnClickListener(this);

        //Get fragment references
        listFrag = (ProfileRouteListFragment) getSupportFragmentManager().findFragmentById(R.id.listFrag);
        mapFrag = (ProfileMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFrag);

        //Searchview
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initSearchView();

        //Firebase
        FirebaseManager.getInstance().initListener(routes,this::updateUI);
    }

    //TAP EVENTS
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fab_add:
                openAddPopup();
                break;
        }

    }
    @Override
    public void OnTap(int index) {
        selectedRoute = index;

        Intent i = new Intent(ProfileActivity.this, RouteDetailActivity.class);
        i.putExtra(Keys.SELECTED_ROUTE, routes.get(selectedRoute));
        startActivity(i);
    } //interface from RouteAdapter

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

        if (!isMyProfile) {
            btn_add_route.setVisibility(View.GONE);
        }else{
            btn_add_route.setVisibility(View.VISIBLE);
        }
    }

    //OPTIONS MENU
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);

        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){
            case R.id.action_search:
                return true;
            case R.id.action_logout:
                Intent i = new Intent(ProfileActivity.this, BlankActivity.class);
                FirebaseManager.getInstance().logOut(this, () -> startActivity(i));
                break;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed() {
        if (searchView.isSearchOpen()) {
            searchView.closeSearch();
        }
        else if(!isMyProfile) {
            FirebaseManager.getInstance().initListener(routes, this::updateUI);
        }
        else{
            super.onBackPressed();
        }
    }

    //SEARCH VIEW
    public void initSearchView() {
        searchView = findViewById(R.id.search_view);
        searchView.setEllipsize(true);
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                FirebaseManager.getInstance().showRoutesOf(query, routes, ProfileActivity.this::updateUI,
                        ()->{isMyProfile = false;});
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //TO DO -> considerar qualsevol coincidencia en comptes de només lletra a lletra linealment
                return false;
            }
        });
        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                FirebaseManager.getInstance().getUsernames(ProfileActivity.this::updateSuggestions);
                btn_add_route.setVisibility(View.GONE);
            }
            @Override
            public void onSearchViewClosed() {
                btn_add_route.setVisibility(View.VISIBLE);
            }
        });

    }
    public void updateSuggestions (List<String> usernames) {

        String[] listToArray = new String[usernames.size()];
        listToArray = usernames.toArray(listToArray);

        searchView.setSuggestions(listToArray);
    }
}
