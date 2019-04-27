package paublanes.travelnet;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class FirebaseManager {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    //KEYS and TAGS
    private String ROUTES_COLL_PATH;
    private final String USERS_C_NAME = "Users";
    private final String ROUTES_C_NAME = "Routes";
    private final String TAG = "Firebase Manager";

    //Constructors
    private static FirebaseManager instance;
    public synchronized static FirebaseManager getInstance() { //pq no creiin dos a la vegada si dos classes ho cridessin

        if (instance == null) {
            instance = new FirebaseManager();
        }
        return instance;
    }
    private FirebaseManager(){

        //Get references to auth and database
        this.mAuth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();

        //Get path to routes
        if(getUser() != null) {
            ROUTES_COLL_PATH = USERS_C_NAME + "/" + getUser().getUid() + "/" + ROUTES_C_NAME;

        }else{
            Log.e(TAG, "User is null, coudn't get path");
        }
    }

    //Firestore
    void initLister(final ArrayList<Route> routes, final Runnable updateUI) {
        CollectionReference cRef = db.collection(ROUTES_COLL_PATH);

        cRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if (getUser() != null) {
                    for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                        Log.d(TAG, String.valueOf(dc.getType()));
                    }

                    downloadRoutes(routes, updateUI);
                }
            }
        });
    }
    void updateRoute (final Route route) {
        DocumentReference docRef = db.collection(ROUTES_COLL_PATH).document(route.getID());
        docRef.set(route, SetOptions.merge());
    }
    void addRoute(Route route) {

        //1. Crear id
        final String routeID = UUID.randomUUID().toString();
        route.setID(routeID);

        //2. Crear y subir documento
        final DocumentReference docRef = db.collection(ROUTES_COLL_PATH).document(routeID);
        docRef.set(route).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "DocumentSnapshot added with ID: " + docRef.getId());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Error adding document", e);
            }
        });
    }
    void downloadRoutes(final ArrayList<Route> routes, final Runnable updateUI) {

        //Agafar totes les rutes del meu usuari
        CollectionReference cRef = db.collection(ROUTES_COLL_PATH);
        //Query query = cRef.whereEqualTo("ownerID", mAuth.getCurrentUser().getUid());

        /*query*/cRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    routes.clear();
                    for (QueryDocumentSnapshot document: task.getResult()){

                        Route route = document.toObject(Route.class);
                        routes.add(route);
                    }

                    //Funcio updateUI
                    updateUI.run();
                    //listFrag.myAdapter.notifyDataSetChanged();
                    //mapFrag.showRoutePoints();
                }else{
                    Log.e(TAG, "Query failed");
                }
            }
        });
    }

    //Auth
    FirebaseUser getUser() {
        return mAuth.getCurrentUser();
    }
    void logOut(Context context, Runnable tryLogin) {
        AuthUI.getInstance()
                .signOut(context)
                .addOnCompleteListener(new OnCompleteListener<Void>() { //pq et deixi tornar a triar conta
                    public void onComplete(@NonNull Task<Void> task) {
                        // user is now signed out
                        tryLogin.run();
                    }
                });
    }
    Intent getSignInActivity() {
        return AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(Arrays.asList(
                        new AuthUI.IdpConfig.GoogleBuilder().build(),
                        /*new AuthUI.IdpConfig.TwitterBuilder().build(),*/
                        new AuthUI.IdpConfig.PhoneBuilder().build(),
                        new AuthUI.IdpConfig.EmailBuilder().build()))
                .setIsSmartLockEnabled(false)
                .build();
    }
    void createUser() {
        if(getUser() != null) {
            DocumentReference documentReference = db.document(USERS_C_NAME + "/" + getUser().getUid());
            Map<String, String> userInfo = new HashMap<String, String>();
            userInfo.put("id", getUser().getUid());

            documentReference.set(userInfo, SetOptions.merge());

            //Modificar path per si canviem de conta
            ROUTES_COLL_PATH = USERS_C_NAME + "/" + getUser().getUid() + "/" + ROUTES_C_NAME;

        }else{
            Log.e(TAG, "User is null, couldn't create user document");
        }
    }


}
