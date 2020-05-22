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
import htw_berlin.ba_timsitte.network.MapDeviceListAdapter;
import htw_berlin.ba_timsitte.network.RouteEntry;
import htw_berlin.ba_timsitte.network.RouteEntryListAdapter;


public class OverviewFragment extends Fragment {

    @BindView(R.id.overview_routingTable) ListView listViewRoutingTable;

    public ArrayList<RouteEntry> routingTable = new ArrayList<>();
    private RouteEntryListAdapter mAdapter;

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

        mAdapter = new RouteEntryListAdapter(getActivity(), routingTable);
        listViewRoutingTable.setAdapter(mAdapter);

        return view;
    }
}
