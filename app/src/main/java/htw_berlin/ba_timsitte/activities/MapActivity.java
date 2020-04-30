package htw_berlin.ba_timsitte.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;
import htw_berlin.ba_timsitte.R;
import htw_berlin.ba_timsitte.network.Device;
import htw_berlin.ba_timsitte.network.MapDeviceListAdapter;

public class MapActivity extends AppCompatActivity {

    @BindView(R.id.lvDeviceList) ListView listViewDeviceList;

    private static final String TAG = "MapActivity";

    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private MapView map = null;
    public ArrayList<Device> deviceList = new ArrayList<>();
    private ArrayList<Marker> markerList = new ArrayList<>();
    private MapDeviceListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //load/initialize the osmdroid configuration, this can be done
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        //inflate and create the map
        setContentView(R.layout.activity_map);
        ButterKnife.bind(this);

        map = (MapView) findViewById(R.id.btnMap);
        map.setTileSource(TileSourceFactory.MAPNIK);

        // zoom buttons
        map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT);
        map.setMultiTouchControls(true);

        // default view point - should be change to live gps location
        IMapController mapController = map.getController();
        mapController.setZoom(17.5);
        GeoPoint startPoint = new GeoPoint(52.566875, 13.410447);
        mapController.setCenter(startPoint);

        // WRITE_EXTERNAL_STORAGE is required in order to show the map
        requestPermissionsIfNecessary(new String[] {
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        });

        //

        createMocks();
        mAdapter = new MapDeviceListAdapter(this, deviceList);
        listViewDeviceList.setAdapter(mAdapter);
        initiateMarkers();
    }

    @Override
    public void onPause() {
        super.onPause();
        map.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {
            permissionsToRequest.add(permissions[i]);
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    private void requestPermissionsIfNecessary(String[] permissions) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                permissionsToRequest.add(permission);
            }
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    @OnItemClick(R.id.lvDeviceList)
    public void onItemShowMarker(int position){
        if (markerList.get(position).isEnabled()){
            markerList.get(position).setEnabled(false);
        } else {
            markerList.get(position).setEnabled(true);
        }
        map.invalidate();
    }

    public void initiateMarkers(){
        for (Device d: deviceList){
            Log.d(TAG, "Adding Marker " + d.getId());
            Marker mMarker = new Marker(map);
            GeoPoint gp = new GeoPoint(d.getGp().getLatitude(), d.getGp().getLongitude());
            mMarker.setPosition(gp);
            mMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            mMarker.setTextLabelBackgroundColor(
                    Color.WHITE
            );
            mMarker.setTextLabelForegroundColor(
                    Color.BLACK
            );
            mMarker.setTextLabelFontSize(80);
            mMarker.setTextIcon(Integer.toString(d.getId()));
            markerList.add(d.getId()-1, mMarker);
            map.getOverlays().add(mMarker);
        }
    }


    // ----------------- testing methods -----------------
    public void createMocks(){
        Device a = new Device(1);
        Device b = new Device(2);
        Device c = new Device(3);
        Device d = new Device(4);
        a.setIs_active(true);
        b.setIs_active(false);
        c.setIs_active(true);
        d.setIs_active(true);
        GeoPoint a1 = new GeoPoint(52.567719, 13.407234);
        GeoPoint b1 = new GeoPoint(52.567938, 13.409229);
        GeoPoint c1 = new GeoPoint(52.568248, 13.411879);
        GeoPoint d1 = new GeoPoint(52.566875, 13.410434);
        a.setGp(a1);
        b.setGp(b1);
        c.setGp(c1);
        d.setGp(d1);
        deviceList.add(a);
        deviceList.add(b);
        deviceList.add(c);
        deviceList.add(d);
    }
}
