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

public class MapDeviceListAdapter extends ArrayAdapter<Device> {
    private Context mContext;
    private List<Device> deviceList = new ArrayList<>();

    public MapDeviceListAdapter(@NonNull Context context, @SuppressLint("SupportAnnotationUsage") @LayoutRes ArrayList<Device> list) {
        super(context, 0, list);
        mContext = context;
        deviceList = list;
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.map_device_adapter_view, parent,false);

        Device currentDevice = deviceList.get(position);


        TextView id = (TextView) listItem.findViewById(R.id.map_deviceId);
        id.setText(Integer.toString(currentDevice.getId()));

        TextView gpLati = (TextView) listItem.findViewById(R.id.map_deviceGeoPointLatitude);
        gpLati.setText(Double.toString(currentDevice.getGp().getLatitude()));

        TextView gpLong = (TextView) listItem.findViewById(R.id.map_deviceGeoPointLongitude);
        gpLong.setText(Double.toString(currentDevice.getGp().getLongitude()));

        TextView isActive = (TextView) listItem.findViewById(R.id.map_deviceIsActive);
        if (currentDevice.isIs_active()){
            isActive.setText("Status: Aktiv");
        } else {
            isActive.setText("Status: Inaktiv");
        }
        return listItem;
    }

}
