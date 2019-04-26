package paublanes.travelnet;

import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.UUID;

public class FirebaseManager {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    //KEYS and TAGS
    private final String K_ROUTE_COLLECTION = "Routes";
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
        this.mAuth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
    }

    //Methods
    void updateFirebaseRoute (final Route route) {
        DocumentReference docRef = db.document(K_ROUTE_COLLECTION + "/" + route.getID());
        docRef.set(route, SetOptions.merge());
    }
    void addFirebaseRoute(Route route) {

        final String routeID = UUID.randomUUID().toString();
        route.setID(routeID);

        final DocumentReference docRef = db.document( K_ROUTE_COLLECTION + "/" + routeID);
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
    Query getFirebaseRoutes() {

        //Agafar totes les rutes del meu usuari
        CollectionReference cRef = db.collection(K_ROUTE_COLLECTION);
        return cRef.whereEqualTo("ownerID", mAuth.getCurrentUser().getUid());
    }
    boolean isUserNull() {
        return mAuth.getCurrentUser() == null;
    }

}
