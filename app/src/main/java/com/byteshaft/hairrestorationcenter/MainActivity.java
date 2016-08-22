package com.byteshaft.hairrestorationcenter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.byteshaft.hairrestorationcenter.account.LoginActivity;
import com.byteshaft.hairrestorationcenter.fragments.AboutUsFragment;
import com.byteshaft.hairrestorationcenter.fragments.ConsultationFragment;
import com.byteshaft.hairrestorationcenter.fragments.ContactUsFragment;
import com.byteshaft.hairrestorationcenter.fragments.EducationFragment;
import com.byteshaft.hairrestorationcenter.fragments.LocationFragment;
import com.byteshaft.hairrestorationcenter.fragments.MessagesFragment;
import com.byteshaft.hairrestorationcenter.fragments.ResetPassword;
import com.byteshaft.hairrestorationcenter.fragments.UpdateProfile;
import com.byteshaft.hairrestorationcenter.gcm.QuickstartPreferences;
import com.byteshaft.hairrestorationcenter.gcm.RegistrationIntentService;
import com.byteshaft.hairrestorationcenter.utils.AppGlobals;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static MainActivity sInstance;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private BroadcastReceiver mRegistrationBroadcastReceiver;

    public static MainActivity getInstance() {
        return sInstance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sInstance = this;
        if (!AppGlobals.isUserLoggedIn()) {
            startActivity(new Intent(AppGlobals.getContext(), LoginActivity.class));
        }
        if (getIntent().getStringExtra("message") != null) {
            loadFragment(new MessagesFragment());
        } else {
            loadFragment(new EducationFragment());
        }
        setContentView(R.layout.activity_main);
        AppGlobals.sActivity = MainActivity.this;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i("TAG", "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (AppGlobals.isUserLoggedIn()) {
            final boolean sentToken = AppGlobals.isRegKeysaved();
            mRegistrationBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (sentToken) {
                        System.out.println(R.string.gcm_send_message);
                    } else {
                        System.out.println(R.string.token_error_message);
                    }
                }

            };
            if (checkPlayServices() && !sentToken) {
                // Start IntentService to register this application with GCM.
                Intent intent = new Intent(this, RegistrationIntentService.class);
                startService(intent);
            }
        }
        LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
        }
        ConsultationFragment.sUploaded = false;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        FragmentManager fragmentManager = getSupportFragmentManager();

        if (id == R.id.nav_education) {
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            loadFragment(new EducationFragment());

        } else if (id == R.id.nav_consultation) {
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            loadFragment(new ConsultationFragment());

        } else if (id == R.id.nav_messages) {
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            loadFragment(new MessagesFragment());

        } else if (id == R.id.nav_about_us) {
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            loadFragment(new AboutUsFragment());

        } else if (id == R.id.nav_location) {
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            loadFragment(new LocationFragment());

        } else if (id == R.id.nav_contact_us) {
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            loadFragment(new ContactUsFragment());

        } else if (id == R.id.nav_update_profile) {
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            loadFragment(new UpdateProfile());

        } else if (id == R.id.nav_reset_password) {
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            loadFragment(new ResetPassword());

        } else if (id == R.id.nav_logout) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("Confirmation");
            alertDialogBuilder.setMessage("Do you really want to logout?").setCancelable(false).setPositiveButton("Ok",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            SharedPreferences sharedpreferences = AppGlobals.getPreferenceManager();
                            SharedPreferences.Editor editor = sharedpreferences.edit();
                            editor.clear();
                            editor.commit();
                            AppGlobals.logout = true;
                            finish();
                            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                        }
                    });
            alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    public static void loadFragment(Fragment fragment) {
        FragmentTransaction tx = MainActivity.getInstance().getSupportFragmentManager().beginTransaction();
        tx.replace(R.id.container, fragment);
        tx.commit();
    }
}
