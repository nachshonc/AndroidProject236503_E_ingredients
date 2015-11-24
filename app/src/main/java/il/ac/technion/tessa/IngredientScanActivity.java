package il.ac.technion.tessa;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.ResultIterator;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.hardware.Camera.*;

public class IngredientScanActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener, FrgIngredientList.OnFragmentInteractionListener {
    static final String DATA_FILES[]={
            "eng.traineddata",
            "heb.traineddata",
            "lord_sandwich.jpg",
            "P1a.jpg",
            "P2a.jpg",
            "P3.jpg"/*,
            "pic1.jpg",
            "pic2.jpg",
            "pic3.jpg",
            "pic4.jpg",
            "pic5.jpg",
            "pic6.jpg",
            "test1.png",
            "test2.png",
            "test3.png",
            "test4.png",
            "e-num-ingred.jpg",*/
            ,"w2-sandwich-ingredients.jpg"/*,
            "salad-dressing.jpg",
            "coconut-milk.jpg"*/
    };

    static final String ALWAYS_COPY_DATA_FILES[] = {
            "IngredientList.txt"
    };

//    static String TEST_FILE=DATA_FILES[DATA_FILES.length-1];
    static String TEST_FILE="P3.jpg"; // "lord_sandwich.jpg"; //null; //"bread.jpg";

    Bitmap origImage, binarizedImage;
//    Preview preview;
    int thresholdValue=80;
    boolean enableBinarize=true, enableGrayscale=true;
    Camera camera;
    public static final String DATA_PATH = Environment
            .getExternalStorageDirectory().toString() + "/E_Ingredients/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingredient_scan);

        loadTrainDataFile();
        IngredientDB.loadDB(); //do we need to loadDB in a background thread? Currently doesn't seem so..
//        preview = new Preview(this, this);

//        ((FrameLayout)findViewById(R.id.preview)).addView(preview);
    }


    String mCurrentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp;
        File storageDir = new File(DATA_PATH+"tessdata");
//        File image = new File(DATA_PATH+"tessdata/" + imageFileName + ".jpg");
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
//        mCurrentPhotoPath = DATA_PATH+"tessdata/" + imageFileName + ".jpg";
        return image;
    }
    static final int REQUEST_IMAGE_CAPTURE = 1;

    static final int REQUEST_TAKE_PHOTO = 1;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.d("TakePic", "Error while creating the image file "+mCurrentPhotoPath+"\n"+ex.toString());
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                savePreferences();
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private void savePreferences(){
        // We need an Editor object to make preference changes.
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        editor.putString("mCurrentPhotoPath", mCurrentPhotoPath);

        // Commit the edits!
        editor.commit();
    }

    private void restorePreferences() {
        SharedPreferences settings = getPreferences(MODE_PRIVATE);
        mCurrentPhotoPath = settings.getString("mCurrentPhotoPath", "");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            if (mCurrentPhotoPath == null)
                restorePreferences();

            Toast.makeText(getApplicationContext(),"Grabbing image from "+mCurrentPhotoPath, Toast.LENGTH_LONG).show();
            setPic();
//            origImage = BitmapFactory.decodeFile(mCurrentPhotoPath);
//
//            ImageView iv = (ImageView) findViewById(R.id.origImage);
//            iv.setImageBitmap(origImage);

        }
    }

    public void snap(View v) {
        if (origImage != null)
            origImage.recycle();
        origImage = null;
        if (binarizedImage != null)
            binarizedImage.recycle();
        binarizedImage = null;
        System.gc();

        if (TEST_FILE != null) {
            mCurrentPhotoPath = DATA_PATH+"/tessdata/"+TEST_FILE;
            setPic();
//            origImage = BitmapFactory.decodeFile(DATA_PATH+"/tessdata/"+TEST_FILE);
//            ImageView iv = (ImageView) findViewById(R.id.origImage);
//            iv.setImageBitmap(origImage);
        } else {
            dispatchTakePictureIntent();
        }
//        preview.mCamera.takePicture(null, null, jpegCallback);

    }


    private void setPic() {
        /*
        // Get the dimensions of the View
        Toast.makeText(getApplicationContext(),"setPic 1", Toast.LENGTH_SHORT).show();
        int targetW = 1024;

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        Toast.makeText(getApplicationContext(),"setPic 2", Toast.LENGTH_SHORT).show();
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        Toast.makeText(getApplicationContext(),"setPic 3", Toast.LENGTH_SHORT).show();
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        Toast.makeText(getApplicationContext(),"setPic 4", Toast.LENGTH_SHORT).show();
        int scaleFactor=1;
        if (photoW >= 2*targetW)
            scaleFactor = photoW/targetW;
        Log.d("ScaleFactor", ""+scaleFactor);
        Toast.makeText(getApplicationContext(),"ScaleFactor =  "+scaleFactor, Toast.LENGTH_SHORT).show();

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Toast.makeText(getApplicationContext(),"Decoding image from "+mCurrentPhotoPath, Toast.LENGTH_SHORT).show();
        */
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;

        origImage = BitmapFactory.decodeFile(mCurrentPhotoPath, options);
        if (origImage == null) {
            Toast.makeText(getApplicationContext(),"origImage is null", Toast.LENGTH_LONG).show();
            return;
        }
        ImageView iv = (ImageView) findViewById(R.id.origImage);

        if (iv != null)
            iv.setImageBitmap(origImage);

        analyze(null);
    }

    @Override
    public void onFragmentInteraction(String id) {

    }

    /*
    ShutterCallback shutterCallback = new ShutterCallback() {
        public void onShutter() {
            Log.d(TAG, "onShutter'd");
        }
    };


    static final String TAG="Tessa";
    /** Handles data for raw picture *
    PictureCallback rawCallback = new PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d(TAG, "onPictureTaken - raw");
        }
    };

    /** Handles data for jpeg picture *
    PictureCallback jpegCallback = new PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            FileOutputStream outStream = null;
            long time = 0;
            try {
                // write to local sandbox file system
//                outStream = CameraDemo.this.openFileOutput(String.format("%d.jpg", System.currentTimeMillis()), 0);
                // Or write to sdcard
                time =  System.currentTimeMillis();
                outStream = new FileOutputStream(String.format(DATA_PATH + "/tessdata/IMG_%d.jpg",time));
                outStream.write(data);
                outStream.close();
                Log.d(TAG, "onPictureTaken - wrote bytes: " + data.length);

                ImageView iv = (ImageView) findViewById(R.id.origImage);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                origImage = BitmapFactory.decodeFile(DATA_PATH + "/tessdata/IMG_" + time+".jpg", options);
                iv.setImageBitmap(origImage);
                iv.refreshDrawableState();
                camera.startPreview();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {



            }
            Log.d(TAG, "onPictureTaken - jpeg");
        }
    };
    */


    private class Analyzer extends AsyncTask<Bitmap, Void, String> {

        private ProgressDialog dialog = new ProgressDialog(IngredientScanActivity.this);
        private ArrayList<String> list = new ArrayList<>();

        private void addIngredient(String ingredient){
            list.add(ingredient);
        }
        @Override
        protected String doInBackground(Bitmap... params) {
            TessBaseAPI baseApi = new TessBaseAPI();

            baseApi.setDebug(false);
            baseApi.init(DATA_PATH, "eng+heb");
//        baseApi.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "E0123456789,()ai-");

            baseApi.setImage(params[0]);
//        baseApi.setImage(b);
            String recognizedText = baseApi.getUTF8Text();

            StringBuffer result = new StringBuffer();
            final ResultIterator iterator = baseApi.getResultIterator();
            iterator.begin(); //crashes my app
            do {
                String word = iterator.getUTF8Text(TessBaseAPI.PageIteratorLevel.RIL_WORD);
                if(word==null) break;
                Log.d("word", "#" + word + "#");
                if (word.matches(".*\\([E£5₪]\\d\\d\\d[a-i]*\\).*")) {
                    String toAdd = word.replaceAll(".*\\([E£5₪](\\d\\d\\d[a-i]*)\\).*", "E$1");
                    Log.d("toAdd",toAdd);
                    addIngredient(toAdd); //result.append(word.replaceAll(".*\\([E£5](\\d\\d\\d[a-i]*)\\).*", "E$1")).append("\n");
                } else {
                    String[] words = word.split(",");
                    for (int i=0; i < words.length; i++) {
                        Log.d("words","%"+words[i]+"%");
                        if (words[i].matches("^[^E£5₪]*[E£5₪]-?[\\doOS][\\doOS][\\doOS][a-i]*([^0-9a-zA-Z].*|)$")) {
                            String toAdd = words[i].replaceAll("^[^E£5₪]*[E5£₪]-?([\\doOS][\\doOS][\\doOS][a-i]*).*", "E$1").replaceAll("[oO]", "0").replaceAll("S","5");
                            Log.d("toAdd",toAdd);
                            addIngredient(toAdd);
                        }
                        //result.append(words[i].replaceAll(".*[E£]-?([\\doO][\\doO][\\doO][a-i]*).*", "E$1").replaceAll("[oO]", "0")).append("\n");
                    }
                }
            } while (iterator.next(TessBaseAPI.PageIteratorLevel.RIL_WORD));

            String res = result.toString();
            baseApi.end();
            Log.d("word", "Analysis complete");
            return res;
        }

        @Override
        protected void onPreExecute() {
//            TextView tv = (TextView) findViewById(R.id.result);
//            tv.setText("Analyzing...");
            this.dialog.setMessage("Please wait");
            this.dialog.show();
        }

        @Override
        protected void onPostExecute(String result) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
//            TextView tv = (TextView) findViewById(R.id.result);
            if (list.isEmpty()) {
//                tv.setText("No match found");
            } else {
                ListView listView = (ListView) findViewById(R.id.frag_list);
                ArrayList<ModelIngredient> models = new ArrayList<>();
                for(int i=0; i<list.size(); ++i) {
                    ModelIngredient ingredient = IngredientDB.getIngredient(list.get(i));
                    if(ingredient==null)
                        ingredient=new ModelIngredient(list.get(i), "Unknown additive", true, false, false);
                    models.add(ingredient);
                }

                listView.setAdapter(new AdapterIngredientList(listView.getContext(), models));
//                if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
//                    FragmentManager fragmentManager=getFragmentManager();
//                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//                    FrgIngredientList fragment = FrgIngredientList.newInstance(list);//DEBUGItemFragment.newInstance("param1", "param2"); //
//                    fragmentTransaction.replace(R.id.frag_list, fragment);
//                    fragmentTransaction.commit();

//                }
//                else {
//                    Intent i = new Intent(IngredientScanActivity.this, ActIngredientList.class);
//                    i.putStringArrayListExtra(ActIngredientList.PARAM_INGREDIENTS, list);
//                    startActivity(i);
//                }
            }
            /*if (result.equals(""))
                result = "No match found";
            tv.setText(result);*/
        }
    }

    public void analyze(View v) {
        if (origImage == null)
        {
            //FOR DEBUG
            ArrayList<String> list=new ArrayList<>(4);
            list.add("E200"); list.add("E101"); list.add("E220"); list.add("E244");
            Intent i = new Intent(IngredientScanActivity.this, ActIngredientList.class);
            i.putStringArrayListExtra(ActIngredientList.PARAM_INGREDIENTS, list);
            startActivity(i);
            return;
        }

        grayscale(v);
        binarize(v);
        if (binarizedImage == null)
            binarizedImage = origImage;
        new Analyzer().execute(binarizedImage);
    }

    public void grayscale(View v) {
        if (enableGrayscale==false || origImage == null)
            return;
        Bitmap grayscaleImage = toGrayscale(origImage);
        origImage.recycle();
        origImage = null;
        System.gc();
        origImage = grayscaleImage;
        ImageView iv = (ImageView) findViewById(R.id.origImage);
        iv.setImageBitmap(grayscaleImage);
    }

    public void binarize(View v) {
        if (enableBinarize==false || origImage == null)
            return;
        if (binarizedImage != null)
            binarizedImage.recycle();
        binarizedImage = null;
        System.gc();
        binarizedImage = lowPassFilter(origImage);
        ImageView iv = (ImageView) findViewById(R.id.origImage);
        iv.setImageBitmap(binarizedImage);
    }

    public Bitmap toGrayscale(Bitmap bmpOriginal)
    {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    public Bitmap lowPassFilter(Bitmap bmpOriginal)
    {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        //int thresholdValue = threshold.getProgress();
        float scale = (float)thresholdValue*(float)5.0/(float)256;
        cm.setScale(scale, scale, scale, 1);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ingredient_scan, menu);
        return true;
    }

    private void imageProccessSettings(){
        Log.d("settings", "called");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Please set the enhancement factor (0--100)");

// Set up the input
        final EditText input = new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);
        final CheckBox box = new CheckBox(this);
        builder.setView(box);

// Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    int val = Integer.parseInt(input.getText().toString());
                    thresholdValue = val;
                } catch (NumberFormatException exception) {
                }
                ;

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            imageProccessSettings();
            return true;
        }
        if (id == R.id.action_grayscale){
            enableGrayscale=!enableGrayscale;
            item.setTitle((enableGrayscale ? getString(R.string.str_dis_gray) : getString(R.string.str_en_gray)));
            if(enableGrayscale && origImage!=null)
                grayscale(null);
        }
        if (id == R.id.action_enhance){
            enableBinarize=!enableBinarize;
            item.setTitle((enableBinarize?getString(R.string.str_dis_bin):getString(R.string.str_en_bin)));
            if(enableBinarize && origImage!=null)
                binarize(null);
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Load the train data file for the OCR algorithm.
     * If the file already on the storage - does nothing.
     */
    private void loadTrainDataFile() {
        String[] paths = new String[]{DATA_PATH, DATA_PATH + "tessdata/"};
        for (String p : paths) {
            File dir = new File(p);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Log.v("DATAPATH", "ERROR: Creation of directory " + p + " on sdcard failed");
                    return;
                } else {
                    Log.v("DATAPATH", "Created directory " + p + " on sdcard");
                }
            }
        }

        copyFiles();
    }


    void copyFiles() {
        for (int i=0; i < DATA_FILES.length; i++)
            copyFile(DATA_FILES[i], false);
        for (int i=0; i < ALWAYS_COPY_DATA_FILES.length; i++)
            copyFile(ALWAYS_COPY_DATA_FILES[i], true);
    }

    void copyFile(String filename, boolean alwaysCopy) {
        if (alwaysCopy || !(new File(DATA_PATH + "tessdata/"+filename))
                .exists()) {
            try {

                AssetManager assetManager = getAssets();
                InputStream in = assetManager.open(filename);
                // GZIPInputStream gin = new GZIPInputStream(in);
                OutputStream out = new FileOutputStream(DATA_PATH
                        + "tessdata/"+filename);

                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                // while ((lenf = gin.read(buff)) > 0) {
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                // gin.close();
                out.close();

                Log.d("DATAPATH", "Copied "+filename);
            } catch (IOException e) {
                Log.d("DATAPATH",
                        "Was unable to copy " + filename + " "
                                + e.toString());
            }
        }
    }

    /*
    private boolean safeCameraOpen(int id) {
        boolean qOpened = false;

        try {
            releaseCameraAndPreview();
            camera = Camera.open(id);
            qOpened = (camera != null);
        } catch (Exception e) {
            Log.e(getString(R.string.app_name), "failed to open Camera");
            e.printStackTrace();
        }

        return qOpened;
    }

    private void releaseCameraAndPreview() {
        preview.setCamera(null);
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }
    */

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    /*
    @Override
    public void onPause() {
        super.onPause();
        releaseCameraAndPreview();
    }

    @Override
    public void onResume() {
        super.onResume();
        safeCameraOpen(0);
        preview.setCamera(camera);
 //       preview.mCamera = camera;
    }
    */


}

class Preview extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "Preview";

    SurfaceHolder mHolder;
    public Camera mCamera;
    List<Camera.Size> mSupportedPreviewSizes;
    Activity mActivity;

    Preview(Context context, Activity activity) {
        super(context);

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mActivity = activity;
    }

    public void setCamera(Camera camera) {
        if (mCamera == camera) { return; }

        stopPreviewAndFreeCamera();

        mCamera = camera;

        if (mCamera != null) {
            List<Camera.Size> localSizes = mCamera.getParameters().getSupportedPreviewSizes();
            mSupportedPreviewSizes = localSizes;
            requestLayout();

            try {
                mCamera.setPreviewDisplay(mHolder);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Important: Call startPreview() to start updating the preview
            // surface. Preview must be started before you can take a picture.
            mCamera.startPreview();
        }
    }

    /**
     * When this function returns, mCamera will be null.
     */
    private void stopPreviewAndFreeCamera() {

        if (mCamera != null) {
            // Call stopPreview() to stop updating the preview surface.
            mCamera.stopPreview();

            // Important: Call release() to release the camera for use by other
            // applications. Applications should release the camera immediately
            // during onPause() and re-open() it during onResume()).
            mCamera.release();

            mCamera = null;
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, acquire the mCamera and tell it where
        // to draw.
        if (mCamera == null)
            mCamera = open();
        try {
            mCamera.setPreviewDisplay(holder);


            mCamera.setPreviewCallback(new PreviewCallback() {

                public void onPreviewFrame(byte[] data, Camera arg1) {
                    /*
                    FileOutputStream outStream = null;
                    try {
                        outStream = new FileOutputStream(String.format("/sdcard/%d.jpg", System.currentTimeMillis()));
                        outStream.write(data);
                        outStream.close();
                        Log.d(TAG, "onPreviewFrame - wrote bytes: " + data.length);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                    }*/
                    Preview.this.invalidate();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return, so stop the preview.
        // Because the CameraDevice object is not a shared resource, it's very
        // important to release it when the activity is paused.
        if (mCamera != null){
            mCamera.stopPreview();
        }
        mCamera = null;
    }

    public static void setCameraDisplayOrientation(Activity activity,
                                                   int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
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


    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // Now that the size is known, set up the mCamera parameters and begin
        // the preview.
        Camera.Parameters parameters = mCamera.getParameters();
//        parameters.setPreviewSize(w, h);
//        parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
        requestLayout();
        mCamera.setParameters(parameters);
        setCameraDisplayOrientation(mActivity, 0, mCamera);

        // Important: Call startPreview() to start updating the preview surface.
        // Preview must be started before you can take a picture.
        mCamera.startPreview();

    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        Paint p= new Paint(Color.RED);
        Log.d(TAG,"draw");
        canvas.drawText("PREVIEW", canvas.getWidth()/2, canvas.getHeight()/2, p );
    }
}
