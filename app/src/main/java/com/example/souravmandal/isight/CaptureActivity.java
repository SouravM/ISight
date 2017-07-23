package com.example.souravmandal.isight;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.content.ContentValues.TAG;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

/**
 * Created by Sourav Mandal on 7/20/2017.
 */

public class CaptureActivity extends Activity {
    Vision vision;
    TextToSpeech ttobj;
    int TAKE_PHOTO_CODE = 0;
    public static int count = 0;
    Uri outputFileUri;
    boolean found = false;


    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    private Camera mCamera;
    private CameraPreview mPreview;

    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        //startActivity(intent);

        setContentView(R.layout.search_surr);

        ttobj = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    ttobj.setLanguage(Locale.UK);

                    ttobj.speak("Your device is ready to capture your surroundings.", TextToSpeech.QUEUE_FLUSH, null);
                    ttobj.speak("Press the volume down key to capture an image.Make sure your phone is steady!",TextToSpeech.QUEUE_ADD,null);

                    Log.e("ss", "initalizing");
                }
            }
        });
        // Create an instance of Camera
        mCamera = getCameraInstance();

        mCamera.setDisplayOrientation(90);

        Camera.Parameters params = mCamera.getParameters();
        if (params.getSupportedFocusModes().contains(
                Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        mCamera.setParameters(params);
        params.setPictureSize(1600, 1200);
        params.setPictureFormat(PixelFormat.JPEG);
        params.setJpegQuality(100);
        mCamera.setParameters(params);

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);

        /*
        final String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Isight/";
        Log.e("dir",dir);
        File newdir = new File(dir);
        newdir.mkdirs();

        count++;
        String file = dir+count+".jpg";
        File newfile = new File(file);
        try {
            newfile.createNewFile();
        }
        catch (IOException e)
        {
        }

        outputFileUri = Uri.fromFile(newfile);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);

        startActivityForResult(cameraIntent, TAKE_PHOTO_CODE);

        //uploadPhoto(outputFileUri);
          */

    }

    public void callGoogleCloud(final Bitmap bitmap){

        Vision.Builder visionBuilder = new Vision.Builder(
                new NetHttpTransport(),
                new AndroidJsonFactory(),
                null);

        visionBuilder.setVisionRequestInitializer(
                new VisionRequestInitializer("AIzaSyDmMkx8ihFQOpuMho17uu5bJMsQDxuSsZI"));


        vision = visionBuilder.build();


        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                try {

                    // Add the image
                    //Image base64EncodedImage = new Image();
                    // Convert the bitmap to a JPEG
                    // Just in case it's a format that Android understands but Cloud Vision
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
                    byte[] imageBytes = byteArrayOutputStream.toByteArray();

                    Image inputImage = new Image();
                    inputImage.encodeContent(imageBytes);

                    Feature desiredFeature = new Feature();
                    desiredFeature.setType("LABEL_DETECTION");


                    AnnotateImageRequest request = new AnnotateImageRequest();
                    request.setImage(inputImage);
                    request.setFeatures(Arrays.asList(desiredFeature));

                    BatchAnnotateImagesRequest batchRequest =
                            new BatchAnnotateImagesRequest();

                    batchRequest.setRequests(Arrays.asList(request));

                    BatchAnnotateImagesResponse batchResponse =
                            vision.images().annotate(batchRequest).execute();

                    List<EntityAnnotation> faces = batchResponse.getResponses()
                            .get(0).getLabelAnnotations();

                    int labels = faces.size();


                    for(int i = 0 ; i < labels ; i++)
                    {
                        Log.e("ss",faces.get(i).getDescription());
                        Log.e("ss",Float.toString(faces.get(i).getScore()));
                        if(faces.get(i).getDescription().equals("automated teller machine") && faces.get(i).getScore()> 0.5)
                        {
                            Log.e("ss","entered");
                            found = true;
                            // Display toast on UI thread
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    ttobj = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                                        @Override
                                        public void onInit(int status) {
                                            if (status == TextToSpeech.SUCCESS) {
                                                ttobj.setLanguage(Locale.UK);

                                                ttobj.speak("A T M found in this direction. Please go ahead.", TextToSpeech.QUEUE_FLUSH, null);
                                                Log.e("ss","qq");
                                            }
                                        }


                                    });

                                }
                            });

                            Intent inte = new Intent(getApplicationContext(),FinalActivity.class);
                            startActivity(inte);
                        }

                    }

                    if(!found)
                    {
                        ttobj = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                            @Override
                            public void onInit(int status) {
                                if (status == TextToSpeech.SUCCESS) {
                                    ttobj.setLanguage(Locale.UK);

                                    ttobj.speak("A T M not found in this direction. Please turn to your right and repeat the sequence.", TextToSpeech.QUEUE_FLUSH, null);
                                    Log.e("ss","right");
                                }
                            }
                        });

                        mCamera.startPreview();
                    }

                    //Log.e("ss",batchResponse.toString());


                }
                catch(Exception e)
                {

                }
                // More code here
            }
        });


    }
/*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == TAKE_PHOTO_CODE && resultCode == RESULT_OK) {
            Log.d("CameraDemo", "Pic saved");
            //uploadPhoto(outputFileUri);
        }
    }
*/

    public void uploadPhoto(Uri uri) {
        if (uri != null) {
            try {

                Bitmap bp = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                //Bitmap bp = BitmapFactory.decodeFile(uri.toString());
                Bitmap resized = Bitmap.createScaledBitmap(bp,(int)(bp.getWidth()*0.7), (int)(bp.getHeight()*0.7), true);

                Bitmap final_src = rotateImage(resized);


                //imageView.setImageBitmap(resized);
                Log.e("uri",uri.toString());
                //bitmap = scaleBitmapDown(bitmap,1200);
                callGoogleCloud(final_src);


            } catch (IOException e) {
                Log.d(TAG, "Image picking failed because " + e.getMessage());

            }
        } else {
            Log.d(TAG, "Image picker gave us a null image.");

        }
    }


    public Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }


    @Override
    protected void onPause() {
        super.onPause();
        //releaseMediaRecorder();       // if you are using MediaRecorder, release it first
        releaseCamera();              // release the camera immediately on pause event
    }

    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }


    public static Bitmap rotateImage(Bitmap bitmapSrc) {

        Matrix matrix = new Matrix();
        matrix.postRotate(-90);
        return Bitmap.createBitmap(bitmapSrc, 0, 0,
                bitmapSrc.getWidth(), bitmapSrc.getHeight(), matrix, true);
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            outputFileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
            if (pictureFile == null){
                Log.e("s","picturefilenull");
                return;
            }
            mCamera.stopPreview();
            try {


                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();



                //do process
                uploadPhoto(outputFileUri);

            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //return super.onKeyDown(keyCode, event);

        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {

            mCamera.takePicture(null, null, mPicture);
            ttobj = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status == TextToSpeech.SUCCESS) {
                        ttobj.setLanguage(Locale.UK);

                        ttobj.speak("Analysing the image and retrieving data points.", TextToSpeech.QUEUE_FLUSH, null);
                        Log.e("ss","right");
                    }
                }
            });


        }
        return true;
    }

    /** Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }




}
