/*
 * Copyright 2018 Google LLC. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.ncf.ar.araquarium;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

/**
 * This is an example activity that uses the Sceneform UX package to make common AR tasks easier.
 */
public class AquariumActivity extends AppCompatActivity {
  private static final String TAG = AquariumActivity.class.getSimpleName();
  private static final double MIN_OPENGL_VERSION = 3.0;

  private ArFragment arFragment;
  private ModelRenderable andyRenderable;

  @Override
  @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
  // CompletableFuture requires api level 24
  // FutureReturnValueIgnored is not valid
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (!checkIsSupportedDeviceOrFinish(this)) {
      return;
    }

    setContentView(R.layout.activity_aquarium);
    arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.sceneform_fragment);
    initializeGallery();
    // When you build a Renderable, Sceneform loads its resources in the background while returning
    // a CompletableFuture. Call thenAccept(), handle(), or check isDone() before calling get().
//    ModelRenderable.builder()
//        .setSource(this, Uri.parse("LampPost.sfb"))
//        .build()
//        .thenAccept(renderable -> andyRenderable = renderable)
//        .exceptionally(
//            throwable -> {
//              Toast toast =
//                  Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG);
//              toast.setGravity(Gravity.CENTER, 0, 0);
//              toast.show();
//              return null;
//            });
    arFragment.setOnTapArPlaneListener(
        (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
          if (andyRenderable == null) {
            return;
          }

          // Create the Anchor.
          Anchor anchor = hitResult.createAnchor();
          AnchorNode anchorNode = new AnchorNode(anchor);
          anchorNode.setParent(arFragment.getArSceneView().getScene());

          // Create the transformable andy and add it to the anchor.
          TransformableNode andy = new TransformableNode(arFragment.getTransformationSystem());
          andy.setParent(anchorNode);
          andy.setRenderable(andyRenderable);
          andy.select();
        });

      Button backToIR = (Button) findViewById(R.id.btnBackToIR);
      backToIR.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              startAugmentedImage();
          }
      });

      Button screenShot = (Button) findViewById(R.id.btnScreenShot);
      screenShot.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              takeScreenShot();
          }
      });

  }

      public void takeScreenShot(){
      //This doesn't work, no idea how to do this.
//          if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
//                  PackageManager.PERMISSION_GRANTED){
//              ActivityCompat.requestPermissions(this,
//                      new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 69);
//          }else {
//              View view = findViewById(R.id.sceneform_fragment);
//              Bitmap bm = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
//              Canvas canvas = new Canvas(bm);
//              view.layout(0, 0, view.getLayoutParams().width, view.getLayoutParams().height);
//              view.draw(canvas);
//              try {
//                  Log.d("ScreenShot", "Starting to take screen shot...");
//                  Date date = new Date();
//                  CharSequence currentDate = android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", date);
//                  String imagePath = Environment.getExternalStorageDirectory().toString() + "/Pictures/AR_Aquarium_ScreenShots_" + currentDate + ".jpg";
//                  File imageFile = new File(imagePath);
//                  FileOutputStream outputStream = new FileOutputStream(imageFile);
//                  int quality = 100;
//                  bm.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
//                  outputStream.flush();
//                  outputStream.close();
//                  Log.d("ScreenShot", "Saved to " + imagePath);
//                  Toast.makeText(this, "saved to " + imagePath, Toast.LENGTH_LONG).show();
//              } catch (Throwable e) {
//                  Log.d("ScreenShot", e.toString());
//                  Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
//              }
//          }
      }

    public void startAugmentedImage(){
        Intent augImg = new Intent(this, AugmentedImageActivity.class);
        startActivity(augImg);
    }

  /**
   * Returns false and displays an error message if Sceneform can not run, true if Sceneform can run
   * on this device.
   *
   * <p>Sceneform requires Android N on the device as well as OpenGL 3.0 capabilities.
   *
   * <p>Finishes the activity if Sceneform can not run
   */
  public static boolean checkIsSupportedDeviceOrFinish(final Activity activity) {
    if (Build.VERSION.SDK_INT < VERSION_CODES.N) {
      Log.e(TAG, "Sceneform requires Android N or later");
      Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG).show();
      activity.finish();
      return false;
    }
    String openGlVersionString =
        ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE))
            .getDeviceConfigurationInfo()
            .getGlEsVersion();
    if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
      Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later");
      Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
          .show();
      activity.finish();
      return false;
    }
    return true;
  }

  private void buildObject(String object){
      ModelRenderable.builder()
              .setSource(this, Uri.parse(object))
              .build()
              .thenAccept(renderable -> andyRenderable = renderable)
              .exceptionally(
                      throwable -> {
                          Toast toast =
                                  Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG);
                          toast.setGravity(Gravity.CENTER, 0, 0);
                          toast.show();
                          return null;
                      });

  }

    private void initializeGallery() {
        LinearLayout gallery = findViewById(R.id.gallery_layout);
        ImageView chair = new ImageView(this);
        chair.setImageResource(R.drawable.dory);
        chair.setContentDescription("chair");
        //chair.setOnClickListener(view ->{buildObject("chair.sfb");});
        gallery.addView(chair);

        ImageView lamp = new ImageView(this);
        lamp.setImageResource(R.drawable.dory);
        lamp.setContentDescription("lamp");
        //lamp.setOnClickListener(view ->{buildObject("LampPost.sfb");});
        gallery.addView(lamp);

        ImageView couch = new ImageView(this);
        couch.setImageResource(R.drawable.dory);
        couch.setContentDescription("couch");
        //couch.setOnClickListener(view ->{buildObject("couch.sfb");});
        gallery.addView(couch);

        ImageView bookshelf = new ImageView(this);
        bookshelf.setImageResource(R.drawable.dory);
        bookshelf.setContentDescription("bookshelf");
        //bookshelf.setOnClickListener(view ->{buildObject("Corona Bookcase Set.sfb");});
        gallery.addView(bookshelf);

        ImageView stand = new ImageView(this);
        stand.setImageResource(R.drawable.dory);
        stand.setContentDescription("stand");
        //stand.setOnClickListener(view ->{buildObject("Collier Webb Console.sfb");});
        gallery.addView(stand);
    }
}
