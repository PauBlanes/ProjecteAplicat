package paublanes.travelnet;

import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

public class RouteDetailActivity extends AppCompatActivity implements RoutePointAdapter.MyInterface, View.OnClickListener {

    Route route;

    //Views
    TextView tv_detail_route_name, tv_detail_start_date, tv_detail_end_date;

    //Route point list
    RecyclerView rv_routepoints;
    RecyclerView.Adapter rpAdapter;

    //Money
    RecyclerView rv_money;
    RecyclerView.Adapter moneyAdapter;

    //Images
    RecyclerView rv_images;
    RecyclerView.Adapter imagesAdapter;

    //ACTIVITY LIFE CYCLE
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_detail);

        //Get route
        route = (Route)getIntent().getSerializableExtra(Keys.SELECTED_ROUTE);

        //Set views
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
        findViewById(R.id.btn_add_rp).setOnClickListener(this);

        //Recycler view for money Info
        rv_money = findViewById(R.id.rv_money);
        rv_money.setHasFixedSize(true);
        rv_money.setLayoutManager(new LinearLayoutManager(this));
        moneyAdapter = new MoneyAdapter(this, route.getMoneyInfo());
        rv_money.setAdapter(moneyAdapter);

        //Images
        findViewById(R.id.btn_add_photos).setOnClickListener(this);
        rv_images = findViewById(R.id.rv_photos);
        rv_images.setHasFixedSize(true);
        rv_images.setLayoutManager(new GridLayoutManager(this, 4));
        imagesAdapter = new ImagesArrayAdapter(this, route.getImageUrls());
        rv_images.setAdapter(imagesAdapter);

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




                }
            }
        }
    }

    void addImageToRoute (String url) {
        //Update route
        route.addImageUrl(url);

        //update recyclerview
        imagesAdapter.notifyDataSetChanged();

        //update firebase
        FirebaseManager.getInstance().updateRoute(route);
    }

    //CHANGE NUM NIGHTS
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

    //PHOTOS
    void openPhotoPicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);

        if (Build.VERSION.SDK_INT>=22) {
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), Keys.K_IMAGE_ACTIVITY);
        }
    }

    //TAP EVENTS
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btn_add_rp:
                startActivityForResult(new Intent(RouteDetailActivity.this, MapsActivity.class), MapsActivity.ACTIVITY_KEY);
                break;
            case R.id.btn_add_photos:
                openPhotoPicker();
                break;
        }
    }


}
