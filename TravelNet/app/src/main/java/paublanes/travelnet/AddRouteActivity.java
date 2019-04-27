package paublanes.travelnet;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.Calendar;

public class AddRouteActivity extends AppCompatActivity implements View.OnClickListener {

    EditText et_start_date, et_end_date, et_first_location, et_route_name;

    LatLng firstLocationCoordinates;
    Calendar startDate, endDate;

    //ACTIVITY LIFE CYCLE
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_route);

        setTitle(R.string.title_activity_add_route);

        et_start_date = findViewById(R.id.et_start_date);
        et_start_date.setOnClickListener(this);
        et_end_date = findViewById(R.id.et_end_date);
        et_end_date.setOnClickListener(this);
        et_first_location = findViewById(R.id.et_first_location);
        et_first_location.setOnClickListener(this);
        et_route_name = findViewById(R.id.et_route_name);
        findViewById(R.id.et_accept).setOnClickListener(this);
        findViewById(R.id.et_cancel).setOnClickListener(this);

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MapsActivity.ACTIVITY_KEY) { //resultCode no es RESULT_OK nse pq
            String place = data.getStringExtra(MapsActivity.PLACE_NAME_KEY);
            et_first_location.setText(place);

            Bundle bundle = data.getParcelableExtra("bundle");
            firstLocationCoordinates = bundle.getParcelable(MapsActivity.COORDINATES_KEY);
        }
    }

    //PICKERS
    void showDatePicker(final EditText et_date) {
        DatePickerFragment newFragment = DatePickerFragment.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                // +1 because january is zero
                final String selectedDate = day + " / " + (month+1) + " / " + year;
                et_date.setText(selectedDate);

                if(et_date.getId() == R.id.et_start_date) {
                    startDate = Calendar.getInstance();
                    startDate.set(year, month, day);
                }
                else {
                    endDate = Calendar.getInstance();
                    endDate.set(year,month,day);
                }

            }
        });
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }
    void showPlacePicker () {

       startActivityForResult(new Intent(AddRouteActivity.this, MapsActivity.class), MapsActivity.ACTIVITY_KEY);
    }

    //CREATE
    private void createRoute() {

        if (et_route_name.getText().toString().isEmpty()
            || et_start_date.getText().toString().isEmpty()
            || et_first_location.getText().toString().isEmpty()) {

            Toast.makeText(this, "Please enter a name, a start date and a starting location",
                    Toast.LENGTH_SHORT).show();
        }
        else{
            //1. Crear a primera localització
            RoutePoint rP = new RoutePoint(firstLocationCoordinates.latitude,
                    firstLocationCoordinates.longitude,
                    et_first_location.getText().toString());

            //2. Crear ruta
            Route route = new Route(et_route_name.getText().toString(), startDate, rP);

            //3. Afegir data final si n'hi ha
            if (!et_end_date.getText().toString().isEmpty()){
                route.setEndDate(endDate);
            }

            //4. Afegir info de diner
            route.addMoneyInfo(new MoneyInfo("Transport", 0));
            route.addMoneyInfo(new MoneyInfo("Housing", 0));
            route.addMoneyInfo(new MoneyInfo("Cash", 0));

            //5. Afegir ID i ordre
            route.setOwnerID(FirebaseManager.getInstance().getUser().getUid());
            route.setListOrder((int)getIntent().getSerializableExtra(Keys.ROUTE_ORDER));

            //6. Pujar a firebase
            FirebaseManager.getInstance().addRoute(route);

            finish();
        }

    }

    //TAP EVENTS
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.et_start_date:
                showDatePicker(et_start_date);
                break;
            case R.id.et_end_date:
                showDatePicker(et_end_date);
                break;
            case R.id.et_first_location:
                showPlacePicker();
                break;
            case R.id.et_accept:
                createRoute();
                break;
            case R.id.et_cancel:
                finish();
                break;
        }
    }
}
