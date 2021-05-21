package com.hcmus.clc18se.buggynote2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.webkit.MimeTypeMap;
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
import com.hcmus.clc18se.buggynote2.data.Note;
import com.hcmus.clc18se.buggynote2.database.BuggyNoteDao;
import com.hcmus.clc18se.buggynote2.database.BuggyNoteDatabase;
import com.hcmus.clc18se.buggynote2.databinding.ActivityBuggyNoteBinding;
import com.hcmus.clc18se.buggynote2.utils.PreferenceUtils;
import com.hcmus.clc18se.buggynote2.utils.interfaces.ControllableDrawerActivity;
import com.hcmus.clc18se.buggynote2.utils.interfaces.OnBackPressed;
import com.hcmus.clc18se.buggynote2.viewmodels.NotesViewModel;
import com.hcmus.clc18se.buggynote2.viewmodels.factories.NotesViewModelFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;
import java.util.Objects;

import timber.log.Timber;

public class BuggyNoteActivity extends AppCompatActivity implements ControllableDrawerActivity {

    private static final int REQUEST_CODE_INTRO = 0x66969;

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

        boolean firstStart = preferences.getBoolean(getString(R.string.first_time_key), true);
        if (firstStart) {
            Intent intent = new Intent(this, AppIntroActivity.class);
            startActivityForResult(intent, REQUEST_CODE_INTRO);
        }

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
        openBackupFile();
    }

    // Request code for creating a backup file.
    private static final int REQUEST_CREATE_BACKUP_FILE = 0x696969;
    private static final String BACKUP_FILE_NAME = "backup";

    private void createBackupFile() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .setType("application/json")
                .putExtra(Intent.EXTRA_TITLE, BACKUP_FILE_NAME);

        startActivityForResult(intent, REQUEST_CREATE_BACKUP_FILE);
    }

    private static final int REQUEST_IMPORT_BACKUP_FILE = 0x96969;

    private void openBackupFile() {

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension("json");
        if (type == null) {
            intent.setType("application/octet-stream");
        } else {
            intent.setType(type);
        }
        Timber.d(intent.getType());

        startActivityForResult(intent, REQUEST_IMPORT_BACKUP_FILE);
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

    private void readBackupFile(Uri documentUri) {
        BuggyNoteDatabase.databaseWriteExecutor.execute(() -> {
            StringBuilder stringBuilder = new StringBuilder();

            try (InputStream inputStream = getContentResolver().openInputStream(documentUri);
                 BufferedReader reader = new BufferedReader(
                         new InputStreamReader(Objects.requireNonNull(inputStream)))
            ) {
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }

            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            List<Note> notes = Note.deserializeFromJson(stringBuilder.toString());
            runOnUiThread(() -> {
                importNotes(notes);
            });
        });
    }

    private void importNotes(List<Note> notes) {
        if (notes == null || notes.isEmpty()) {
            Toast.makeText(this, getString(R.string.import_failed), Toast.LENGTH_SHORT).show();
        } else {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.import_note_dialog_title)
                    .setMessage(getString(R.string.import_note_dialog_msg, notes.size()))
                    .setPositiveButton(getString(R.string.import_notes_dialog_positive), (d, w) -> {
                        notesViewModel.insertNewNotes(notes);
                        Toast.makeText(this, getString(R.string.import_note_dialog_succeed), Toast.LENGTH_SHORT).show();
                    }).setNegativeButton(R.string.cancel, (d, w) -> {
            })
                    .show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK &&
                data != null &&
                data.getData() != null) {

            if (requestCode == REQUEST_CREATE_BACKUP_FILE) {
                saveBackupFile(data.getData());
            } else if (requestCode == REQUEST_IMPORT_BACKUP_FILE) {
                readBackupFile(data.getData());
            }
        }

        if (requestCode == REQUEST_CODE_INTRO) {
            if (resultCode == RESULT_OK) {
                preferences.edit()
                        .putBoolean(getString(R.string.first_time_key), false)
                        .apply();
            } else {
                preferences.edit()
                        .putBoolean(getString(R.string.first_time_key), true)
                        .apply();
                //User cancelled the intro so we'll finish this activity too.
                finish();
            }
        }
    }
}