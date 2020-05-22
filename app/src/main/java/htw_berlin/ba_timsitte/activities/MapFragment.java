package htw_berlin.ba_timsitte.activities;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

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
import htw_berlin.ba_timsitte.network.MapDeviceListAdapter;
import htw_berlin.ba_timsitte.network.Node;
import htw_berlin.ba_timsitte.network.RouteEntry;

public class MapFragment extends Fragment {

    @BindView(R.id.lvDeviceList)
    ListView listViewDeviceList;
    @BindView(R.id.mapView)
    MapView map = null;

    private static final String TAG = "MapFragment";

    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    public ArrayList<Node> nodeList = new ArrayList<>();
    public ArrayList<RouteEntry> routingTable = new ArrayList<>();
    private ArrayList<Marker> markerList = new ArrayList<>();
    private MapDeviceListAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        //load/initialize the osmdroid configuration, this can be done
        Context ctx = getActivity().getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        ButterKnife.bind(this, view);

        initiateMapView();

//        Intent intent = getActivity().getIntent();
//        nodeList = intent.getParcelableArrayListExtra("nodeList");

        mAdapter = new MapDeviceListAdapter(getActivity(), nodeList);
        listViewDeviceList.setAdapter(mAdapter);
//        initiateMarkers();
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        map.onPause();
    }



    // ----------------- Map methods -----------------
    public void initiateMapView(){
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
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {
            permissionsToRequest.add(permissions[i]);
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    getActivity(),
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    private void requestPermissionsIfNecessary(String[] permissions) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(getActivity(), permission)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                permissionsToRequest.add(permission);
            }
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    getActivity(),
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    /**
     * Enable/disable marker of node on map
     * @param position
     */
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
        for (Node d: nodeList){
            Log.d(TAG, "Adding Marker " + d.getAddr());
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
            mMarker.setTextIcon(d.getAddr());
            markerList.add(mMarker);
            map.getOverlays().add(mMarker);
        }
    }
}
