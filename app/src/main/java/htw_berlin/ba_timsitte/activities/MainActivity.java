package htw_berlin.ba_timsitte.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;

import butterknife.BindView;
import butterknife.ButterKnife;
import htw_berlin.ba_timsitte.R;
import htw_berlin.ba_timsitte.communication.BluetoothService;
import htw_berlin.ba_timsitte.network.AODVNetworkProtocol;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private static final String TAG = "MainActivity";
    @BindView(R.id.app_toolbar) Toolbar mToolbar;
    @BindView(R.id.drawer_layout) DrawerLayout drawerLayout;
    @BindView(R.id.navigationView) NavigationView navigationView;

    private BluetoothFragment mBluetoothFragment;
    private CommandFragment mCommandFragment;
    private MapFragment mMapFragment;
    private SettingsFragment mSettingsFragment;
    private OverviewFragment mOverviewFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initiateFragments();

        if (findViewById(R.id.fragment_container) != null){

            if (savedInstanceState != null){
                return;
            }

            initiateFirstFragment();
        }

        navigationView.setNavigationItemSelectedListener(this);
        setSupportActionBar(mToolbar);
        setUpHomeButton();

        // mBroadcastReceiver2 for discovery state
        IntentFilter discoverStateIntent = new IntentFilter();
        discoverStateIntent.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        discoverStateIntent.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        discoverStateIntent.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(mBroadcastReceiver2, discoverStateIntent);

        // mBroadcastReceiver for bluetooth state
        IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver3, intentFilter);


    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mBroadcastReceiver2);
        unregisterReceiver(mBroadcastReceiver3);
        super.onDestroy();
    }

    // ----------------- Toolbar methods -----------------

    private void setUpHomeButton(){
        if (getSupportActionBar() != null){
            final ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        } return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_bluetooth:
                //loadFragment(mBluetoothFragment);
                drawerLayout.closeDrawers();
                break;
            case R.id.nav_command:
                loadFragment(mCommandFragment);
                drawerLayout.closeDrawers();
                break;
            case R.id.nav_map:
                loadFragment(mMapFragment);
                drawerLayout.closeDrawers();
                break;
            case R.id.nav_settings:
                loadFragment(mSettingsFragment);
                drawerLayout.closeDrawers();
                break;
        }
        return true;
    }

    // ----------------- Fragment methods -----------------

    public void initiateFragments(){
        mBluetoothFragment = new BluetoothFragment();
        mCommandFragment = new CommandFragment();
        mMapFragment = new MapFragment();
        mSettingsFragment = new SettingsFragment();
        mOverviewFragment = new OverviewFragment();
    }

    public void initiateFirstFragment(){
        // In case this activity was started with special instructions from an Intent,
        // pass the Intent's extras to the fragment as arguments
        mBluetoothFragment.setArguments(getIntent().getExtras());
        mOverviewFragment.setArguments(getIntent().getExtras());
        // Add the fragment to the 'fragment_container' FrameLayout
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, mBluetoothFragment)
                .commit();
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_overview, mOverviewFragment)
                .commit();
    }

    public void loadFragment(Fragment fragment) {
        if (fragment != null){
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
            Log.d(TAG, "loadFragment: " + fragment.toString());
        }
    }

    // ----------------- BroadcastReceiver methods -----------------
    /**
     * Broadcast receiver for discover state
     */
    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)){
                Log.d(TAG, "mBroadcastReceiver2: Discovery process started");
                mBluetoothFragment.setDiscoverStatus("Discovery in progress...");
                mBluetoothFragment.getMbtnDiscover().setEnabled(false);
            }
            else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)){
                Log.d(TAG, "onReceive: Discovery finished");
                mBluetoothFragment.setDiscoverStatus("Discovery ended.");
                mBluetoothFragment.getMbtnDiscover().setEnabled(true);
            }
        }
    };

    /**
     * Broadcast receiver for changes made to bluetooth states
     */
    private final BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)){
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                switch (state){
                    case BluetoothAdapter.STATE_OFF:
                        // Log.d(TAG, "mBroadcastReceiver3: STATE OFF");
                        mBluetoothFragment.setDiscoverStatus("Please turn on Bluetooth.");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "mBroadcastReceiver3: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        // Log.d(TAG, "mBroadcastReceiver3: STATE ON");
                        mBluetoothFragment.setDiscoverStatus("Bluetooth on. Ready for discovering.");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "mBroadcastReceiver3: STATE TURNING ON");
                        break;
                }
            }
        }
    };


    /**
     * Broadcast receiver for checking to which device it is connected
     */
    private final BroadcastReceiver mBroadcastReceiver4 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(BluetoothAdapter.STATE_CONNECTED)){

            }
        }
    };

    public CommandFragment getCommandFragment() {
        return mCommandFragment;
    }
}
