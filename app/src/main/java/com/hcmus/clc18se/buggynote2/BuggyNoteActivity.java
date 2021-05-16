package com.hcmus.clc18se.buggynote2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.google.android.material.navigation.NavigationView;
import com.hcmus.clc18se.buggynote2.databinding.ActivityBuggyNoteBinding;
import com.hcmus.clc18se.buggynote2.utils.OnBackPressed;

public class BuggyNoteActivity extends AppCompatActivity {

    private NavHostFragment navHostFragment = null;
    private NavController navController = null;

    public AppBarConfiguration getAppBarConfiguration() {
        return appBarConfiguration;
    }

    AppBarConfiguration appBarConfiguration = null;

    private ActivityBuggyNoteBinding binding = null;
    private DrawerLayout drawerLayout = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false);

        binding = ActivityBuggyNoteBinding.inflate(getLayoutInflater());
        navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        }

        drawerLayout = binding.drawerLayout;
        appBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_notes,
                R.id.nav_tags,
                R.id.nav_archive)
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
        setContentView(binding.getRoot());
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

}