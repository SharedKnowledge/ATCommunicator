package htw_berlin.ba_timsitte.activities;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import htw_berlin.ba_timsitte.R;
import htw_berlin.ba_timsitte.network.Route;
import htw_berlin.ba_timsitte.network.RouteListAdapter;


public class OverviewFragment extends Fragment {

    @BindView(R.id.overview_routingTable) ListView listViewRoutingTable;

    public ArrayList<Route> routingTable = new ArrayList<>();
    private RouteListAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_overview, container, false);
        ButterKnife.bind(this, view);

        mAdapter = new RouteListAdapter(getActivity(), routingTable);
        listViewRoutingTable.setAdapter(mAdapter);

        return view;
    }
}
