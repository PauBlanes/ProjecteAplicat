package paublanes.travelnet;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class RouteDetailActivity extends AppCompatActivity {

    Route route;

    TextView tv_detail_route_name, tv_detail_start_date, tv_detail_end_date;

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
    }
}
