package paublanes.travelnet;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.Fade;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity
        implements View.OnClickListener, RouteAdapter.ItemFunctionalities {

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
    FloatingActionButton fab_add_route, fab_share;
    TextView tv_name, tv_unique_name;

    //Is my profile?
    boolean isMyProfile = true;

    //Firebase
    FirebaseManager firebaseManager;

    //Ads
    private RewardedAd rewardedAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //Create array of predefined routes
        routes = new ArrayList<>();

        //Get view references
        fab_add_route = findViewById(R.id.fab_add);
        fab_add_route.setOnClickListener(this);
        fab_share = findViewById(R.id.fab_share);
        fab_share.setOnClickListener(this);
        tv_name = findViewById(R.id.tv_name); tv_name.setText("");
        tv_unique_name = findViewById(R.id.tv_unique_name); tv_unique_name.setText("");

        //Get fragment references
        listFrag = (ProfileRouteListFragment) getSupportFragmentManager().findFragmentById(R.id.listFrag);
        mapFrag = (ProfileMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFrag);

        //Searchview
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initSearchView();

        //Firebase
        firebaseManager = FirebaseManager.getInstance();
        firebaseManager.initListener(routes,this::updateRoutesUI); //init routes listener
        firebaseManager.getUserInfoFromId(firebaseManager.getUser().getUid(), this::updateUserInfoUI); //only once because this info cannot be changed(at least now)

        setTitle(""); //perque en el style windowNoTitle no funciona

        //Ads
        rewardedAd = new RewardedAd(this,
                "ca-app-pub-3940256099942544/5224354917");

        RewardedAdLoadCallback adLoadCallback = new RewardedAdLoadCallback() {
            @Override
            public void onRewardedAdLoaded() {
                // Ad successfully loaded.
            }

            @Override
            public void onRewardedAdFailedToLoad(int errorCode) {
                // Ad failed to load.
            }
        };
        rewardedAd.loadAd(new AdRequest.Builder().build(), adLoadCallback);
    }
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseManager.getInstance().recieveDynamicLinks(this, getIntent(), this::receiveProfile);
    }

    //TAP EVENTS
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fab_add:
                openAddPopup();
                break;
            case R.id.fab_share:
                FirebaseManager.getInstance().generateShortDeepLink(this, this::shareProfile);
                break;
        }

    }
    @Override
    public void OnTap(int index) {
        selectedRoute = index;

        Intent i = new Intent(ProfileActivity.this, RouteDetailActivity.class);
        i.putExtra(Keys.SELECTED_ROUTE, routes.get(selectedRoute));
        i.putExtra(Keys.K_IS_MY_PROFILE, isMyProfile);
        startActivity(i);
    } //interface from RouteAdapter
    @Override
    public void routeDeleteMenu(View itemView, int routeIndex) {
        if (isMyProfile) {
            itemView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                @Override
                public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                    menu.add("delete").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            FirebaseManager.getInstance().deleteRoute(routes.get(routeIndex).getID());
                            return true;
                        }
                    });
                }
            });
        }
    }

    //ADD ROUTE
    void openAddPopup() {
        showAd();
    }

    //UTILS
    public ArrayList<Route> getRoutes() {
        return routes;
    }
    void updateRoutesUI() {
        listFrag.myAdapter.notifyDataSetChanged();
        mapFrag.showRoutePoints();

        if (!isMyProfile) {
            fab_add_route.hide();
            fab_share.hide();
        }else{
            fab_add_route.show();
            fab_share.show();
        }
    }
    void updateUserInfoUI(Map<String, Object> data) {
        tv_name.setText(data.get(Keys.K_NAME).toString());
        tv_unique_name.setText("@"+data.get(Keys.K_UNIQUENAME).toString());
    }

    //OPTIONS MENU
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);

        toolbar.setOverflowIcon(getDrawable(R.drawable.ic_3dot));

        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){
            case R.id.action_search:
                return true;
            case R.id.action_logout:
                Intent i = new Intent(ProfileActivity.this, BlankActivity.class);
                firebaseManager.logOut(this, () -> startActivity(i));
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

            isMyProfile = true;
            firebaseManager.initListener(routes, this::updateRoutesUI);
            firebaseManager.getUserInfoFromId(firebaseManager.getUser().getUid(), this::updateUserInfoUI);
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
                firebaseManager.getRoutesFromName(query, routes, ProfileActivity.this::updateRoutesUI,
                        ()->{isMyProfile = false;});
                firebaseManager.getUserInfoFromName(query, ProfileActivity.this::updateUserInfoUI);
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
                firebaseManager.getUsernames(ProfileActivity.this::updateSuggestions);
                fab_add_route.hide();
            }
            @Override
            public void onSearchViewClosed() {
                fab_add_route.show();
            }
        });

    }
    public void updateSuggestions (List<String> usernames) {

        String[] listToArray = new String[usernames.size()];
        listToArray = usernames.toArray(listToArray);

        searchView.setSuggestions(listToArray);
    }

    //SHARE
    void shareProfile(Uri shortLink) {
        Intent intent = new Intent();
        String msg = "Check out my profile: " + shortLink;
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, msg);
        intent.setType("text/plain");
        startActivity(Intent.createChooser(intent, "Share Profile")); //així no es guarda la opció triada
    }
    void receiveProfile(Uri shortLink) {

        isMyProfile = false;

        String id = shortLink.toString().replace("https://paublanes.travelnet/", "");
        FirebaseManager.getInstance().getRoutesFromId(id, routes, this::updateRoutesUI);
        FirebaseManager.getInstance().getUserInfoFromId(id, this::updateUserInfoUI);
    }

    void showAd() {
        if (rewardedAd.isLoaded()) {
            Activity activityContext = this;
            RewardedAdCallback adCallback = new RewardedAdCallback() {
                public void onRewardedAdOpened() {
                    // Ad opened.
                }

                public void onRewardedAdClosed() {
                    Intent i = new Intent(ProfileActivity.this, AddRouteActivity.class);
                    i.putExtra(Keys.ROUTE_ORDER, routes.size());
                    startActivity(i);
                }

                public void onUserEarnedReward(@NonNull RewardItem reward) {
                    Log.d("REWARD", "User earned reward");
                }

                public void onRewardedAdFailedToShow(int errorCode) {
                    // Ad failed to display
                }
            };
            rewardedAd.show(activityContext, adCallback);
        } else {
            Log.d("TAG", "The rewarded ad wasn't loaded yet.");
            Intent i = new Intent(ProfileActivity.this, AddRouteActivity.class);
            i.putExtra(Keys.ROUTE_ORDER, routes.size());
            startActivity(i);
        }
    }
}
