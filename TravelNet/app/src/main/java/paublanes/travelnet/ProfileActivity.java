package paublanes.travelnet;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.ArrayList;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //Create array of predefined routes
        routes = new ArrayList<>();

        //Get view references
        findViewById(R.id.fab_add).setOnClickListener(this);

        //Get fragment references
        listFrag = (ProfileRouteListFragment) getSupportFragmentManager().findFragmentById(R.id.listFrag);
        mapFrag = (ProfileMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFrag);

        //Start firebase listener
        FirebaseManager.getInstance().initLister(routes, this::updateUI);

        //Searchview
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initSearchView();
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
    }

    //ACTION BAR
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
    private void initSearchView() {
        searchView = findViewById(R.id.search_view);
        searchView.setEllipsize(true);
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Toast.makeText(ProfileActivity.this, query, Toast.LENGTH_SHORT).show();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                searchView.setSuggestions(getResources().getStringArray(R.array.proves)); //si ho poso abans es queda la pantalla blanca
            }
            @Override
            public void onSearchViewClosed() {
                //Do some magic
            }
        });
    }
    @Override
    public void onBackPressed() {
        if (searchView.isSearchOpen()) {
            searchView.closeSearch();
        }
        else{
            super.onBackPressed();
        }

    }
}
