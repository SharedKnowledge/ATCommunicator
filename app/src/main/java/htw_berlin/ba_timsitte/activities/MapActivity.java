package htw_berlin.ba_timsitte.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;
import htw_berlin.ba_timsitte.R;
import htw_berlin.ba_timsitte.network.Node;
import htw_berlin.ba_timsitte.network.MapDeviceListAdapter;
import htw_berlin.ba_timsitte.network.RouteEntry;

public class MapActivity extends AppCompatActivity {

    @BindView(R.id.lvDeviceList) ListView listViewDeviceList;
    @BindView(R.id.app_toolbar) Toolbar mToolbar;
    @BindView(R.id.btnCommand) MapView map = null;

    private static final String TAG = "MapActivity";

    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    public ArrayList<Node> nodeList = new ArrayList<>();
    public ArrayList<RouteEntry> routingTable = new ArrayList<>();
    private ArrayList<Marker> markerList = new ArrayList<>();
    private MapDeviceListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //load/initialize the osmdroid configuration, this can be done
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        setContentView(R.layout.activity_map);
        ButterKnife.bind(this);

        initiateSupportActionBar();
        initiateMapView();

        createMocks();
        mAdapter = new MapDeviceListAdapter(this, nodeList);
        listViewDeviceList.setAdapter(mAdapter);
        initiateMarkers();
        initiateLines();
    }

    @Override
    public void onPause() {
        super.onPause();
        map.onPause();
    }

    // ----------------- Toolbar methods -----------------
    public void initiateSupportActionBar(){
        setSupportActionBar(mToolbar);
        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setTitle("Map");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
            default:
                return super.onOptionsItemSelected(item);
        }
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
        for (Node d: nodeList){
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

    public void initiateLines(){
        for (RouteEntry re: routingTable){
            Log.d(TAG, "Adding RouteEntry");
            Polyline mPolyline = new Polyline(map);
            GeoPoint gpNext = re.getNext().getGp();
            GeoPoint gpDestination = re.getDestination().getGp();
            mPolyline.addPoint(gpNext);
            mPolyline.addPoint(gpDestination);
            map.getOverlays().add(mPolyline);
        }
    }


    // ----------------- testing methods -----------------
    public void createMocks(){
        Node a = new Node(1);
        Node b = new Node(2);
        Node c = new Node(3);
        Node d = new Node(4);
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
        nodeList.add(a);
        nodeList.add(b);
        nodeList.add(c);
        nodeList.add(d);

        RouteEntry re1 = new RouteEntry(1, a, b, 1);
        RouteEntry re2 = new RouteEntry(1, b, c, 1);
        RouteEntry re3 = new RouteEntry(1, a, d, 2);
        RouteEntry re4 = new RouteEntry(1, c, d, 2);

        routingTable.add(re1);
        routingTable.add(re2);
        routingTable.add(re3);
        routingTable.add(re4);

    }
}
