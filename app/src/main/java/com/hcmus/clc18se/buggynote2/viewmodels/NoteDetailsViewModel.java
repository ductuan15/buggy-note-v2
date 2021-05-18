package com.hcmus.clc18se.buggynote2.viewmodels;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.webkit.MimeTypeMap;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.hcmus.clc18se.buggynote2.data.Audio;
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
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class NoteDetailsViewModel extends AndroidViewModel {

    private final long id;
    private final BuggyNoteDao database;
    public int photoIndex;

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
            note = database.getNoteLiveDataFromId(id);
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

    private final MutableLiveData<Long> navigateToAudioView = new MutableLiveData<>(null);

    public final LiveData<Long> getNavigateToPhotoView() {
        return navigateToPhotoView;
    }

    public final LiveData<Long> getNavigateToAudioView() {
        return navigateToAudioView;
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

    public void navigateToAudioView() { navigateToAudioView.setValue(id); }

    public void doneNavigatingToPhotoView() {
        navigateToPhotoView.setValue(null);
    }

    public void doneNavigatingToAudioView() {
        navigateToAudioView.setValue(null);
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
                removePhotos(note.getValue().photos);
                removeAudios(note.getValue().audios);

                getApplication()
                        .getApplicationContext()
                        .getSharedPreferences("REMINDER", Context.MODE_PRIVATE)
                        .edit().remove(String.valueOf(note.getValue().note.id))
                        .apply();

                deleteRequest.postValue(true);
            });
        }
    }

    public void reloadNote() {
        note = database.getNoteLiveDataFromId(id);
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

            ContentResolver cR = getApplication().getContentResolver();
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            String type = mime.getExtensionFromMimeType(cR.getType(uri));

            if (type != null && !type.isEmpty()) {
                fileName.append('.')
                        .append(type);
            }

            //int extensionIdx = uri.getPath().lastIndexOf('.');
            // int extensionIdx = fileUri.getPath().lastIndexOf('.');

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

    public void removePhoto(Photo photo) {
        BuggyNoteDatabase.databaseWriteExecutor.execute(() -> {
            try {
                File file = new File(Uri.parse(photo.uri).getPath());

                if (file.exists() && (file.delete())) {
                    database.deletePhoto(photo);
                    photoRemoved.postValue(true);

                    NoteWithTags note = getNote().getValue();
                    if (note != null) {
                        List<Photo> photos = note.photos;
                        photos.remove(photo);
                    }

                }

            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        });
    }

    public void addAudio(Uri uri) {

        BuggyNoteDatabase.databaseWriteExecutor.execute(() -> {

            ContextWrapper cw = new ContextWrapper(getApplication().getApplicationContext());
            File directory = cw.getDir("audios", Context.MODE_PRIVATE);

            StringBuilder fileName = new StringBuilder(String.valueOf(System.currentTimeMillis()));

            ContentResolver cR = getApplication().getContentResolver();
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            String type = mime.getExtensionFromMimeType(cR.getType(uri));

            if (type != null && !type.isEmpty()) {
                fileName.append('.')
                        .append(type);
            }

            //int extensionIdx = uri.getPath().lastIndexOf('.');
            // int extensionIdx = fileUri.getPath().lastIndexOf('.');

            File fileDest = new File(directory, fileName.toString());

            while (fileDest.exists()) {
                fileName.append("0");
                fileDest = new File(directory, fileName.toString());
            }

            try {
                copy(uri, fileDest);
                File[] files = directory.listFiles((dir, name) -> name.equals(fileName.toString()));

                Audio audio = new Audio(0L, Uri.fromFile(files[0]).toString(), note.getValue().note.id);
                database.addAudio(audio);

            } catch (IOException e) {
                e.printStackTrace();
            }

        });
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getApplication().getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                assert cursor != null;
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public void removeAudio(Audio audio) {
        BuggyNoteDatabase.databaseWriteExecutor.execute(() -> {
            try {
                File file = new File(Uri.parse(audio.uri).getPath());

                if (file.exists() && (file.delete())) {
                    database.deleteAudio(audio);
                    photoRemoved.postValue(true);

                    NoteWithTags note = getNote().getValue();
                    if (note != null) {
                        List<Audio> audios = note.audios;
                        audios.remove(audio);
                    }

                }

            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        });
    }

    public void removePhotos(List<Photo> photos) {
        if (photos != null) {
            return;
        }

        BuggyNoteDatabase.databaseWriteExecutor.execute(() -> {
            try {
                List<File> files = new ArrayList<>();
                for (Photo photo : photos) {
                    File file = new File(Uri.parse(photo.uri).getPath());
                    files.add(file);
                }

                for (File file : files) {
                    if (file.exists()) {
                        file.delete();
                    }
                }
                NoteWithTags note = getNote().getValue();
                if (note != null) {
                    List<Photo> allPhotos = note.photos;
                    allPhotos.removeAll(photos);
                }

                database.deletePhoto(photos.toArray(new Photo[]{}));

            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        });
    }

    public void removeAudios(List<Audio> audios) {
        if (audios != null) {
            return;
        }

        BuggyNoteDatabase.databaseWriteExecutor.execute(() -> {
            try {
                List<File> files = new ArrayList<>();
                for (Audio audio : audios) {
                    File file = new File(Uri.parse(audio.uri).getPath());
                    files.add(file);
                }

                for (File file : files) {
                    if (file.exists()) {
                        file.delete();
                    }
                }
                NoteWithTags note = getNote().getValue();
                if (note != null) {
                    List<Audio> allPhotos = note.audios;
                    allPhotos.removeAll(audios);
                }

                database.deleteAudio(audios.toArray(new Audio[]{}));

            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        });
    }

    private final MutableLiveData<Boolean> photoRemoved = new MutableLiveData<>(false);

    private final MutableLiveData<Boolean> audioRemoved = new MutableLiveData<>(false);

    public final LiveData<Boolean> getPhotoRemovedState() {
        return photoRemoved;
    }

    public final LiveData<Boolean> getAudioRemovedState() {
        return audioRemoved;
    }

    public void doneHandlingPhotoRemove() {
        photoRemoved.postValue(false);
    }

    public void doneHandlingAudioRemove() {
        audioRemoved.postValue(false);
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
