package il.ac.technion.tessa;

import android.app.Activity;
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
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.RequiresPermission;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.camera.CropImageIntentBuilder;
import com.googlecode.tesseract.android.ResultIterator;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import com.googlecode.leptonica.android.Binarize;
import com.googlecode.leptonica.android.Pix;
import com.googlecode.leptonica.android.ReadFile;
import com.googlecode.leptonica.android.WriteFile;
import com.googlecode.leptonica.android.Constants;
import com.soundcloud.android.crop.Crop;

import static android.hardware.Camera.*;

public class IngredientScanActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener,
            OnItemClickListener {
    static String TEST_FILE=null; //"lord_sandwich.jpg"; //null; //"bread.jpg";
    static final boolean MOCK_OCR = false;
    static final ArrayList<String> MOCK_LIST = new ArrayList<>(Arrays.asList("E100", "E102", "E110", "E121","E151", "E270", "E266"));
    static final String DATA_FILES[]={
            "eng.traineddata",
            "heb.traineddata"/*,
            "lord_sandwich.jpg",
            "P1a.jpg",
            "P2a.jpg",
            "P3.jpg",
            "Q1.jpg",
            "Q2.jpg"
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
            "e-num-ingred.jpg",
            "w2-sandwich-ingredients.jpg",
            "salad-dressing.jpg",
            "coconut-milk.jpg"*/
    };

    static final String ALWAYS_COPY_DATA_FILES[] = {
            "IngredientList.txt",
            "Edb.txt"
    };
    public static void error(String s){Log.d("error", s);}

//    static String TEST_FILE=DATA_FILES[DATA_FILES.length-1];

    Bitmap origImage;
    Pix binarizedPixImage;
//    Preview preview;
    int thresholdValue=80;
    boolean enableBinarize=true, enableGrayscale=true;
    ArrayList<String> ingredientsList = new ArrayList<>();
//    Camera camera;
    EDBHandler dbHandler;
    public static final String DATA_PATH = Environment
            .getExternalStorageDirectory().toString() + "/E_Ingredients/";
    private ListView listView;
    private AdapterIngredientList adapter;
    private static final String FIRST_RUN_FLAG="FIRSTRUN";
    private static final String FIRST_RUN_SHARED_PREF_NAME="FIRST_RUN_SP_NAME";
    private static final String NOT_FOUND_STR = "NOT_FOUND";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingredient_scan);

        SharedPreferences wmbPreference = this.getSharedPreferences(FIRST_RUN_SHARED_PREF_NAME, MODE_PRIVATE); //PreferenceManager.getDefaultSharedPreferences(this);
        dbHandler = new EDBHandler(this, null, null, 1);
        boolean isFirstRun = wmbPreference.getBoolean(FIRST_RUN_FLAG, true);
        Log.d("FIRST_RUN", "115 " + ((isFirstRun)?"true":"false"));
        // even if marked false, things may have changed
        if (!isFirstRun || true) {
            String cmpDataFiles = TextUtils.join(":", DATA_FILES);
            String dataFiles=wmbPreference.getString("DATAFILES", "");
            if (!cmpDataFiles.equals(dataFiles)) {
                Log.d("FIRST_RUN", "116");
                isFirstRun = true;
                SharedPreferences.Editor editor = wmbPreference.edit();
                editor.putString("DATAFILES", cmpDataFiles);
                editor.apply();
            }
        }
        if (!isFirstRun || true) {
            String cmpDataFiles = TextUtils.join(":", ALWAYS_COPY_DATA_FILES);
            String dataFiles=wmbPreference.getString("ALWAYSCOPYDATAFILES", "");
            if (!cmpDataFiles.equals(dataFiles)) {
                Log.d("FIRST_RUN", "127");
                isFirstRun = true;
                SharedPreferences.Editor editor = wmbPreference.edit();
                editor.putString("ALWAYSCOPYDATAFILES", cmpDataFiles);
                editor.apply();
            }
        }
        if (!isFirstRun || true) {
            if (wmbPreference.getInt("EDBVERSION", -1) != EDBHandler.DATABASE_VERSION) {
                Log.d("FIRST_RUN", "136");
                isFirstRun = true;
                SharedPreferences.Editor editor = wmbPreference.edit();
                editor.putInt("EDBVERSION", EDBHandler.DATABASE_VERSION);
                editor.apply();
            }
        }
        Log.d("FIRST_RUN", "145 " + ((isFirstRun)?"true":"false"));
        if (isFirstRun)
        {
            // Code to run onceכ
            SharedPreferences.Editor editor = wmbPreference.edit();
            editor.putBoolean(FIRST_RUN_FLAG, false);
            editor.commit();
            loadTrainDataFile();

            // Seems like parseDB needs to happen in a background thread.
            new DBParserTask().execute(dbHandler);
        }



//        preview = new Preview(this, this);

//        ((FrameLayout)findViewById(R.id.preview)).addView(preview);
        listView = (ListView) findViewById(R.id.frag_list);
        listView.setOnItemClickListener(this);
        if(adapter==null) {//happen anyway. onRestoreInstanceState is responsible to populate the list
            adapter = new AdapterIngredientList(listView.getContext(), new ArrayList<EDBIngredient>());
            listView.setAdapter(adapter);
        }
    }


    String mCurrentPhotoPath;


    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putString("mCurrentPhotoPath", mCurrentPhotoPath);
        outState.putInt("thresholdValue", thresholdValue);
        outState.putBoolean("enableBinarize", enableBinarize);
        outState.putBoolean("enableGrayscale", enableGrayscale);
        outState.putInt("mOrientation", mOrientation);
        outState.putStringArrayList("ingredientsList", ingredientsList);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mCurrentPhotoPath = savedInstanceState.getString("mCurrentPhotoPath");
        thresholdValue = savedInstanceState.getInt("thresholdValue");
        enableBinarize = savedInstanceState.getBoolean("enableBinarize");
        enableGrayscale = savedInstanceState.getBoolean("enableGrayscale");
        ingredientsList = savedInstanceState.getStringArrayList("ingredientsList");
        mOrientation = savedInstanceState.getInt(("mOrientation"));

        if (ingredientsList == null) {
            if (mCurrentPhotoPath != null)
                setPic(true);
        } else {
            if (mCurrentPhotoPath != null)
                setPic(false);
            ListView listView = (ListView) findViewById(R.id.frag_list);
            ArrayList<EDBIngredient> models = new ArrayList<>();
            if(ingredientsList.size()==1 && ingredientsList.get(0)==NOT_FOUND_STR)
                models.add(EDBIngredient.notFound);
            else {
                for (int i = 0; i < ingredientsList.size(); ++i) {
                    EDBIngredient ingredient = dbHandler.findIngredient(ingredientsList.get(i));
                    if (ingredient == null) {
                        ingredient = new EDBIngredient(ingredientsList.get(i));
                        ingredient.setTitle("Unknown additive");
                    }
                    models.add(ingredient);
                }
            }
            adapter = new AdapterIngredientList(listView.getContext(), models);
            listView.setAdapter(adapter);
        }


    }



    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
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

    static final int REQUEST_INGREDIENT_SELECTION = 3;

    static final int REQUEST_IMAGE_CROP = 2;

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
        editor.putInt("mOrientation", mOrientation);
        editor.putInt("thresholdValue", thresholdValue);
        editor.putBoolean("enableBinarize", enableBinarize);
        editor.putBoolean("enableGrayscale", enableGrayscale);

        // Commit the edits!
        editor.commit();
    }

    private void restorePreferences() {
        SharedPreferences settings = getPreferences(MODE_PRIVATE);
        mCurrentPhotoPath = settings.getString("mCurrentPhotoPath", "");
        mOrientation = settings.getInt("mOrientation", 0);
        thresholdValue = settings.getInt("thresholdValue", 80);
        enableGrayscale = settings.getBoolean("enableGrayscale", true);
        enableBinarize = settings.getBoolean("enableBinarize", true);

    }

    private int mOrientation;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            mOrientation = 0;
            if (mCurrentPhotoPath == null)
                restorePreferences();
            try {
                ExifInterface exif = new ExifInterface(mCurrentPhotoPath);
                mOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                Log.d("EXIF(1)", "Exif: " + mOrientation);
            } catch (Exception e) {
                e.printStackTrace();
            }



            //Toast.makeText(getApplicationContext(),"Grabbing image from "+mCurrentPhotoPath, Toast.LENGTH_LONG).show();
//            setPic(true);
//            origImage = BitmapFactory.decodeFile(mCurrentPhotoPath);
//
//            ImageView iv = (ImageView) findViewById(R.id.origImage);
//            iv.setImageBitmap(origImage);

            Uri imageUri = Uri.fromFile(new File(mCurrentPhotoPath));
//            CropImageIntentBuilder cropImage = new CropImageIntentBuilder(1024*2, 768*2, imageUri);
//            cropImage.setOutlineColor(Color.GREEN); // 0xFF03A9F4);
//            cropImage.setSourceImage(imageUri);

         //   startActivityForResult(cropImage.getIntent(this), REQUEST_IMAGE_CROP);
            Crop.of(imageUri, imageUri).asSquare().start(this);

        } else if (requestCode == REQUEST_IMAGE_CROP && resultCode == RESULT_OK) {
            setPic(true);

        } else if (requestCode == Crop.REQUEST_CROP && resultCode == RESULT_OK) {
            setPic(true);
        } else if (requestCode == REQUEST_INGREDIENT_SELECTION && resultCode == RESULT_OK) {
            String txtToAdd = data.getStringExtra(EIngredientSelectionActivity.EXTRA_SELECTED);

            EDBIngredient ingredient = dbHandler.findIngredient(txtToAdd);
            if(ingredient==null){
                Toast.makeText(getApplicationContext(), "Unknown additive", Toast.LENGTH_SHORT).show();
                return;
            }
            if(adapter.exists(txtToAdd)){
                Toast.makeText(getApplicationContext(), "Additive already appears in the list", Toast.LENGTH_SHORT).show();
                return;
            }
            ingredientsList.add(txtToAdd);
            if(adapter.getSize()>=1 && adapter.getModel(0)==EDBIngredient.notFound){
                adapter.remove(EDBIngredient.notFound);
            }
            adapter.add(ingredient);
            adapter.notifyDataSetChanged();
            listView.deferNotifyDataSetChanged();
            if(adapter.getSize()>1) {
                listView.post(new Runnable() {
                    @Override
                    public void run() {
                        // Select the last row so it will scroll into view...
                        listView.setSelection(adapter.getSize() - 1);
                    }
                });
            }


        }
    }

    public void add(View v) {
        Log.d("Add", "Add pressed");
        startActivityForResult(new Intent(this, EIngredientSelectionActivity.class), REQUEST_INGREDIENT_SELECTION);
    }

    public void snap(View v) {
        /*if (origImage != null)
            origImage.recycle();
        origImage = null;
        if (binarizedImage != null)
            binarizedImage.recycle();
        binarizedImage = null;
        System.gc();*/

        if (TEST_FILE != null) {
            mCurrentPhotoPath = DATA_PATH+"/tessdata/"+TEST_FILE;
            setPic(true);
//            origImage = BitmapFactory.decodeFile(DATA_PATH+"/tessdata/"+TEST_FILE);
//            ImageView iv = (ImageView) findViewById(R.id.origImage);
//            iv.setImageBitmap(origImage);
        } else {
            dispatchTakePictureIntent();
        }
//        preview.mCamera.takePicture(null, null, jpegCallback);

    }


    private void setPic(boolean analyzeIt) {

        if (mCurrentPhotoPath == null) {
            return;
        }
        try {
            int targetW = 1024;
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inSampleSize = 2;
            //BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
            origImage = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);

            try {
                Matrix matrix = new Matrix();
                if (mOrientation == 6) {
                    matrix.postRotate(90);
                } else if (mOrientation == 3) {
                    matrix.postRotate(180);
                } else if (mOrientation == 8) {
                    matrix.postRotate(270);
                }
                origImage = Bitmap.createBitmap(origImage, 0, 0, origImage.getWidth(), origImage.getHeight(), matrix, true); // rotating bitmap
            } catch (Exception e) {
                error(e.toString());
            }
            ImageView iv = (ImageView) findViewById(R.id.origImage);

            // try using leptonica instead
            if (enableGrayscale && enableBinarize && origImage != null) {
                Pix pix = ReadFile.readBitmap(origImage);
                binarizedPixImage = Binarize.otsuAdaptiveThreshold(pix);
            }
            /*if (enableGrayscale && origImage != null)
                grayscale(null);
            if (enableBinarize && origImage != null)
                binarize(null); */

            if (iv != null && binarizedPixImage != null)
                iv.setImageBitmap(WriteFile.writeBitmap(binarizedPixImage));

            TextView tv = (TextView) findViewById(R.id.cameraMessage);
            tv.setText("");
        }catch(OutOfMemoryError e){
            binarizedPixImage=null; origImage=null;
            System.gc(); System.gc();
            Toast.makeText(getApplicationContext(), "OutOfMemoryError. Trying to handle..", Toast.LENGTH_SHORT).show();
            ImageView iv = (ImageView) findViewById(R.id.origImage);
            iv.setImageResource(0);
            System.gc(); System.gc();
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inSampleSize = 8;
            origImage = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        }

        if (analyzeIt)
            analyze(null);
    }

    @Override
    public void onItemClick(AdapterView<?> l, View v, int position, long id) {
        ListView listView = (ListView) findViewById(R.id.frag_list);
        AdapterIngredientList adapter = (AdapterIngredientList) listView.getAdapter();

//        Toast.makeText(this, String.format("Item %d chosen. ID=%s", position, adapter.getModel(position).getFullName()), Toast.LENGTH_SHORT).show();
//        String url = "https://www.google.co.il/webhp?sourceid=chrome-instant&ion=1&espv=2&ie=UTF-8#q=" + adapter.getModel(position).getTag();

        {
            // FIXME should replace model entirely with EDBIngredient. The following code is for testing purpose only
            String key = adapter.getModel(position).getKey();
            EDBIngredient ing = dbHandler.findIngredient(key);
            if (ing != null) {
//                Log.d("EDB", "Found entry for key=" + key + ": " + ing.toHTML());
                Intent intent = new Intent(this, DetailsViewActivity.class);
                intent.putExtra("key", key);
                startActivity(intent);
            } else
                Log.d("EDB", "Did not find entry for key=" + key);
        }

        /*
        url = (url + "+" + adapter.getModel(position).getFullName()).replaceAll(" +", "+");
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
        */

//        if (null != mListener) {
//            mListener.onFragmentInteraction(adapter.getModel(position).getTag());
//        }

    }

/*    public void addIngredient(View view) {
        String txtToAdd = txtAdd.getText().toString();
        txtToAdd = txtToAdd.replaceFirst("^e", "E");
        if(!txtToAdd.startsWith("E"))
            txtToAdd = "E".concat(txtToAdd);
        Log.d("addIngredient: ", txtToAdd);
        EDBIngredient ingredient = dbHandler.findIngredient(txtToAdd);
        if(ingredient==null){
            Toast.makeText(getApplicationContext(), "Unknown additive", Toast.LENGTH_SHORT).show();
            return;
        }
        if(adapter.exists(txtToAdd)){
            Toast.makeText(getApplicationContext(), "Additive already appears in the list", Toast.LENGTH_SHORT).show();
            return;
        }
        ingredientsList.add(txtToAdd);
        if(adapter.getSize()>=1 && adapter.getModel(0)==EDBIngredient.notFound){
            adapter.remove(EDBIngredient.notFound);
        }
        adapter.add(ingredient);
        adapter.notifyDataSetChanged();
        listView.deferNotifyDataSetChanged();
        if(adapter.getSize()>1) {
            listView.post(new Runnable() {
                @Override
                public void run() {
                    // Select the last row so it will scroll into view...
                    listView.setSelection(adapter.getSize() - 1);
                }
            });
        }
    }
*/

    public static Bitmap scaleBitmap(Bitmap bitmapToScale) {
        if(bitmapToScale == null)
            return null;
//get the original width and height
        int width = bitmapToScale.getWidth();
        int height = bitmapToScale.getHeight();
// create a matrix for the manipulation
        Matrix matrix = new Matrix();
// resize the bit map
        matrix.postScale(2, 2);
// recreate the new Bitmap and set it back
        return Bitmap.createBitmap(bitmapToScale, 0, 0, bitmapToScale.getWidth(), bitmapToScale.getHeight(), matrix, true);
    }

    public void imageClick(View view) {
        if(origImage==null)
            return;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("");///+++

        Bitmap b = origImage;
        try{
            b=scaleBitmap(b);
        }catch(OutOfMemoryError e){b=origImage; System.gc(); System.gc(); Toast.makeText(getApplicationContext(), "OutOfMemory. Trying to handle", Toast.LENGTH_SHORT).show();}
        ImageViewTouch imageView = new ImageViewTouch(getApplicationContext(), null);
//        imageView.setImageBitmap(origImage);
//        ImageView imageView = new ImageView(getApplicationContext(), null);
        imageView.setImageBitmap(b);


        builder.setView(imageView);
        builder.show();
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
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        try {
            // Checks the orientation of the screen
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                setContentView(R.layout.activity_ingredient_scan);
                //Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
            } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                setContentView(R.layout.activity_ingredient_scan);
                //Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
            }
            listView = (ListView) findViewById(R.id.frag_list);
            listView.setOnItemClickListener(this);
            //if(adapter==null) {//happen anyway. onRestoreInstanceState is responsible to populate the list
            //adapter = new AdapterIngredientList(listView.getContext(), new ArrayList<EDBIngredient>());
            listView.setAdapter(adapter);

            ImageView iv = (ImageView) findViewById(R.id.origImage);
            TextView tv = (TextView)findViewById(R.id.cameraMessage);
            Bitmap b = origImage;
            if (enableGrayscale && enableBinarize && origImage != null) {
                Pix pix = ReadFile.readBitmap(origImage);
                binarizedPixImage = Binarize.otsuAdaptiveThreshold(pix);
            }

            if (binarizedPixImage != null) b = WriteFile.writeBitmap(binarizedPixImage);
            if (b != null) {
                iv.setImageBitmap(b);
                tv.setText("");
            }
        }catch(Exception e){
            error("failed on configuration change");
        }
    }


    private class DBParserTask extends AsyncTask<EDBHandler, Void, Void> {
        private ProgressDialog dialog;

        @Override
        protected Void doInBackground(EDBHandler... params) {
            try {
                EDBHandler dbHandler = params[0];
                dbHandler.parseDB();
            }catch(Exception e){
                error(e.toString());
            }
            return null;
        }

        protected void onPreExecute() {
            this.dialog = new ProgressDialog(IngredientScanActivity.this);
            this.dialog.setMessage("Please wait while finishing the installation...");
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            this.dialog.show();
        }

        @Override
        protected void onPostExecute(Void result) {
            try {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }catch(Exception e){
                //Toast.makeText(IngredientScanActivity.this.getApplicationContext(), "Dialog is no longer attached to the window", Toast.LENGTH_SHORT).show();
            }

        }
    }


    private class Analyzer extends AsyncTask<Pix, Integer, String> implements TessBaseAPI.ProgressNotifier {

        private ProgressDialog dialog = new ProgressDialog(IngredientScanActivity.this);
        private ArrayList<String> list = new ArrayList<>();
        private Bitmap originalPixToBitmap;
        private ImageView originalImage;

        private void addIngredient(String ingredient){
            list.add(ingredient);
        }
        @Override
        protected String doInBackground(Pix... params) {
            originalPixToBitmap = WriteFile.writeBitmap(params[0]);
            if(MOCK_OCR){
                list=MOCK_LIST;
                try {
                    Thread.sleep(0);
                } catch (InterruptedException e) { Log.d("sleep", "ignored");  }
                return "";
            }
            TessBaseAPI baseApi = new TessBaseAPI(this);
            baseApi.setDebug(false);
            baseApi.init(DATA_PATH, "eng+heb");
//        baseApi.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "E0123456789,()ai-");

            Log.d("tessa api set image", params[0] == null ? "null" : "non null");
            try {
                baseApi.setImage(params[0]);
            }catch (RuntimeException e){
                Toast.makeText(IngredientScanActivity.this.getApplicationContext(), "OCR failed to read image. Please try again", Toast.LENGTH_SHORT).show();
                return "";
            }
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
                        if (words[i].matches("^[^E£5₪]*[E£5₪]-?[\\doOSD][\\doOSD][\\doOSD][a-i]*([^0-9a-zA-Z].*|)$")) {
                            String toAdd = words[i].replaceAll("^[^E£5₪]*[E5£₪]-?([\\doOSD][\\doOSD][\\doOSD][a-i]*).*", "E$1").
                                    replaceAll("[oOD]", "0").
                                    replaceAll("S","5");
                            Log.d("toAdd",toAdd);
                            addIngredient(toAdd);
                        } else if (words[i].matches("^[\\doOSD][\\doOSD][\\doOSD][a-i]*-?[E£5₪]$")) {
                            String toAdd = words[i].replaceAll("^([\\doOSD][\\doOSD][\\doOSD][a-i]*).*", "E$1").
                                    replaceAll("[oOD]", "0").
                                    replaceAll("S","5");
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

//        private StrongProgDialog strongProgDialog;

        static final String TITLE="Extracting text from the picture";
        static final String MESSAGE_BASE="This may take a while, please be patient.";
        @Override
        protected void onPreExecute() {
//            TextView tv = (TextView) findViewById(R.id.result);
//            tv.setText("Analyzing...");
            /*strongProgDialog = new StrongProgDialog();
            strongProgDialog.show(getFragmentManager(), "OCR progress dialog");*/
//            dialog.setMessage(MESSAGE_BASE+"0%");
            dialog.setTitle(TITLE);
            dialog.setMessage(MESSAGE_BASE);
            dialog.setIndeterminate(false);
            dialog.setMax(100);
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setProgressNumberFormat("");
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }catch(Exception e){

            }

            try{
                int sz = list.size();
                Log.d("Finished OCR", String.format("%d %s", sz, list.isEmpty()?"empty":"nonempty"));
                ListView listView = (ListView) findViewById(R.id.frag_list);
                ArrayList<EDBIngredient> models = new ArrayList<EDBIngredient>();
                for(int i=0; i<list.size(); ++i) {
                    EDBIngredient ingredient = dbHandler.findIngredient(list.get(i));
                    if(ingredient==null) {
                        // skip misidentified ingredients
                        continue;
//                        ingredient = new EDBIngredient(list.get(i));
//                        ingredient.setTitle("Unknown ingredient");
                    }
                    models.add(ingredient);
                }
                if(list.isEmpty()){
                    models.add(EDBIngredient.notFound);
                    list.add(NOT_FOUND_STR);
                }
                IngredientScanActivity cur =  IngredientScanActivity.this;
                cur.ingredientsList = list;
                cur.adapter = new AdapterIngredientList(listView.getContext(), models);
                Log.d("setting a new adapter", String.format("size=%d",cur.adapter.getSize()));
                cur.listView.setAdapter(cur.adapter);
            }catch(Exception e) {error(e.toString()); }
        }

        @Override
        public void onProgressValues(TessBaseAPI.ProgressValues progressValues) {
            Log.d("Progress:", ""+progressValues.getPercent()+" [("+progressValues.getBoundingBoxLeft()+", "+
            progressValues.getBoundingBoxTop()+"), ("+progressValues.getBoundingBoxRight()+", "+progressValues.getBoundingBoxBottom()+")]");
            try {
                if (dialog.isShowing()) {
                    if (originalImage == null)
                        originalImage = (ImageView) IngredientScanActivity.this.findViewById(R.id.origImage);
//                    dialog.setMessage(MESSAGE_BASE+progressValues.getPercent()+"%");
                    publishProgress(progressValues.getPercent());
//                    dialog.setProgress(progressValues.getPercent());
/*                    Paint paint = new Paint();
                    Bitmap temp = Bitmap.createBitmap(originalPixToBitmap.getWidth(), originalPixToBitmap.getHeight(),
                            Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(temp);
                    canvas.drawBitmap(originalPixToBitmap, 0, 0, null);
                    paint.setColor(Color.RED);
                    canvas.drawRect(progressValues.getBoundingBoxLeft(), progressValues.getBoundingBoxTop(),
                            progressValues.getBoundingBoxRight(), progressValues.getBoundingBoxBottom(), paint);
                    originalImage.setImageDrawable(new BitmapDrawable(getResources(), temp));*/
                }
            }catch(Exception e){
                e.printStackTrace();
            }


        }
        @Override
        public void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            dialog.setProgress(progress[0]);
        }
    }


    public void analyze(View v) {
        if (origImage == null)
        {
            return;
        }
//        grayscale(v);
//        binarize(v);
        if (binarizedPixImage != null)
        new Analyzer().execute(binarizedPixImage);
    }
/*
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
//        if (binarizedImage != origImage && binarizedImage != null)
//            binarizedImage.recycle();
//        binarizedImage = null;
        System.gc();
        binarizedPixImage = Binarize.otsuAdaptiveThreshold(ReadFile.readBitmap(origImage));
        System.gc();
        ImageView iv = (ImageView) findViewById(R.id.origImage);
        iv.setImageBitmap(WriteFile.writeBitmap(binarizedPixImage));
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
    */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
  /*      try {
            getMenuInflater().inflate(R.menu.menu_ingredient_scan, menu);
            menu.getItem(0).setTitle((enableGrayscale ? getString(R.string.str_dis_gray) : getString(R.string.str_en_gray)));
            menu.getItem(1).setTitle((enableBinarize ? getString(R.string.str_dis_bin) : getString(R.string.str_en_bin)));
        }catch(Exception e){error(e.toString()); } */

        return true;
    }

    private void imageProccessSettings(){
        Log.d("settings", "called");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Please set the enhancement factor (0--100)");

// Set up the input
        final EditText input = new EditText(this);
        input.setText(""+thresholdValue);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    int val = Integer.parseInt(input.getText().toString());
                    thresholdValue = val;
                    setPic(true);
                } catch (NumberFormatException exception) {
                    Log.d("Set enhance factor", "not a valid number");
                }

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
        try {
            int id = item.getItemId();

            //noinspection SimplifiableIfStatement
            if (id == R.id.action_settings) {
                imageProccessSettings();
                return true;
            }
            if (id == R.id.action_grayscale) {
                enableGrayscale = !enableGrayscale;
                item.setTitle((enableGrayscale ? getString(R.string.str_dis_gray) : getString(R.string.str_en_gray)));
                setPic(true);
            }
            if (id == R.id.action_enhance) {
                enableBinarize = !enableBinarize;
                Log.d("626:", "Setting enableBinarize to " + enableBinarize);
                item.setTitle((enableBinarize ? getString(R.string.str_dis_bin) : getString(R.string.str_en_bin)));
                setPic(true);
            }
        }catch(Exception e){error(e.toString());        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Load the train data file for the OCR algorithm.
     * If the file already on the storage - does nothing.
     */
    private void loadTrainDataFile() {
        try {
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
        }catch(Exception e){error(e.toString()); }
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
*/
    @Override
    public void onResume() {
        super.onResume();
        Log.d("onResume", "main activity");
        adapter.notifyDataSetChanged();
    }


}
