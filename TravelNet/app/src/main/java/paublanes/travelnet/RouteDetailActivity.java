package paublanes.travelnet;

import android.app.Dialog;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

public class RouteDetailActivity extends AppCompatActivity implements RoutePointAdapter.MyInterface {

    Route route;

    TextView tv_detail_route_name, tv_detail_start_date, tv_detail_end_date;

    //Route point list
    RecyclerView rv_routepoints;
    RecyclerView.Adapter rpAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_detail);

        route = (Route)getIntent().getSerializableExtra(ProfileActivity.K_ROUTE_NAME);

        tv_detail_route_name = findViewById(R.id.tv_detail_route_name);
        tv_detail_route_name.setText(route.getName());
        tv_detail_start_date = findViewById(R.id.tv_detail_start_date);
        tv_detail_start_date.setText(route.stringStartDate());
        tv_detail_end_date = findViewById(R.id.tv_detail_end_date);
        if (!route.stringEndDate().isEmpty()) {
            tv_detail_end_date.setText(route.stringEndDate());
        }

        //Recycler view for route points
        rv_routepoints = findViewById(R.id.rv_routepoints);
        rv_routepoints.setHasFixedSize(true);
        rv_routepoints.setLayoutManager(new LinearLayoutManager(this));
        rpAdapter = new RoutePointAdapter(this, route.getLocations());
        rv_routepoints.setAdapter(rpAdapter);
    }

    @Override
    public void onTap(int index) {
        show(index);
    }

    public void show(final int index)
    {

        final Dialog d = new Dialog(RouteDetailActivity.this);
        d.setTitle("NumberPicker");
        d.setContentView(R.layout.numpicker_dialog);
        Button b1 = (Button) d.findViewById(R.id.button1);
        Button b2 = (Button) d.findViewById(R.id.button2);
        final NumberPicker np = (NumberPicker) d.findViewById(R.id.numberPicker1);
        np.setMaxValue(100);
        np.setMinValue(0);
        np.setWrapSelectorWheel(false);
        //np.setOnValueChangedListener(RouteDetailActivity.this);
        b1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                route.getLocations().get(index).setNumNights(np.getValue());
                d.dismiss();
            }
        });
        b2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });
        d.show();


    }
}
