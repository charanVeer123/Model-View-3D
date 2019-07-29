package com.dmitrybrant.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.dmitrybrant.modelviewer.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;




public class AndroidSurfaceviewExample extends AppCompatActivity implements SurfaceHolder.Callback, View.OnTouchListener {

    TextView testView;

    Camera camera;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    LinearLayout linearLayout;

    PictureCallback rawCallback;
    ShutterCallback shutterCallback;

    PictureCallback jpegCallback;

    String EXTRA_CAMERA_DATA = "camera_data";

    Button btAccept;


    String type="";

    public static int touchCount = 0;

    /** Called when the activity is first created. */

    int windowwidth;
    int windowheight;

    ArrayList<Integer> xyValueList;

    private Paint paint ;



    MaterialDialog dialog = null;

    ImageView imageViewtop,imageViewBottom;

    boolean topTouch = false;
    boolean downTouch = false;
    View horizTopLine1,horizTopLine2;

    View bottomLine1,bottomLine2;
    LinearLayout linearLayout2,llLinear;



    public static void runJustBeforeBeingDrawn(final View view, final Runnable runnable) {
        final ViewTreeObserver.OnPreDrawListener preDrawListener = new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                view.getViewTreeObserver().removeOnPreDrawListener(this);
                runnable.run();
                return true;
            }
        };
        view.getViewTreeObserver().addOnPreDrawListener(preDrawListener); }





    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);


        ActivityCompat.requestPermissions(AndroidSurfaceviewExample.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA},1);
        if(getIntent()!=null)
        {
            if(getIntent().hasExtra("type"))
            {
                type = getIntent().getStringExtra("type");

            }

        }
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);



        setContentView(R.layout.activity_main2);


        xyValueList = new ArrayList<>(4);

        windowwidth = getWindowManager().getDefaultDisplay().getWidth();
        windowheight = getWindowManager().getDefaultDisplay().getHeight();


        surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        linearLayout = (LinearLayout) findViewById(R.id.llLinear);

        horizTopLine1 =  findViewById(R.id.horizTopLine1);
        horizTopLine2 =  findViewById(R.id.horizTopLine2);


        bottomLine1=  findViewById(R.id.bottomline1);
        bottomLine2 =  findViewById(R.id.bottomline2);




        linearLayout2 =  findViewById(R.id.linearLayout2);
        llLinear =  findViewById(R.id.llLinear);


        ViewTreeObserver viewTreeObserver = surfaceView.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

                @Override
                public void onGlobalLayout() {


                    surfaceView.getViewTreeObserver().removeOnGlobalLayoutListener(this);


                    int   viewWidth = surfaceView.getWidth();

                    int   viewHeight = surfaceView.getHeight();




                    System.out.println("AndroidSurfaceviewExample.onGlobalLayout - viewWidth- -" + viewWidth);
                    System.out.println("AndroidSurfaceviewExample.onGlobalLayout - viewHeight- -" + viewHeight);
                    Toast.makeText(AndroidSurfaceviewExample.this, "viewWidth " +viewWidth, Toast.LENGTH_SHORT).show();
                    Toast.makeText(AndroidSurfaceviewExample.this, "viewHeight "+viewHeight, Toast.LENGTH_SHORT).show();

                    float yCord = (float) (0.1*viewWidth);
                    float xCord = (float) (0.05*viewHeight);

                    float yCord1 = (float) (0.9*viewWidth);
                    float xCord1 = (float) (0.95*viewHeight);


//                    int x = horizTopLine1.getLeft();
//                    int y = horizTopLine2.getRight();
////                    horizTopLine2.setVisibility(View.VISIBLE);
//
//                    System.out.println("AndroidSurfaceviewExample.onGlobalLayout - - -Cases " + x);
//                    System.out.println("AndroidSurfaceviewExample.onGlobalLayout - - - ddhh " + y);
//
//                    float zz = x + xCord;
//                    float yy = y + yCord;
//
//                    horizTopLine2.setTranslationX(zz);
//                    horizTopLine2.setVisibility(View.VISIBLE);
//
//                    System.out.println("AndroidSurfaceviewExample.onGlobalLayout - - -check " + zz);
//                    System.out.println("AndroidSurfaceviewExample.onGlobalLayout - - -check2222 " + yy);




                    horizTopLine2.setTranslationX(xCord);


                    bottomLine2.setTranslationY(yCord);
                    bottomLine2.refreshDrawableState();
//                  horizTopLine2.setTranslationY(yCord);






                    System.out.println("AndroidSurfaceviewExample.onGlobalLayout - -  Testing " + xCord);
                    System.out.println("AndroidSurfaceviewExample.onGlobalLayout - -  Testing222 " + yCord);

                    Toast.makeText(AndroidSurfaceviewExample.this, "xCord " +xCord, Toast.LENGTH_SHORT).show();
                    Toast.makeText(AndroidSurfaceviewExample.this, "yCord "+yCord, Toast.LENGTH_SHORT).show();

//                    horizTopLine2.setX(xCord);
//                    horizTopLine2.setY(yCord);

//                    horizTopLine2.setX(xCord);
//                    horizTopLine2.setY(yCord);
//                    horizTopLine2.setTranslationY(yCord);
                    horizTopLine2.setVisibility(View.VISIBLE);



                    // horizTopLine1.setX(xCord);
                    // horizTopLine1.setY(yCord);
//
//                    horizTopLine2.setX(xCord);
//                    horizTopLine2.setY(yCord);
////
//                    horizTopLine1.refreshDrawableState();
                    horizTopLine2.refreshDrawableState();

//                    linearLayout2.setX(xCord);
                    // linearLayout2.setY(yCord);
//                    horizTopLine2.setVisibility(View.VISIBLE);

                    // horizTopLine1.refreshDrawableState();



                }
            });
        }


        imageViewtop = (ImageView) findViewById(R.id.camera_image_viewtop);
        imageViewBottom = (ImageView) findViewById(R.id.camera_image_viewbottom);

        imageViewtop.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) imageViewtop.getLayoutParams();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        break;
                    case MotionEvent.ACTION_MOVE:
                        topTouch = true;
                        int x_cord = (int) event.getRawX();
                        int y_cord = (int) event.getRawY();

                        if (x_cord > windowwidth) {
                            x_cord = windowwidth;
                        }
                        if (y_cord > windowheight) {
                            y_cord = windowheight;
                        }

                        layoutParams.leftMargin = x_cord - 25;
                        layoutParams.topMargin = y_cord - 75;

                        imageViewtop.setLayoutParams(layoutParams);

                        break;
                    default:
                        break;
                }



                if(imageViewtop.getVisibility()==View.VISIBLE || imageViewBottom.getVisibility()==View.VISIBLE)
                {
                    linearLayout.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            return false;
                        }
                    });
                }


                return true;
            }
        });



        imageViewBottom.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) imageViewBottom.getLayoutParams();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        break;
                    case MotionEvent.ACTION_MOVE:
                        downTouch = true;

                        int x_cord = (int) event.getRawX();
                        int y_cord = (int) event.getRawY();

                        if (x_cord > windowwidth) {
                            x_cord = windowwidth;
                        }
                        if (y_cord > windowheight) {
                            y_cord = windowheight;
                        }


                        layoutParams.leftMargin = x_cord - 25;
                        layoutParams.topMargin = y_cord - 75;

                        imageViewBottom.setLayoutParams(layoutParams);
                        break;
                    default:
                        break;
                }



                if(imageViewtop.getVisibility()==View.VISIBLE || imageViewBottom.getVisibility()==View.VISIBLE)
                {
                    linearLayout.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            return false;
                        }
                    });
                }


                return true;
            }
        });


        btAccept = (Button) findViewById(R.id.btCapture);

        surfaceHolder = surfaceView.getHolder();

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        surfaceHolder.addCallback(this);

        // deprecated setting, but required on Android versions prior to 3.0
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);


        jpegCallback = new PictureCallback() {

            public void onPictureTaken(byte[] data, Camera camera) {

//                if(type.equalsIgnoreCase("back")) {
////                    linearLayout.setOnTouchListener(AndroidSurfaceviewExample.this);
////                    imageViewtop.setVisibility(View.VISIBLE);
////                    imageViewBottom.setVisibility(View.VISIBLE);
//                    showDialogInstruction(file);
//                }

                camera.release();
                btAccept.setText("Done");



//                File file1 = new File("data/data/com.dmitrybrant.modelviewer/test.txt");
//                if (!file1.exists()) {
//                    try {
//                        file1.createNewFile();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }




                FileOutputStream outStream = null;

                try {
                    File file = new File("data/data/com.dmitrybrant.modelviewer"+"/"+ System.currentTimeMillis()+"CameraAppoo.jpg");

                    if(!file.exists())
                    {
                        file.createNewFile();
                    }
                    outStream = new FileOutputStream(String.format("data/data/com.dmitrybrant.modelviewer"+"/"+file.getName()));
                    outStream.write(data);
                    outStream.close();

                    Log.d("Log", "onPictureTaken - wrote bytes: " + data.length);


                    if(type.equalsIgnoreCase("back")) {
//                    linearLayout.setOnTouchListener(AndroidSurfaceviewExample.this);
//                    imageViewtop.setVisibility(View.VISIBLE);
//                    imageViewBottom.setVisibility(View.VISIBLE);
                        showDialogInstruction(file);
                        imageViewtop.setVisibility(View.VISIBLE);
                        imageViewBottom.setVisibility(View.VISIBLE);
                    }



                    btAccept.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if(type.equalsIgnoreCase("back"))
                            {
                                if(topTouch && downTouch)
                                {

                                    Intent returnIntent = new Intent();
                                    returnIntent.putExtra("file",file.getAbsolutePath());

                                    if(xyValueList.isEmpty())
                                    {
                                        Float aFloat1 = imageViewtop.getX();
                                        Float aFloat2 =    imageViewtop.getY();

                                        Float aFloat3 = imageViewBottom.getX();
                                        Float aFloat4 = imageViewBottom.getY();

                                        xyValueList.add(aFloat1.intValue());
                                        xyValueList.add(aFloat2.intValue());
                                        xyValueList.add(aFloat3.intValue());
                                        xyValueList.add(aFloat4.intValue());
                                    }

                                    returnIntent.putExtra("hashmap",xyValueList);
                                    setResult(Activity.RESULT_OK,returnIntent);
                                    finish();
                                }
                                else
                                {

                                    showDialogInstruction(file);

                                }

                            }
                            else
                            {
                                Intent returnIntent = new Intent();
                                returnIntent.putExtra("file",file.getAbsolutePath());
                                if(xyValueList.isEmpty())
                                {

                                    xyValueList.add(0);
                                    xyValueList.add(0);
                                    xyValueList.add(0);
                                    xyValueList.add(0);
                                }
                                returnIntent.putExtra("hashmap",xyValueList);


                                setResult(Activity.RESULT_OK,returnIntent);
                                finish();

                            }





                        }
                    });

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                }

                Toast.makeText(getApplicationContext(), "Picture Saved", Toast.LENGTH_SHORT).show();

            }
        };

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);


    }

    private void showDialogInstruction(File file) {
        if(dialog ==null)
        {
            dialog =  new MaterialDialog.Builder(this)
                    .customView(R.layout.dialogpreview,false)
                    .show();

            Button button = (Button)dialog.getCustomView().findViewById(R.id.button);
//            ImageView img = (ImageView)dialog.getCustomView().findViewById(R.id.imgSample);
//
//
//            BitmapFactory.Options options = new BitmapFactory.Options();
//            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
//
//            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
//            img.setImageBitmap(bitmap);
//            img.setRotation(90F);

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
        }
        else
        {

            dialog.show();
        }


    }


    public void captureImage(View v) throws IOException {
//        if(touchCount<2)
//        {
//            TastyToast.makeText(getApplicationContext(), "Please give two Click", TastyToast.LENGTH_LONG, TastyToast.INFO);
//        }
//        else
//        {
        //take the picture
        if(camera!=null)
            camera.takePicture(null, null, jpegCallback);

        // }



    }



    public void refreshCamera() {

        if (surfaceHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }



        // stop preview before making changes

        try {
            camera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }



        // set preview size and make any resize, rotate or


        // start preview with new settings

        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (Exception e) {



        }

    }



    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {

        // Now that the size is known, set up the camera parameters and begin

        // the preview.

        try {
            refreshCamera();
        } finally {

        }

    }


    public void surfaceCreated(SurfaceHolder holder) {

        try {
            // open the camera
            camera = Camera.open();

        } catch (RuntimeException e) {
            // check for exceptions
            System.err.println(e);
            return;

        }

        Camera.Parameters param;

        param = camera.getParameters();



        // modify parameter

        param.setPreviewSize(352, 288);

        try {
            camera.setParameters(param);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            // The Surface has been created, now tell the camera where to draw
            // the preview.
            setCameraDisplayOrientation(AndroidSurfaceviewExample.this,1,camera);
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (Exception e) {
            // check for exceptions
            System.err.println(e);
            return;
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // stop preview and release camera
        try {
            if(camera!=null) {


                camera.stopPreview();
                camera.release();
                camera = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void setCameraDisplayOrientation(Activity activity,
                                                   int cameraId, android.hardware.Camera camera) {

        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();

        android.hardware.Camera.getCameraInfo(cameraId, info);

        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        touchCount=0;
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if(touchCount<2)
        {
            int x = (int) event.getX();
            int y = (int) event.getY();

            if(touchCount==0)
            {
                imageViewtop.setVisibility(View.VISIBLE);
                imageViewtop.setX(x);
                imageViewtop.setY(y);
                imageViewtop.refreshDrawableState();
            }
            else
            {
                imageViewBottom.setVisibility(View.VISIBLE);
                imageViewBottom.setX(x);
                imageViewBottom.setY(y);
                imageViewBottom.refreshDrawableState();
            }





            xyValueList.add(x);
            xyValueList.add(y);

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.i("TAG", "touched down");
                    break;
                case MotionEvent.ACTION_MOVE:
                    Log.i("TAG", "moving: (" + x + ", " + y + ")");
                    break;
                case MotionEvent.ACTION_UP:
                    Log.i("TAG", "touched up");
                    break;
            }
            ++touchCount;
        }
        else
        {
//            TastyToast.makeText(getApplicationContext(), "Now you can capture Image", TastyToast.LENGTH_LONG, TastyToast.SUCCESS);

        }




        return false;
    }
}
