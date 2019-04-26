package paublanes.travelnet;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

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

    //ACTIVITY LIFE CYCLE
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        tryLogin();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //Create array of predefined routes
        routes = new ArrayList<>();

        //View references
        findViewById(R.id.fab_add).setOnClickListener(this);
        findViewById(R.id.btn_signout).setOnClickListener(this);

        //Fragment references
        listFrag = (ProfileRouteListFragment) getSupportFragmentManager().findFragmentById(R.id.listFrag);
        mapFrag = (ProfileMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFrag);

        db.collection("Routes").addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {

                for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                    Toast.makeText(ProfileActivity.this, String.valueOf(dc.getType()), Toast.LENGTH_SHORT).show();
                }

                downloadFirebaseRoutes();
                /*for (QueryDocumentSnapshot document: queryDocumentSnapshots) {
                    Toast.makeText(ProfileActivity.this, "changes", Toast.LENGTH_SHORT).show();
                }*/
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();


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

                //Add to firebase
                //FirebaseManager.getInstance().addFirebaseRoute(route);

                //Update Locally
                routes.add(route);
                //updateUI();

                addFirebaseRoute(route);
            }
            else if (requestCode == Keys.K_ROUTE_DETAIL) {
                Route route = (Route)data.getSerializableExtra(Keys.SELECTED_ROUTE);

                /*FirebaseManager.getInstance().updateFirebaseRoute(route);
                updateUI();*/

                updateFirebaseRoute(route);
            }
            else if (requestCode == Keys.K_SIGN_IN) {
                Log.d("SIGN IN", "OK");
            }
        }
    }

    //FIREBASE
    void updateFirebaseRoute (final Route route) {

        DocumentReference docRef = db.document("Routes/"+route.getID());
        docRef.set(route, SetOptions.merge());

        updateUI();

    }
    void addFirebaseRoute(Route route) {

        final String routeID = UUID.randomUUID().toString();
        route.setID(routeID);

        final DocumentReference docRef = db.document("Routes/"+routeID);
        docRef.set(route).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(activityTAG, "DocumentSnapshot added with ID: " + docRef.getId());

                //Add listener
                /*docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {

                        if (documentSnapshot.exists()){
                            Toast.makeText(ProfileActivity.this, "Document changed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });*/
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(activityTAG, "Error adding document", e);
            }
        });



        /*db.collection("Routes")
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
                });*/
    }
    void downloadFirebaseRoutes() {

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
    void tryLogin() {

        //Si no estem loguejats
        if (FirebaseManager.getInstance().isUserNull()) {
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
    }

    //TAP EVENTS
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
    public void OnTap(int index) {
        selectedRoute = index;

        Intent i = new Intent(ProfileActivity.this, RouteDetailActivity.class);
        i.putExtra(Keys.SELECTED_ROUTE, routes.get(selectedRoute));
        startActivityForResult(i, Keys.K_ROUTE_DETAIL);
    }
    void openAddPopup() {
        startActivityForResult(new Intent(ProfileActivity.this, AddRouteActivity.class), Keys.K_ADD_ROUTE);
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
