package paublanes.travelnet;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

public class RouteDetailActivity extends AppCompatActivity implements RoutePointAdapter.MyInterface, View.OnClickListener {

    Route route;

    //Apartado general
    TextView tv_detail_route_name, tv_detail_start_date, tv_detail_end_date;

    //Route point list
    RecyclerView rv_routepoints;
    RecyclerView.Adapter rpAdapter;

    //Money
    RecyclerView rv_money;
    RecyclerView.Adapter moneyAdapter;

    Intent resultIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_detail);

        route = (Route)getIntent().getSerializableExtra(Keys.SELECTED_ROUTE);

        resultIntent = new Intent();

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

    }

    //interface del recycler view
    @Override
    public void onTapNumNights(int index) {
        showNumPicker(index);
    }

    public void showNumPicker(final int index)
    {
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
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btn_add_rp:
                startActivityForResult(new Intent(RouteDetailActivity.this, MapsActivity.class), MapsActivity.ACTIVITY_KEY);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MapsActivity.ACTIVITY_KEY) { //resultCode no es RESULT_OK nse pq

            //1. Obtenir data
            String placeName = data.getStringExtra(MapsActivity.PLACE_NAME_KEY);
            Bundle bundle = data.getParcelableExtra("bundle");
            LatLng latLng = bundle.getParcelable(MapsActivity.COORDINATES_KEY);

            //2. Crear route point i afegirlo a la ruta local
            RoutePoint rp = new RoutePoint(latLng.latitude, latLng.longitude, placeName);
            route.addLocation(rp);

            //3.refresh la recycler view
            rpAdapter.notifyDataSetChanged();

        }
    }

    //ENVIAR INFO AL PERFIL QUAN TIRO ENRERE
    @Override
    public void finish() {
        resultIntent.putExtra(Keys.SELECTED_ROUTE, route);
        setResult(RESULT_OK, resultIntent);

        super.finish();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case android.R.id.home:
                //perque es comporti igual que la fletxeta del mobil
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
