package paublanes.travelnet;

import android.app.Activity;
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
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;


public class FirebaseManager {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference storageReference;

    //KEYS and TAGS
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
        this.storageReference = FirebaseStorage.getInstance().getReference();
    }

    //Routes
    void initListener(final ArrayList<Route> routes, final Runnable updateUI) {
        CollectionReference cRef = db.collection(getMyRoutesPath());

        cRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if (getUser() != null) {
                    for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                        Log.d(TAG, String.valueOf(dc.getType()));
                    }
                    getRoutesFromId(getUser().getUid(), routes, updateUI);
                }
            }
        });
    }
    void updateRoute (final Route route) {
        DocumentReference docRef = db.collection(getMyRoutesPath()).document(route.getID());
        docRef.set(route, SetOptions.merge());
    }
    void addRoute(Route route) {
        //1. Crear id
        final String routeID = UUID.randomUUID().toString();
        route.setID(routeID);

        //2. Crear y subir documento
        final DocumentReference docRef = db.collection(getMyRoutesPath()).document(routeID);
        docRef.set(route).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "DocumentSnapshot added with ID: " + docRef.getId());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Error adding document", e);
            }
        });
    }
    void getRoutesFromId(String id, final ArrayList<Route> routes, final Runnable updateUI) {

        //1.Reference to my routes collection
        CollectionReference cRef = db.collection(getRoutesPathFromId(id));
        downloadRoutes(cRef,routes, updateUI);
    }
    void getRoutesFromName(String nameToSearch, final ArrayList<Route> routes, final Runnable updateUI,
                           final Runnable completionHandler) {
        //1. Get Collection of users
        CollectionReference cRef = db.collection(USERS_C_NAME);

        //2. Find the one with the correct name
        Query query = cRef.whereEqualTo(Keys.K_UNIQUENAME, nameToSearch);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                    if (doc.exists()) {
                        Log.d(TAG, "Document with unique id exists");

                        //3. Download their routes
                        downloadRoutes(doc.getReference().collection(ROUTES_C_NAME), routes, updateUI);

                        //4. Callback
                        completionHandler.run();

                    }else{
                        Log.e(TAG, "Document with uique id does not exist");
                    }
                }else{
                    Log.e(TAG, "Query to find doc from unique_name failed");
                }
            }
        });
    }
    private void downloadRoutes(CollectionReference cRef, final ArrayList<Route> routes, final Runnable updateUI) {
        cRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    //3. Add routes
                    routes.clear();
                    for (QueryDocumentSnapshot document: task.getResult()){

                        Route route = document.toObject(Route.class);
                        routes.add(route);
                    }

                    //4. Update the UI
                    updateUI.run();

                }else{
                    Log.e(TAG, "Query failed");
                }
            }
        });
    }
    void deleteRoute(String routeId) {
        db.collection(getMyRoutesPath()).document(routeId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting document", e);
                    }
                });
    }

    //User info
    void getUserInfoFromName(String nameToSearch, final Consumer<Map<String,Object>> completionHandler){
        Query query = db.collection(USERS_C_NAME).whereEqualTo(Keys.K_UNIQUENAME, nameToSearch);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                    if (doc.exists()) {
                        Log.d(TAG, "Could get doc to download user info");
                        completionHandler.accept(doc.getData());
                    }else{
                        Log.e(TAG, "Could not get doc to download user info");
                    }
                }else{
                    Log.e(TAG, "Task to download userinfo failed");
                }
            }
        });
    }
    void getUserInfoFromId(String id, final Consumer<Map<String,Object>> completionHandler){
        DocumentReference docRef = db.collection(USERS_C_NAME).document(id);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        completionHandler.accept(doc.getData());
                        Log.d(TAG, "Get user info sucess");
                    }else {Log.e(TAG, "My user document does not exist");}
                }else{Log.e(TAG, "Task to get my user info wasn not successful");}
            }
        });
    }

    //Auth
    void logOut(Context context, Runnable completionHandler) {
        AuthUI.getInstance()
                .signOut(context)
                .addOnCompleteListener(new OnCompleteListener<Void>() { //pq et deixi tornar a triar conta
                    public void onComplete(@NonNull Task<Void> task) {
                        // user is now signed out
                        completionHandler.run();
                    }
                });
    }
    Intent getSignInActivity() {
        return AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(Arrays.asList(
                        new AuthUI.IdpConfig.GoogleBuilder().build(),
                        new AuthUI.IdpConfig.TwitterBuilder().build(),
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

    //Images
    void uploadImage (Uri filePath, Consumer<String> completionHandler) {
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
                        completionHandler.accept(downloadUri.toString());
                    } else {
                        Log.e(TAG, "Could not upload image");
                    }
                }
            });

        }else{
            Log.e(TAG, "Image path is null");
        }
    }
    void updateRouteImages(Route route) {
        DocumentReference docRef = db.collection(getMyRoutesPath()).document(route.getID());
        docRef.update("imageUrls", route.getImageUrls());
    }
    void deleteImgFromStorage(String imgUrl) {
        StorageReference photoRef = storageReference.getStorage().getReferenceFromUrl(imgUrl);
        photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // File deleted successfully
                Log.d(TAG, "onSuccess: deleted file");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Uh-oh, an error occurred!
                Log.d(TAG, "onFailure: did not delete file");
            }
        });
    }

    //Username
    void hasUsername(Consumer<Boolean> completionHandler) {
        if(getUser() != null) {
            DocumentReference docRef = db.document(USERS_C_NAME + "/" + getUser().getUid());
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d(TAG, "Username: Document exists!");
                            Map<String, Object> data = document.getData();
                            if (data.containsKey(Keys.K_UNIQUENAME)){
                                completionHandler.accept(true);
                                Log.d(TAG, "Username: Has username");
                            }else{
                                completionHandler.accept(false);
                                Log.d(TAG, "Username: Exists but has no key");
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
    void addNameAndUniqueNameIfNotTaken(String name, String uniqueName, Consumer<Boolean> updateUI) {
        //1. Get collection reference
        CollectionReference cRef = db.collection(USERS_C_NAME);

        //2.Query for this id
        Query query = cRef.whereEqualTo(Keys.K_UNIQUENAME, uniqueName);
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
                        data.put(Keys.K_NAME, name);
                        data.put(Keys.K_UNIQUENAME, uniqueName);

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
    void getUsernames(Consumer<List<String>> completionHandler){
        List<String> usernames = new ArrayList<String>();

        //1. Reference to users collection
        CollectionReference cRef = db.collection(USERS_C_NAME);

        //2.Retrieve usernames
        cRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){

                    for (QueryDocumentSnapshot document: task.getResult()){

                        //Exclude my own id
                        String docId = document.getId();
                        String myId = getUser().getUid();
                        if (!docId.equals(myId)){
                            usernames.add(document.get(Keys.K_UNIQUENAME).toString());
                        }
                    }

                    completionHandler.accept(usernames);

                }else{
                    Log.e(TAG, "Query failed");
                }
            }
        });
    }

    //Others
    String getMyRoutesPath() {
        return USERS_C_NAME + "/" + getUser().getUid() + "/" + ROUTES_C_NAME;
    }
    String getRoutesPathFromId(String id) {
        return USERS_C_NAME + "/" + id + "/" + ROUTES_C_NAME;
    }

    //Dynamic Links
    private Uri generateLongDeepLink() {
        DynamicLink dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse("https://paublanes.travelnet/"+getUser().getUid()))
                .setDomainUriPrefix("https://travelnet.page.link")
                // Open links with this app on Android
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder().build())
                // Open links with com.example.ios on iOS
                .setIosParameters(new DynamicLink.IosParameters.Builder("paublanes.travelnet").build())
                .buildDynamicLink();

        return dynamicLink.getUri();
    }
    void generateShortDeepLink(Activity activity, final Consumer<Uri> completionHandler) {
        Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLongLink(generateLongDeepLink())
                .buildShortDynamicLink()
                .addOnCompleteListener(activity, new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                        if (task.isSuccessful()) {
                            // Short link created
                            Uri shortLink = task.getResult().getShortLink();
                            completionHandler.accept(shortLink);
                            Log.d(TAG, "Short link created successfully");
                        } else {
                            Log.e(TAG, "Could not build dynamic link");
                        }
                    }
                });

    }
    void recieveDynamicLinks(Activity activity, Intent intent, final Consumer<Uri> completionHandler) {
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(intent)
                .addOnSuccessListener(activity, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        // Get deep link from result (may be null if no link is found)
                        if (pendingDynamicLinkData != null) {
                            completionHandler.accept(pendingDynamicLinkData.getLink());
                        }
                    }
                })
                .addOnFailureListener(activity, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "getDynamicLink:onFailure", e);
                    }
                });
    }

}
