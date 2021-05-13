package com.hcmus.clc18se.buggynote2.viewmodels;

import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.hcmus.clc18se.buggynote2.data.CheckListItem;
import com.hcmus.clc18se.buggynote2.data.NoteWithTags;
import com.hcmus.clc18se.buggynote2.data.Photo;
import com.hcmus.clc18se.buggynote2.database.BuggyNoteDao;
import com.hcmus.clc18se.buggynote2.database.BuggyNoteDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import timber.log.Timber;

public class NoteDetailsViewModel extends AndroidViewModel {

    private final long id;
    private final BuggyNoteDao database;

    public NoteDetailsViewModel(
            Application application,
            long id,
            BuggyNoteDao database
    ) {
        super(application);
        this.id = id;
        this.database = database;
    }

    private LiveData<NoteWithTags> note;

    public LiveData<NoteWithTags> getNote() {
        if (note == null) {
            note = database.getNoteFromId(id);
            note.observeForever((note) -> {
                if (note != null && note.note.isCheckList()) {

                    BuggyNoteDatabase.databaseWriteExecutor.execute(() ->
                            checkListItems.postValue(
                                    CheckListItem.compileFromNoteContent(note.note.noteContent)
                            )
                    );

                }
            });
        }
        return note;
    }

    private final MutableLiveData<Boolean> reloadDataRequest = new MutableLiveData<>(false);

    public final LiveData<Boolean> getReloadDataRequestState() {
        return reloadDataRequest;
    }

    public void requestReloadingData() {
        reloadDataRequest.setValue(true);
    }

    public void doneRequestingReloadData() {
        reloadDataRequest.setValue(false);
    }

    private final MutableLiveData<Long> navigateToTagSelection = new MutableLiveData<>(null);

    public final LiveData<Long> getNavigateToTagSelection() {
        return navigateToTagSelection;
    }

    private final MutableLiveData<Long> navigateToPhotoView = new MutableLiveData<>(null);

    public final LiveData<Long> getNavigateToPhotoView() {
        return navigateToPhotoView;
    }

    private final MutableLiveData<List<CheckListItem>> checkListItems = new MutableLiveData<>(null);

    public final LiveData<List<CheckListItem>> getCheckListItems() {
        return checkListItems;
    }

    public void navigateToTagSelection() {
        navigateToTagSelection.setValue(id);
    }

    public void doneNavigatingToTagSelection() {
        navigateToTagSelection.setValue(null);
    }

    public void navigateToPhotoView() {
        navigateToPhotoView.setValue(id);
    }

    public void doneNavigatingToPhotoView() {
        navigateToPhotoView.setValue(null);
    }

    private final MutableLiveData<Boolean> deleteRequest = new MutableLiveData<>(false);

    public LiveData<Boolean> getDeleteRequest() {
        return (LiveData<Boolean>) deleteRequest;
    }

    public void doneHandlingRequestDate() {
        deleteRequest.setValue(false);
    }

    public void deleteMe() {
        if (note.getValue() != null) {
            BuggyNoteDatabase.databaseWriteExecutor.execute(() -> {
                int nCol = database.removeNote(note.getValue().note);
                Timber.d("Remove note" + nCol + " affected");

                deleteRequest.postValue(true);
            });
        }
    }

    public void reloadNote() {
        note = database.getNoteFromId(id);
    }

    public void togglePin() {
        if (note.getValue() != null) {
            note.getValue().note.isPinned = !note.getValue().note.isPinned;
        }
    }

    public void addPhoto(Uri uri) {

        BuggyNoteDatabase.databaseWriteExecutor.execute(() -> {

            ContextWrapper cw = new ContextWrapper(getApplication().getApplicationContext());
            File directory = cw.getDir("photos", Context.MODE_PRIVATE);

            StringBuilder fileName = new StringBuilder(String.valueOf(System.currentTimeMillis()));
            File fileDest = new File(directory, fileName.toString());

            while (fileDest.exists()) {
                fileName.append("0");
                fileDest = new File(directory, fileName.toString());
            }

            try {
                copy(uri, fileDest);
                File[] files = directory.listFiles((dir, name) -> name.equals(fileName.toString()));

                Photo photo = new Photo(0L, Uri.fromFile(files[0]).toString(), note.getValue().note.id);
                database.addPhoto(photo);

            } catch (IOException e) {
                e.printStackTrace();
            }

        });
    }

    public void copy(Uri uri, File dst) throws IOException {

        try (InputStream in = getApplication().getContentResolver().openInputStream(uri)) {
            try (OutputStream out = new FileOutputStream(dst)) {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
        }
    }
}
