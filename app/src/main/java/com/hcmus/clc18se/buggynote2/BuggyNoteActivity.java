package com.hcmus.clc18se.buggynote2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.hcmus.clc18se.buggynote2.database.BuggyNoteDao;
import com.hcmus.clc18se.buggynote2.database.BuggyNoteDatabase;
import com.hcmus.clc18se.buggynote2.databinding.ActivityBuggyNoteBinding;
import com.hcmus.clc18se.buggynote2.utils.PreferenceUtils;
import com.hcmus.clc18se.buggynote2.utils.interfaces.ControllableDrawerActivity;
import com.hcmus.clc18se.buggynote2.utils.interfaces.OnBackPressed;
import com.hcmus.clc18se.buggynote2.viewmodels.NotesViewModel;
import com.hcmus.clc18se.buggynote2.viewmodels.factories.NotesViewModelFactory;

import java.io.IOException;
import java.io.OutputStream;

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

    private NotesViewModel notesViewModel;

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
        setupNavigation();
        setUpViewModels();
        setContentView(binding.getRoot());

        preferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    private void setUpViewModels() {
        BuggyNoteDao database = BuggyNoteDatabase.getInstance(this).buggyNoteDatabaseDao();

        notesViewModel = new ViewModelProvider(
                this,
                new NotesViewModelFactory(
                        database,
                        this.getApplication()))
                .get(NotesViewModel.class);

    }

    private void setupNavigation() {
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
    }

    @Override
    protected void onResume() {
        super.onResume();

        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey(EXTRA_NOTE_ID)) {
            long id = extras.getLong(EXTRA_NOTE_ID, -1);
            if (id != -1) {

                navController.navigate(
                        NavigationNoteDetailsDirections.actionGlobalNavigationNoteDetails(id)
                );
            }
        }
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

    public static final String ACTION_VIEW_NOTE = "com.hcmus.clc18se.buggynote2.ACTION_VIEW_NOTE";
    public static final String EXTRA_NOTE_ID = "com.hcmus.clc18se.buggynote2.ACTION_VIEW_NOTE";

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

    }

    public void onActionShowBackupPreview() {
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setMessage(notesViewModel.getJsonFromNoteList())
                .setNegativeButton(R.string.dismiss, (u, v) -> {
                }).show();

        TextView textView = dialog.findViewById(android.R.id.message);

        if (textView != null) {
            textView.setTypeface(Typeface.MONOSPACE);
        }

    }

    public void onActionBackup() {
        createBackupFile();
    }

    public void onActionImport() {
        Toast.makeText(this, R.string.preference_import, Toast.LENGTH_SHORT).show();
    }

    // Request code for creating a PDF document.
    private static final int REQUEST_CREATE_BACKUP_FILE = 0x696969;
    private static final String BACKUP_FILE_NAME = "backup.bgn";

    private void createBackupFile() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .setType("application/json")
                .putExtra(Intent.EXTRA_TITLE, BACKUP_FILE_NAME);

        startActivityForResult(intent, REQUEST_CREATE_BACKUP_FILE);
    }

    private void saveBackupFile(Uri documentUri) {
        BuggyNoteDatabase.databaseWriteExecutor.execute(() -> {
            try (OutputStream outputStream = getContentResolver().openOutputStream(documentUri)) {
                String serializedJson = notesViewModel.getJsonFromNoteList();
                outputStream.write(serializedJson.getBytes());

                runOnUiThread(() -> {
                    Toast.makeText(this, getString(R.string.Saved), Toast.LENGTH_SHORT).show();
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CREATE_BACKUP_FILE &&
                resultCode == RESULT_OK &&
                data != null &&
                data.getData() != null) {

            saveBackupFile(data.getData());
        }
    }
}