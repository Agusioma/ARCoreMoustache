package com.example.moustache;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.core.AugmentedFace;
import com.google.ar.core.Frame;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.Texture;
import com.google.ar.sceneform.ux.AugmentedFaceNode;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "ARMOUSTACHE";
    private Texture texture;
    private boolean isAdded = false;
    private final HashMap<AugmentedFace, AugmentedFaceNode> faceNodeMap = new HashMap<>();

    CustomArFragment customArFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        customArFragment = (CustomArFragment) getSupportFragmentManager().findFragmentById(R.id.arFragment);

        //checking that the fragment is not null
        assert customArFragment != null;

        customArFragment.getArSceneView().setCameraStreamRenderPriority(Renderable.RENDER_PRIORITY_FIRST);
        customArFragment.getArSceneView().getScene().addOnUpdateListener(frameTime -> {
            if (texture == null) {
                return;
            }
            Frame frame = customArFragment.getArSceneView().getArFrame();
            assert frame != null;
            Collection<AugmentedFace> augmentedFaces = frame.getUpdatedTrackables(AugmentedFace.class);

            for (AugmentedFace augmentedFace : augmentedFaces) {
                if (isAdded) return;

                AugmentedFaceNode augmentedFaceMode = new AugmentedFaceNode(augmentedFace);
                augmentedFaceMode.setParent(customArFragment.getArSceneView().getScene());
                augmentedFaceMode.setFaceMeshTexture(texture);
                faceNodeMap.put(augmentedFace, augmentedFaceMode);
                isAdded = true;

                // Remove any AugmentedFaceNodes associated with an AugmentedFace once the stracking has stopped.
                Iterator<Map.Entry<AugmentedFace, AugmentedFaceNode>> iterator = faceNodeMap.entrySet().iterator();
                Map.Entry<AugmentedFace, AugmentedFaceNode> entry = iterator.next();
                AugmentedFace face = entry.getKey();
                while (face.getTrackingState() == TrackingState.STOPPED) {
                    AugmentedFaceNode node = entry.getValue();
                    node.setParent(null);
                    iterator.remove();
                }
            }
        });

        ImageButton btn1 = findViewById(R.id.btn1);
        btn1.setOnClickListener(this::updateMoustache);

        ImageButton btn2 = findViewById(R.id.btn2);
        btn2.setOnClickListener(this::updateMoustache);

        ImageButton btn3 = findViewById(R.id.btn3);
        btn3.setOnClickListener(this::updateMoustache);

        ImageButton btn4 = findViewById(R.id.btn4);
        btn4.setOnClickListener(this::updateMoustache);
    }


    //method for adding the moustache texture to the image
    private void pasteTexture(int drawable) {
        Texture.builder()
                .setSource(this, drawable)
                .build()
                .thenAccept(textureModel -> this.texture = textureModel)
                .exceptionally(throwable -> {
                    Toast.makeText(this, "cannot load texture", Toast.LENGTH_SHORT).show();
                    return null;
                });
    }

    //method for updating the moustache
    public void updateMoustache(View view) {
        Log.d(TAG, "onClickRecord");
        switch (view.getId()) {
            case R.id.btn1: {
                pasteTexture(R.drawable.moustache_two);
                break;
            }
            case R.id.btn2: {
                pasteTexture(R.drawable.moustache_three);
                break;
            }
            case R.id.btn3: {
                pasteTexture(R.drawable.moustache_four);
                break;
            }
            case R.id.btn4: {
                pasteTexture(R.drawable.moustache_five);
                break;
            }
        }
    }
}