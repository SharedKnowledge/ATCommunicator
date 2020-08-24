package htw_berlin.ba_timsitte.activities;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import htw_berlin.ba_timsitte.R;
import htw_berlin.ba_timsitte.adapter.RREQListAdapter;
import htw_berlin.ba_timsitte.adapter.RouteListAdapter;

public class AODVFragment extends Fragment {

    private static final String TAG = "AODVFragment";

    @BindView(R.id.routingTable) ListView routingTableListView;
    @BindView(R.id.requestTable) ListView requestTableListView;
    @BindView(R.id.btnRefreshTables)
    Button btnRefreshTables;

    private RouteListAdapter routeListAdapter;
    private RREQListAdapter rreqListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_aodv, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @OnClick(R.id.btnRefreshTables)
    public void refreshTables(){
        Context ctx = getActivity().getApplicationContext();
//        routeListAdapter = new RouteListAdapter(ctx, ((MainActivity) getActivity()).getRoutingTable());
//        routingTableListView.setAdapter(routeListAdapter);
//
//        rreqListAdapter = new RREQListAdapter(ctx, ((MainActivity) getActivity()).getRreqTable());
//        requestTableListView.setAdapter(rreqListAdapter);
    }

}
