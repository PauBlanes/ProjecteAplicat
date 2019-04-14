package paublanes.travelnet;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class RoutePointAdapter extends RecyclerView.Adapter<RoutePointAdapter.ViewHolder> {

    MyInterface i_implementator;
    public interface MyInterface {
        void onTap(int index);
    }

    private ArrayList<RoutePoint> routePointsList;

    public RoutePointAdapter (Context context, ArrayList<RoutePoint> list) {
        routePointsList  = list;
        i_implementator = (MyInterface) context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tv_rp_location, tv_rp_nº_nights;
        LinearLayout layout_nºnights;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tv_rp_location = itemView.findViewById(R.id.tv_rp_location);
            tv_rp_nº_nights = itemView.findViewById(R.id.tv_rp_nº_nights);
            layout_nºnights = itemView.findViewById(R.id.layout_nºnights);
        }
    }

    @NonNull
    @Override
    public RoutePointAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.routepoint_row_layout, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoutePointAdapter.ViewHolder viewHolder, final int i) {
        RoutePoint rp = routePointsList.get(i);

        viewHolder.tv_rp_location.setText(rp.getPlaceName());
        viewHolder.tv_rp_nº_nights.setText("x" + rp.getNumNights());
        viewHolder.layout_nºnights.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                i_implementator.onTap(i);
            }
        });


    }

    @Override
    public int getItemCount() {
        return routePointsList.size();
    }

    public void setNumNights(String numNights) {

    }
}
