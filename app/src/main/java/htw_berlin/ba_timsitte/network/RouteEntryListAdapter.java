package htw_berlin.ba_timsitte.network;

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

public class RouteEntryListAdapter extends ArrayAdapter<RouteEntry> {
    private Context mContext;
    private List<RouteEntry> routeEntryList = new ArrayList<>();

    public RouteEntryListAdapter(@NonNull Context context, @SuppressLint("SupportAnnotationUsage") @LayoutRes ArrayList<RouteEntry> list) {
        super(context, 0, list);
        mContext = context;
        routeEntryList = list;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.adapter_view_route_entry, parent,false);

        RouteEntry currentRouteEntry = routeEntryList.get(position);

        TextView destination = (TextView) listItem.findViewById(R.id.av_destination);
        destination.setText(currentRouteEntry.getDestination());

        TextView next = (TextView) listItem.findViewById(R.id.av_next);
        next.setText(currentRouteEntry.getNext());

        TextView hopCount = (TextView) listItem.findViewById(R.id.av_hopcount);
        hopCount.setText(currentRouteEntry.getHop_count());

        TextView sequence = (TextView) listItem.findViewById(R.id.av_sequence);
        sequence.setText(currentRouteEntry.getSequence());

        return listItem;
    }
}
