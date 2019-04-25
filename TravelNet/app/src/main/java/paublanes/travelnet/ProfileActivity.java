package paublanes.travelnet;

import android.content.Intent;
import java.util.Calendar;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.Arrays;

public class ProfileActivity extends AppCompatActivity
        implements View.OnClickListener, RouteAdapter.ItemClicked {

    //Data
    ArrayList<Route> routes;
    int selectedRoute = 0;

    //Views
    FloatingActionButton fab_add;

    //Fragments
    ProfileRouteListFragment listFrag;
    ProfileMapFragment mapFrag;

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    //TAG
    String activityTAG = "Activitat perfil";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Firebase
        mAuth = FirebaseAuth.getInstance();
        tryLogin();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //Create array of predefined routes
        routes = new ArrayList<Route>();

        //View references
        findViewById(R.id.fab_add).setOnClickListener(this);
        findViewById(R.id.btn_signout).setOnClickListener(this);

        //Fragment references
        listFrag = (ProfileRouteListFragment) getSupportFragmentManager().findFragmentById(R.id.listFrag);
        mapFrag = (ProfileMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFrag);

        //DEBUG
        /*RoutePoint testRoutePoint = new RoutePoint(41.390205, 2.154007, "Barcelona");
        Calendar testStartDate = Calendar.getInstance();
        testStartDate.set(2019, 2, 5);
        Route testRoute = new Route("Ruta 1", testStartDate, testRoutePoint);
        testRoute.addMoneyInfo(new MoneyInfo("Transport", 0));
        testRoute.addMoneyInfo(new MoneyInfo("Housing", 0));
        testRoute.addMoneyInfo(new MoneyInfo("Cash", 0));
        if (mAuth.getCurrentUser() != null) {
            testRoute.setOwnerID(mAuth.getCurrentUser().getUid());
        }else {
            Log.e(activityTAG, "User is null");
        }
        testRoute.setListOrder(routes.size());
        addRouteAndSaveIt(testRoute);*/
        //END DEBUG

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
                if (mAuth.getCurrentUser() != null) {
                    route.setOwnerID(mAuth.getCurrentUser().getUid());
                }else {
                    Log.e(activityTAG, "User is null");
                }
                route.setListOrder(routes.size());

                addRouteAndSaveIt(route);
            }
            else if (requestCode == Keys.K_ROUTE_DETAIL) {
                Route route = (Route)data.getSerializableExtra(Keys.SELECTED_ROUTE);

                updateFirebaseRoute(route);
            }
            else if (requestCode == Keys.K_SIGN_IN) {
                Log.d("SIGN IN", "OK");
            }
        }
    }

    void updateFirebaseRoute (final Route route) {

        //Agafar totes les rutes del meu usuari
        Query query = db.collection("Routes")
                .whereEqualTo("ownerID", route.getOwnerID())
                .whereEqualTo("listOrder", route.getListOrder());

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){

                    for (QueryDocumentSnapshot document: task.getResult()){

                        db.collection("Routes").document(document.getId())
                                .set(route, SetOptions.merge());

                    }
                    listFrag.myAdapter.notifyDataSetChanged();
                    mapFrag.showRoutePoints();

                    //getFireBaseData();
                }else{
                    Log.e(activityTAG, "Query failed");
                }
            }
        });
    }
    void addRouteAndSaveIt(Route route) {

        //Add route locally
        routes.add(route);
        if (listFrag.myAdapter != null) {
            listFrag.myAdapter.notifyDataSetChanged();
        }

        //Add route to firebase
        db.collection("Routes")
                .add(route)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(activityTAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(activityTAG, "Error adding document", e);
                    }
                });
    }

    void getFireBaseData() {

        routes.clear();

        //Agafar totes les rutes del meu usuari
        CollectionReference cRef = db.collection("Routes");
        Query query = cRef.whereEqualTo("ownerID", mAuth.getCurrentUser().getUid());

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){

                    for (QueryDocumentSnapshot document: task.getResult()){

                        Route route = document.toObject(Route.class);
                        Log.d(activityTAG, "" + route.getMoneyInfo().size());
                        routes.add(route);
                    }
                    listFrag.myAdapter.notifyDataSetChanged();
                    mapFrag.showRoutePoints();
                }else{
                    Log.e(activityTAG, "Query failed");
                }
            }
        });

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
            case R.id.btn_signout:
                AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() { //pq et deixi tornar a triar conta
                    public void onComplete(@NonNull Task<Void> task) {
                        // user is now signed out
                        tryLogin();
                    }
                });
                break;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        getFireBaseData();
    }

    private void tryLogin() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        //Si no estem loguejats
        if (currentUser == null) {
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(Arrays.asList(
                                    new AuthUI.IdpConfig.GoogleBuilder().build(),
                                    /*new AuthUI.IdpConfig.TwitterBuilder().build(),*/
                                    new AuthUI.IdpConfig.PhoneBuilder().build(),
                                    new AuthUI.IdpConfig.EmailBuilder().build()))
                            .setIsSmartLockEnabled(false)
                            .build(),
                    Keys.K_SIGN_IN);
        }
        else {
            //Cojemos referencia a la base de datos
            db = FirebaseFirestore.getInstance();
        }
    }
}
