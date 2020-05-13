package htw_berlin.ba_timsitte.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;

import butterknife.BindView;
import butterknife.ButterKnife;
import htw_berlin.ba_timsitte.R;
import htw_berlin.ba_timsitte.network.AODVNetworkProtocol;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    @BindView(R.id.app_toolbar) Toolbar mToolbar;
    @BindView(R.id.drawer_layout) DrawerLayout drawerLayout;
    @BindView(R.id.navigationView) NavigationView navigationView;

    AODVNetworkProtocol protocol = AODVNetworkProtocol.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        if (findViewById(R.id.fragment_container) != null){

            if (savedInstanceState != null){
                return;
            }

            initiateFirstFragment();
        }

        navigationView.setNavigationItemSelectedListener(this);
        setSupportActionBar(mToolbar);
        setUpHomeButton();
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

    public void initiateFirstFragment(){
        BluetoothFragment firstFragment = new BluetoothFragment();

        // In case this activity was started with special instructions from an Intent,
        // pass the Intent's extras to the fragment as arguments
        firstFragment.setArguments(getIntent().getExtras());

        // Add the fragment to the 'fragment_container' FrameLayout
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, firstFragment).commit();
    }

    public void loadFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_bluetooth:
                loadFragment(new BluetoothFragment());
                drawerLayout.closeDrawers();
                break;
            case R.id.nav_command:
                loadFragment(new CommandFragment());
                drawerLayout.closeDrawers();
                break;
            case R.id.nav_map:
                loadFragment(new MapFragment());
                drawerLayout.closeDrawers();
                break;
            case R.id.nav_settings:
                loadFragment(new SettingsFragment());
                drawerLayout.closeDrawers();
                break;
        }
        return true;
    }
}
