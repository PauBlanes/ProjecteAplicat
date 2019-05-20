package paublanes.travelnet;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;


public class FirebaseManager {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference storageReference;

    //KEYS and TAGS
    private String ROUTES_COLL_PATH;
    private final String USERS_C_NAME = "Users";
    private final String ROUTES_C_NAME = "Routes";
    private final String TAG = "Firebase Manager";
    private final String K_NAME = "name";
    private final String K_UNIQUENAME = "unique_name";

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
        this.storageReference = FirebaseStorage.getInstance().getReference();

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
    void addNameAndUniqueNameIfNotTaken(String name, String uniqueName, Consumer<Boolean> updateUI) {
        //1. Get collection reference
        CollectionReference cRef = db.collection(USERS_C_NAME);

        //2.Query for this id
        Query query = cRef.whereEqualTo(K_UNIQUENAME, uniqueName);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.d(TAG, "Could not query for unique name");
                }
                else{
                    //Name is taken
                    if (task.getResult().size() > 0) {
                        Log.d(TAG, "Unique name is not unique");
                        updateUI.accept(false);
                    }
                    //Name is not taken
                    else{
                        //1. Get Document Reference
                        final DocumentReference docRef = db.collection(USERS_C_NAME).document(getUser().getUid());

                        //2. Generate data to add
                        Map<String, Object> data = new HashMap<>();
                        data.put(K_NAME, name);
                        data.put(K_UNIQUENAME, uniqueName);

                        //3. Add data
                        docRef.set(data, SetOptions.merge())
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            updateUI.accept(true);
                                        }
                                    }
                                });
                    }
                }
            }
        });
    }

    //Auth
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

            //Modificar path per si venim d'una altra conta
            ROUTES_COLL_PATH = USERS_C_NAME + "/" + getUser().getUid() + "/" + ROUTES_C_NAME;

        }else{
            Log.e(TAG, "User is null, couldn't create user document");
        }
    }
    FirebaseUser getUser() {
        return mAuth.getCurrentUser();
    }
    void isNewUser(Consumer<Boolean> completionHandler) {
        if(getUser() != null) {
            DocumentReference docRef = db.document(USERS_C_NAME + "/" + getUser().getUid());
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d(TAG, "Document exists!");
                            completionHandler.accept(false);
                        } else {
                            Log.d(TAG, "Document does not exist!");
                            completionHandler.accept(true);
                        }
                    } else {
                        Log.d(TAG, "Failed with: ", task.getException());
                    }
                }
            });
        }else{
            Log.e(TAG, "User is null, couldn't create user document");
        }
    }
    void hasUsername(Consumer<Boolean> completionHandler) {
        if(getUser() != null) {
            DocumentReference docRef = db.document(USERS_C_NAME + "/" + getUser().getUid());
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d(TAG, "Document exists!");
                            Map<String, Object> data = document.getData();
                            if (data.containsKey(K_UNIQUENAME)){
                                completionHandler.accept(true);
                            }else{
                                completionHandler.accept(false);
                            }
                        } else {
                            Log.d(TAG, "Document does not exist!");
                            completionHandler.accept(false);
                        }
                    } else {
                        Log.d(TAG, "Failed with: ", task.getException());
                    }
                }
            });
        }else{
            Log.e(TAG, "User is null, couldn't create user document");
        }
    }

    //Storage
    void uploadImage (Uri filePath, Consumer<String> c) {
        if (filePath != null) {
            final StorageReference ref = storageReference.child(getUser().getUid()+"/"+UUID.randomUUID().toString());
            UploadTask uploadTask = ref.putFile(filePath);

            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    // Continue with the task to get the download URL

                    return ref.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        c.accept(downloadUri.toString());
                    } else {
                        // Handle failures
                        // ...
                    }
                }
            });

        }else{
            Log.e(TAG, "Image path is null");
        }
    }
}
