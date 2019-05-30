package paublanes.travelnet;

import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Fade;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

public class RouteDetailActivity extends AppCompatActivity
        implements RoutePointAdapter.RPFunctionalities, View.OnClickListener, MoneyAdapter.MoneyFunctionalities,
        ImagesArrayAdapter.ImgGridFuncionalities {

    Route route;

    //Views
    TextView tv_detail_route_name, tv_detail_start_date, tv_detail_end_date;
    ImageButton fab_add_photos, fab_add_money, fab_add_rp;

    //Route point list
    RecyclerView rv_routepoints;
    RecyclerView.Adapter rpAdapter;

    //Money
    RecyclerView rv_money;
    RecyclerView.Adapter moneyAdapter;

    //Images
    RecyclerView rv_images;
    RecyclerView.Adapter imagesAdapter;

    boolean isMyProfile;

    //ACTIVITY LIFE CYCLE
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_detail);

        //Get route
        route = (Route)getIntent().getSerializableExtra(Keys.SELECTED_ROUTE);

        //Title
        tv_detail_route_name = findViewById(R.id.tv_detail_route_name);
        tv_detail_route_name.setText(route.getName());
        tv_detail_start_date = findViewById(R.id.tv_detail_start_date);
        tv_detail_start_date.setText(route.getStartDateString());
        tv_detail_end_date = findViewById(R.id.tv_detail_end_date);
        if (!route.getEndDateString().isEmpty()) {
            tv_detail_end_date.setText(route.getEndDateString());
        }

        //Recycler view for route points
        rv_routepoints = findViewById(R.id.rv_routepoints);
        rv_routepoints.setHasFixedSize(true);
        rv_routepoints.setLayoutManager(new LinearLayoutManager(this));
        rpAdapter = new RoutePointAdapter(this, route.getLocations());
        rv_routepoints.setAdapter(rpAdapter);
        fab_add_rp = findViewById(R.id.btn_add_rp);
        fab_add_rp.setOnClickListener(this);

        //Recycler view for money Info
        rv_money = findViewById(R.id.rv_money);
        rv_money.setHasFixedSize(true);
        rv_money.setLayoutManager(new LinearLayoutManager(this));
        moneyAdapter = new MoneyAdapter(this, route.getMoneyInfo());
        rv_money.setAdapter(moneyAdapter);
        fab_add_money = findViewById(R.id.btn_add_money);
        fab_add_money.setOnClickListener(this);
        updateTotal();

        //Images
        fab_add_photos = findViewById(R.id.btn_add_photos);
        fab_add_photos.setOnClickListener(this);
        rv_images = findViewById(R.id.rv_photos);
        rv_images.setHasFixedSize(true);
        rv_images.setLayoutManager(new GridLayoutManager(this, 4));
        imagesAdapter = new ImagesArrayAdapter(this, route.getImageUrls());
        rv_images.setAdapter(imagesAdapter);
        registerForContextMenu(rv_images);

        //Set title
        setTitle(route.getName());
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == MapsActivity.ACTIVITY_KEY && data != null) { //resultCode no es RESULT_OK nse pq

                //1. Obtenir data
                String placeName = data.getStringExtra(MapsActivity.PLACE_NAME_KEY);
                Bundle bundle = data.getParcelableExtra("bundle");
                LatLng latLng = bundle.getParcelable(MapsActivity.COORDINATES_KEY);

                //2. Crear route point i afegirlo a la ruta local
                RoutePoint rp = new RoutePoint(latLng.latitude, latLng.longitude, placeName);
                route.addLocation(rp);

                //3.refresh la recycler view
                rpAdapter.notifyDataSetChanged();

                //4.Avisar firebase
                FirebaseManager.getInstance().updateRoute(route);
            }
            else if (requestCode == Keys.K_IMAGE_ACTIVITY && data != null) {
                ClipData clipData = data.getClipData();

                if (clipData != null) {
                    //Add images chosen to route
                    for (int i = 0;  i < clipData.getItemCount(); i++) {
                        FirebaseManager.getInstance().uploadImage(clipData.getItemAt(i).getUri(),
                                this::addImageToRoute);
                    }
                }//if it's only one
                else if (data.getData() != null) {
                    FirebaseManager.getInstance().uploadImage(data.getData(),
                            this::addImageToRoute);
                }
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        isMyProfile = getIntent().getBooleanExtra(Keys.K_IS_MY_PROFILE, true);
        if (isMyProfile){
            fab_add_rp.setVisibility(View.VISIBLE);
            fab_add_money.setVisibility(View.VISIBLE);
            fab_add_photos.setVisibility(View.VISIBLE);
        }else{
            fab_add_rp.setVisibility(View.GONE);
            fab_add_money.setVisibility(View.GONE);
            fab_add_photos.setVisibility(View.GONE);
        }
    }

    //ROUTE POINTS INTERFACE
    @Override
    public void onTapNumNights(int index) {
        showNumPicker(index);
    }
    public void showNumPicker(final int index) {
        final NumberPicker numberPicker = new NumberPicker(RouteDetailActivity.this);
        numberPicker.setMaxValue(30);
        numberPicker.setMinValue(0);
        numberPicker.setValue(1);

        AlertDialog.Builder builder = new AlertDialog.Builder(RouteDetailActivity.this);
        builder.setTitle("Number of nights");
        //builder.setMessage("Choose a value :");
        builder.setPositiveButton("ACCEPT", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                route.getRoutePoint(index).setNumNights(numberPicker.getValue());
                rpAdapter.notifyDataSetChanged();

                FirebaseManager.getInstance().updateRoute(route);

                dialog.dismiss();
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setView(numberPicker);

        builder.show();
    }
    @Override
    public void rpDeleteMenu(View itemView, int index) {
        if (isMyProfile) {
            itemView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                @Override
                public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                    menu.add("delete").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            route.deleteLocation(index);
                            rpAdapter.notifyDataSetChanged();
                            FirebaseManager.getInstance().updateRoute(route);
                            return true;
                        }
                    });
                }
            });
        }
    }


    //Money
    @Override
    public void deleteMoneyCategory(int index) {
        if (isMyProfile) {
            AlertDialog.Builder builder = new AlertDialog.Builder(RouteDetailActivity.this);
            // Get the layout inflater
            LayoutInflater inflater = getLayoutInflater();

            builder.setMessage("Are you sure you want to delete?")
                    // Add action buttons
                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            route.deleteMoneyCategory(index);
                            moneyAdapter.notifyDataSetChanged();
                            FirebaseManager.getInstance().updateRoute(route);
                        }
                    })
                    .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });

            new Dialog(getApplicationContext());
            builder.show();
        }
    }
    @Override
    public void editMoneyCategory(int index) {

        AlertDialog.Builder builder = new AlertDialog.Builder(RouteDetailActivity.this);
        // Get the layout inflater
        LayoutInflater inflater = getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.money_edit, null);
        EditText et_name = dialogView.findViewById(R.id.edit_money_name);
        et_name.setText(route.getMoneyInfo().get(index).getCategory());
        EditText et_amount = dialogView.findViewById(R.id.edit_money_amount);
        et_amount.setText(String.valueOf(route.getMoneyInfo().get(index).getAmount()));

        builder.setTitle("Money Category Editor");
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(dialogView)
                // Add action buttons
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        if (et_amount != null && et_name != null){
                            route.getMoneyInfo().get(index).setAmount(Integer.parseInt(et_amount.getText().toString()));
                            route.getMoneyInfo().get(index).setCategory(et_name.getText().toString());
                            moneyAdapter.notifyDataSetChanged();
                            FirebaseManager.getInstance().updateRoute(route);
                            updateTotal();
                        } else {Log.d("Edit money dialog", "Amount or name is null");}

                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });

        new Dialog(getApplicationContext());
        builder.show();

    }
    public void addMoney() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RouteDetailActivity.this);
        // Get the layout inflater
        LayoutInflater inflater = getLayoutInflater();

        builder.setTitle("Add Money Category");
        View dialogView = inflater.inflate(R.layout.money_edit, null);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(dialogView)
                // Add action buttons
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        EditText et_name = dialogView.findViewById(R.id.edit_money_name);
                        String name = et_name.getText().toString();
                        EditText et_amount = dialogView.findViewById(R.id.edit_money_amount);
                        int amount = Integer.parseInt(et_amount.getText().toString());

                        route.getMoneyInfo().add(new MoneyInfo(name, amount));
                        moneyAdapter.notifyDataSetChanged();
                        FirebaseManager.getInstance().updateRoute(route);

                        updateTotal();
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });

        new Dialog(getApplicationContext());
        builder.show();
    }
    public void updateTotal() {
        int total = 0;
        for (MoneyInfo each : route.getMoneyInfo()){
            total += each.getAmount();
        }

        TextView tv_total = findViewById(R.id.tv_totalMoney);
        tv_total.setText(String.valueOf(total) + "$");
    }

    //PHOTOS
    void openPhotoPicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(intent, "Select Picture"), Keys.K_IMAGE_ACTIVITY);

    }
    void addImageToRoute (String url) {
        //Update route locally
        route.addImageUrl(url);

        //update recyclerview
        imagesAdapter.notifyDataSetChanged();

        //update firebase
        FirebaseManager.getInstance().updateRouteImages(route);
    }
    public void onImgTap(int index, View imageView) {

        Intent intent = new Intent(RouteDetailActivity.this, FullscreenImgActivity.class);

        intent.putExtra(Keys.K_IMG_TO_SHOW, route.getImageUrls().get(index));

        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeSceneTransitionAnimation(RouteDetailActivity.this, imageView,
                        ViewCompat.getTransitionName(imageView));
        startActivity(intent, options.toBundle());
    }
    @Override
    public void onImgLongTap(int index, View view) {
        PopupMenu popupMenu = new PopupMenu(RouteDetailActivity.this, view);
        popupMenu.getMenuInflater().inflate(R.menu.floating_menu, popupMenu.getMenu());
        popupMenu.setGravity(Gravity.RIGHT);

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                FirebaseManager.getInstance().deleteImgFromStorage(route.getImageUrls().get(index));
                route.deleteImage(index);
                imagesAdapter.notifyDataSetChanged();
                FirebaseManager.getInstance().updateRouteImages(route);
                return true;
            }
        });

        popupMenu.show();
    }

    //TAP EVENTS
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btn_add_rp:
                startActivityForResult(new Intent(RouteDetailActivity.this, MapsActivity.class), MapsActivity.ACTIVITY_KEY);
                break;
            case R.id.btn_add_money:
                addMoney();
                break;
            case R.id.btn_add_photos:
                openPhotoPicker();
                break;
        }
    }
}
