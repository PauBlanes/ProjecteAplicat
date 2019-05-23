package paublanes.travelnet;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.ViewHolder> {

    private ArrayList<Route> routeList;

    ItemFunctionalities i_implementator;

    public interface ItemFunctionalities {
        void OnTap(int index);
        void routeDeleteMenu(View itemView, int routeIndex);
    }

    public RouteAdapter(Context context, ArrayList<Route> list) {
        routeList = list;
        i_implementator = (ItemFunctionalities) context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tv_routeName, tv_dates;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);

            tv_routeName = itemView.findViewById(R.id.tv_routeName);
            tv_dates = itemView.findViewById(R.id.tv_dates);
            tv_dates.setText("");
        }
    }

    @NonNull
    @Override
    public RouteAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.route_row_layout, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RouteAdapter.ViewHolder viewHolder, final int i) {

        Route r = routeList.get(i);
        viewHolder.tv_routeName.setText(r.getName());

        viewHolder.tv_dates.setText(r.getStartDateString() + " - " + r.getEndDateString());

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                i_implementator.OnTap(i); //passar id de firebase
            }
        });

        i_implementator.routeDeleteMenu(viewHolder.itemView, i);
    }

    @Override
    public int getItemCount() {
        return routeList.size();
    }
}
