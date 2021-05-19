package com.hcmus.clc18se.buggynote2.fragments;

import android.app.Activity;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.hcmus.clc18se.buggynote2.BuggyNoteActivity;
import com.hcmus.clc18se.buggynote2.R;
import com.hcmus.clc18se.buggynote2.utils.views.LongClickablePreference;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
        setupPreferences();
    }

    private void setupPreferences() {
        Preference backupPref = findPreference(getString(R.string.preference_backup_key));

        if (backupPref instanceof LongClickablePreference) {

            ((LongClickablePreference) backupPref).setLongClickListener(v -> {
                Activity activity = requireActivity();
                if (activity instanceof BuggyNoteActivity) {
                    ((BuggyNoteActivity) activity).onActionShowBackupPreview();
                    return true;
                }
                return false;
            });
        }

        backupPref.setOnPreferenceClickListener(
                preference -> {
                    Activity activity = requireActivity();
                    if (activity instanceof BuggyNoteActivity) {
                        ((BuggyNoteActivity) activity).onActionBackup();
                        return true;
                    }
                    return false;
                }
        );

        Preference importPref = findPreference(getString(R.string.preference_import_key));
        importPref.setOnPreferenceClickListener(
                preference -> {
                    Activity activity = requireActivity();
                    if (activity instanceof BuggyNoteActivity) {
                        ((BuggyNoteActivity) activity).onActionImport();
                        return true;
                    }
                    return false;
                }
        );
    }
}
