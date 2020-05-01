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

public class MapDeviceListAdapter extends ArrayAdapter<Node> {
    private Context mContext;
    private List<Node> nodeList = new ArrayList<>();

    public MapDeviceListAdapter(@NonNull Context context, @SuppressLint("SupportAnnotationUsage") @LayoutRes ArrayList<Node> list) {
        super(context, 0, list);
        mContext = context;
        nodeList = list;
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.map_device_adapter_view, parent,false);

        Node currentNode = nodeList.get(position);


        TextView id = (TextView) listItem.findViewById(R.id.map_deviceId);
        id.setText(Integer.toString(currentNode.getId()));

        TextView gpLati = (TextView) listItem.findViewById(R.id.map_deviceGeoPointLatitude);
        gpLati.setText(Double.toString(currentNode.getGp().getLatitude()));

        TextView gpLong = (TextView) listItem.findViewById(R.id.map_deviceGeoPointLongitude);
        gpLong.setText(Double.toString(currentNode.getGp().getLongitude()));

        TextView isActive = (TextView) listItem.findViewById(R.id.map_deviceIsActive);
        if (currentNode.isIs_active()){
            isActive.setText("Status: Aktiv");
        } else {
            isActive.setText("Status: Inaktiv");
        }
        return listItem;
    }

}
