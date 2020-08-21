package htw_berlin.ba_timsitte.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import htw_berlin.ba_timsitte.R;
import htw_berlin.ba_timsitte.adapter.RouteListAdapter;

public class AODVFragment extends Fragment {

    private static final String TAG = "AODVFragment";

    @BindView(R.id.routingTable) ListView routingTableListView;
    @BindView(R.id.requestTable) ListView requestTableListView;

    private RouteListAdapter routeListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_aodv, container, false);
        ButterKnife.bind(this, view);
        //routeListAdapter = new RouteListAdapter(this, R.layout.adapter_view_route_entry);

        //routingTableListView.setAdapter(routeListAdapter);
        return view;
    }
}
