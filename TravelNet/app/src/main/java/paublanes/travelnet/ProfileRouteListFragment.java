package paublanes.travelnet;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileRouteListFragment extends Fragment {

    RecyclerView recyclerView;
    RecyclerView.Adapter myAdapter;

    View view;

    public ProfileRouteListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //routes.add(new Route("Prova", new Date(1/2/3), new Date(5/8/15)));

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_profile_route_list, container, false);
        return view;
    }

    @Override
   public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        recyclerView = view.findViewById(R.id.rv_route_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));

        myAdapter = new RouteAdapter(this.getActivity(), ((ProfileActivity)this.getActivity()).getRoutes());
        recyclerView.setAdapter(myAdapter);
    }
}
