package com.example.moustache;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import com.google.ar.core.Config;
import com.google.ar.core.RecordingConfig;
import com.google.ar.core.RecordingStatus;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.RecordingFailedException;
import com.google.ar.sceneform.ux.ArFragment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumSet;
import java.util.Set;

public class CustomArFragment extends ArFragment {
    private static final String TAG = "ARMOUSTACHE";
    private Session session;

    private final String MP4_VIDEO_MIME_TYPE = "video/mp4";
    @Override
    protected Config getSessionConfiguration(Session session) {
        Config config = new Config(session);
        config.setAugmentedFaceMode(Config.AugmentedFaceMode.MESH3D);
        this.getArSceneView().setupSession(session);
        session.configure(config);
        return config;
    }

    @Override
    protected Set<Session.Feature> getSessionFeatures() {
        return EnumSet.of(Session.Feature.FRONT_CAMERA);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FrameLayout frameLayout = (FrameLayout) super.onCreateView(inflater, container, savedInstanceState);
        getPlaneDiscoveryController().hide();
        getPlaneDiscoveryController().setInstructionView(null);
        return frameLayout;
        //commented this out as I was testing it when try to save the video

        /*try {
            session = new Session(getContext());
            CameraConfigFilter filter = new CameraConfigFilter(session);
            filter.setFacingDirection(CameraConfig.FacingDirection.FRONT);
            List<CameraConfig> cameraConfigList = session.getSupportedCameraConfigs(filter);
            session.setCameraConfig(cameraConfigList.get(0));
        } catch (UnavailableArcoreNotInstalledException e) {
            throw new RuntimeException(e);

        } catch (UnavailableApkTooOldException e) {
            throw new RuntimeException(e);
        } catch (UnavailableSdkTooOldException e) {
            throw new RuntimeException(e);
        } catch (UnavailableDeviceNotCompatibleException e) {
            throw new RuntimeException(e);
        }*/

    }

    //The methods below were to handle the video saving
    boolean startRecording() {
        Uri mp4FileUri = createMp4File();
        if (mp4FileUri == null)
            return false;

        Log.d(TAG, "startRecording at: " + mp4FileUri);

        pauseARCoreSession();

        // Configure the ARCore session to start recording.
        RecordingConfig recordingConfig = new RecordingConfig(session)
                .setMp4DatasetUri(mp4FileUri)
                .setAutoStopOnPause(true);

        try {
            // Prepare the session for recording, but do not start recording yet.
            session.startRecording(recordingConfig);
        } catch (RecordingFailedException e) {
            Log.e("TAG", "startRecording - Failed to prepare to start recording", e);
            return false;
        }

        boolean canResume = resumeARCoreSession();
        if (!canResume)
            return false;

        // Correctness checking: check the ARCore session's RecordingState.
        RecordingStatus recordingStatus = session.getRecordingStatus();
        Log.d("TAG", String.format("startRecording - recordingStatus %s", recordingStatus));
        return recordingStatus == RecordingStatus.OK;
    }
    private void pauseARCoreSession() {
        session.pause();
    }
    private Uri createMp4File() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String mp4FileName = "armoustache-" + dateFormat.format(new Date()) + ".mp4";

        ContentResolver resolver = getContext().getContentResolver();

        Uri videoCollection = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            videoCollection = MediaStore.Video.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL_PRIMARY);
        } else {
            videoCollection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        }

        // Create a new Media file record.
        ContentValues newMp4FileDetails = new ContentValues();
        newMp4FileDetails.put(MediaStore.Video.Media.DISPLAY_NAME, mp4FileName);
        newMp4FileDetails.put(MediaStore.Video.Media.MIME_TYPE, MP4_VIDEO_MIME_TYPE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // The Relative_Path column is only available since API Level 29.
            newMp4FileDetails.put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES);
        } else {
            // Use the Data column to set path for API Level <= 28.
            File mp4FileDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
            String absoluteMp4FilePath = new File(mp4FileDir, mp4FileName).getAbsolutePath();
            newMp4FileDetails.put(MediaStore.Video.Media.DATA, absoluteMp4FilePath);
        }

        Uri newMp4FileUri = resolver.insert(videoCollection, newMp4FileDetails);

        // Ensure that this file exists and can be written.
        if (newMp4FileUri == null) {
            Log.e(TAG, String.format("Failed to save Video in MediaStore. API Level = %d", Build.VERSION.SDK_INT));
            return null;
        }

        // This call ensures the file exist before we pass it to the ARCore API.
        if (!testFileWriteAccess(newMp4FileUri)) {
            return null;
        }

        Log.d(TAG, String.format("createMp4File = %s, API Level = %d", newMp4FileUri, Build.VERSION.SDK_INT));

        return newMp4FileUri;
    }

    // Test if the file represented by the content Uri can be open with write access.
    private boolean testFileWriteAccess(Uri contentUri) {
        try (java.io.OutputStream mp4File = getContext().getContentResolver().openOutputStream(contentUri)) {
            Log.d(TAG, String.format("Success in testFileWriteAccess %s", contentUri.toString()));
            return true;
        } catch (java.io.FileNotFoundException e) {
            Log.e(TAG, String.format("FileNotFoundException in testFileWriteAccess %s", contentUri.toString()), e);
        } catch (java.io.IOException e) {
            Log.e(TAG, String.format("IOException in testFileWriteAccess %s", contentUri.toString()), e);
        }

        return false;
    }
    private boolean resumeARCoreSession() {
        try {
            session.resume();
        } catch (CameraNotAvailableException e) {
            Log.e("TAG", "CameraNotAvailableException in resumeARCoreSession", e);
            return false;
        }
        return true;
    }
}