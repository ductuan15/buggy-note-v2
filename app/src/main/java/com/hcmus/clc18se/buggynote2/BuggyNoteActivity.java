package com.hcmus.clc18se.buggynote2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import com.google.android.material.navigation.NavigationView;
import com.hcmus.clc18se.buggynote2.databinding.ActivityBuggyNoteBinding;
import com.hcmus.clc18se.buggynote2.utils.PreferenceUtils;
import com.hcmus.clc18se.buggynote2.utils.interfaces.ControllableDrawerActivity;
import com.hcmus.clc18se.buggynote2.utils.interfaces.OnBackPressed;

public class BuggyNoteActivity extends AppCompatActivity implements ControllableDrawerActivity {

    private NavHostFragment navHostFragment = null;
    private NavController navController = null;

    public AppBarConfiguration getAppBarConfiguration() {
        return appBarConfiguration;
    }

    AppBarConfiguration appBarConfiguration = null;

    private ActivityBuggyNoteBinding binding = null;
    private DrawerLayout drawerLayout = null;

    private SharedPreferences preferences = null;

    int[] topDestinations = new int[]{
            R.id.nav_notes,
            R.id.nav_tags,
            R.id.nav_archive,
            R.id.nav_trash
    };

    private final NavController.OnDestinationChangedListener onDestinationChangedListener =
            (controller, destination, arguments) -> {
                for (int id : topDestinations) {
                    if (destination.getId() == id) {
                        unlockTheDrawer();
                        return;
                    }
                }
                lockTheDrawer();
            };

    private final SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener = (pref, key) -> {
        if (key.equals(getString(R.string.app_theme_key))) {
            PreferenceUtils.configTheme(null, this);
            startActivity(new Intent(this, BuggyNoteActivity.class));
            finish();
        } else if (key.equals(getString(R.string.app_color_key))) {
            startActivity(new Intent(this, BuggyNoteActivity.class));
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceUtils.configColor(this);
        PreferenceUtils.configTheme(null, this);

        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        binding = ActivityBuggyNoteBinding.inflate(getLayoutInflater());
        navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        }

        drawerLayout = binding.drawerLayout;
        appBarConfiguration = new AppBarConfiguration.Builder(topDestinations)
                .setOpenableLayout(drawerLayout)
                .build();

        NavigationView navView = binding.navView;
        NavigationUI.setupWithNavController(navView, navController);

        navView.setNavigationItemSelectedListener(item -> {
                    drawerLayout.closeDrawer(GravityCompat.START);
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (!NavigationUI.onNavDestinationSelected(item, navController)) {
                            onOptionsItemSelected(item);
                        }
                    }, 200);
                    return true;
                }
        );
        navController.addOnDestinationChangedListener(onDestinationChangedListener);
        setContentView(binding.getRoot());

        preferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    @Override
    public void onBackPressed() {
        try {
            Fragment currentFragment = navHostFragment.getChildFragmentManager().getFragments().get(0);

            if (currentFragment instanceof OnBackPressed) {
                boolean defaultBackPress = ((OnBackPressed) currentFragment).onBackPressed();

                if (!defaultBackPress) {
                    super.onBackPressed();
                } else if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START);
                }
            } else {
                super.onBackPressed();
            }
        } catch (IndexOutOfBoundsException exception) {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        PreferenceUtils.configTheme(newConfig.uiMode, this);
        recreate();
    }

    @Override
    protected void onDestroy() {
        preferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
        super.onDestroy();
    }


    @Override
    public void lockTheDrawer() {
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    @Override
    public void unlockTheDrawer() {
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }
}