package htw_berlin.ba_timsitte.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import htw_berlin.ba_timsitte.R;
import htw_berlin.ba_timsitte.network.Route;

public class RouteListAdapter extends ArrayAdapter<Route> {
    private Context mContext;
    private List<Route> routeList = new ArrayList<>();

    public RouteListAdapter(@NonNull Context context, @SuppressLint("SupportAnnotationUsage") @LayoutRes ArrayList<Route> list) {
        super(context, 0, list);
        mContext = context;
        routeList = list;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.adapter_view_route_entry, parent,false);

        Route currentRoute = routeList.get(position);

        TextView destination = (TextView) listItem.findViewById(R.id.av_destination);
        destination.setText(currentRoute.getDestination());

        TextView next = (TextView) listItem.findViewById(R.id.av_next);
        next.setText(currentRoute.getNext());

        TextView hopCount = (TextView) listItem.findViewById(R.id.av_hopcount);
        hopCount.setText(currentRoute.getHop_count());

        TextView sequence = (TextView) listItem.findViewById(R.id.av_sequence);
        sequence.setText(currentRoute.getSequence());

        return listItem;
    }
}
