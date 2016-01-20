package il.ac.technion.tessa;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuAdapter;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.googlecode.leptonica.android.Binarize;
import com.googlecode.leptonica.android.Pix;
import com.googlecode.leptonica.android.ReadFile;
import com.googlecode.leptonica.android.WriteFile;
import com.googlecode.tesseract.android.ResultIterator;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;

public class IngredientScanActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener,
            OnItemClickListener {
    static final String DATA_FILES[]={
            "eng.traineddata",
            "heb.traineddata"
    };

    static final String ALWAYS_COPY_DATA_FILES[] = {
            "IngredientList.txt",
            "Edb.txt"
    };
    public static void error(String s){Log.d("error", s);}

    final static int IMAGE_SCALE_FACTOR=2;

    Bitmap origImage;
    Pix binarizedPixImage;
    ArrayList<String> ingredientsList = new ArrayList<>();
    ArrayList<Rect> rectangles = new ArrayList<>();
    EDBHandler dbHandler;
    public static final String DATA_PATH = Environment
            .getExternalStorageDirectory().toString() + "/E_Ingredients/";
    private SwipeMenuListView listView;
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

        listView = (SwipeMenuListView) findViewById(R.id.frag_list);
        listView.setOnItemClickListener(this);
        setSwipe(listView);
        if(adapter==null) {//happen anyway. onRestoreInstanceState is responsible to populate the list
            adapter = new AdapterIngredientList(listView.getContext(), new ArrayList<EDBIngredient>());
            listView.setAdapter(adapter);
        }


        Log.d("FIRST_RUN", "145 " + ((isFirstRun) ? "true" : "false"));
        if (isFirstRun)
        {
            File storageDir = getStorageDir(REQUEST_WRITE_PERMISSION_FIRST_RUN);

            if (storageDir != null)
                firstRun();

        }


    }

    void firstRun() {
        // Code to run once
        SharedPreferences wmbPreference = this.getSharedPreferences(FIRST_RUN_SHARED_PREF_NAME, MODE_PRIVATE); //PreferenceManager.getDefaultSharedPreferences(this);

        boolean isFirstRun = wmbPreference.getBoolean(FIRST_RUN_FLAG, true);

        Log.d("REQUS", "isFirstRun = "+isFirstRun);
        if (isFirstRun) {
            SharedPreferences.Editor editor = wmbPreference.edit();
            editor.putBoolean(FIRST_RUN_FLAG, false);
            editor.commit();
            loadTrainDataFile();

            // Seems like parseDB needs to happen in a background thread.
            new DBParserTask().execute(dbHandler);
        }
    }
    static final int REQUEST_WRITE_PERMISSION_FIRST_RUN=1;
    static final int REQUEST_WRITE_PERMISSION_CAMERA=2;

    public File getStorageDir(int stage) {
        File storageDir = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            //RUNTIME PERMISSION Android M
            if(PackageManager.PERMISSION_GRANTED== ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                storageDir = new File(DATA_PATH + "tessdata");
            }else{
                requestPermission(this, stage);
                return null;
            }

        } else {
            storageDir = new File(DATA_PATH+"tessdata");
        }
        return storageDir;
    }

    int mStage=0;
    void setStage(int stage) {
        Log.d("REQUS", "setStage("+stage+")");
        mStage = stage;
    }

    int getStage() {
        Log.d("REQUS","getStage() == "+mStage);
        return mStage;
    }

    String mRequestedPermission=null;

    void setRequestedPermission(String permission) {
        mRequestedPermission = permission;
    }

    String getRequestedPermission() {
        return mRequestedPermission;
    }

    private static void requestPermission(final Context context, int stage){
        ((IngredientScanActivity)context).setStage(stage);
        String permission = null;
        int permission_detail = 0;
        switch (stage) {
            case REQUEST_WRITE_PERMISSION_FIRST_RUN:
                permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
                permission_detail = R.string.permission_storage;
                break;
            case REQUEST_WRITE_PERMISSION_CAMERA:
                permission = Manifest.permission.CAMERA;
                permission_detail = R.string.permission_camera;
                break;
        }
        if (permission == null)
            return;
        ((IngredientScanActivity)context).setRequestedPermission(permission);

        if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, permission)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.

            new AlertDialog.Builder(context)
                    .setMessage(context.getResources().getString(permission_detail))
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions((Activity) context,
                                    new String[]{((IngredientScanActivity) context).getRequestedPermission()},
                                    ((IngredientScanActivity) context).getStage()
                            );
                        }
                    }).show();

        } else {
            // permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{((IngredientScanActivity)context).getRequestedPermission()},
                    ((IngredientScanActivity) context).getStage());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        Log.d("REQUS", "onRequestPermissiosnResult " + requestCode);
        switch (requestCode) {
            case REQUEST_WRITE_PERMISSION_FIRST_RUN: {
                if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    firstRun();
                } else {
                    Toast.makeText(this,
                            getResources().getString(R.string.permission_storage_failure),
                            Toast.LENGTH_LONG).show();
                    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                }
                return;
            }
            case REQUEST_WRITE_PERMISSION_CAMERA: {
                if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    dispatchTakePictureIntent();
                } else {
                    Toast.makeText(this,
                            getResources().getString(R.string.permission_camera_failure),
                            Toast.LENGTH_LONG).show();
                    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                }
                return;
            }
        }
    }

    private void setSwipe(SwipeMenuListView listView) {

        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                if (menu.getViewType() == 0) {
                    // create "delete" item
                    SwipeMenuItem deleteItem = new SwipeMenuItem(
                            getApplicationContext());
                    // set item background
                    deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                            0x3F, 0x25)));
                    // set item width
                    int pixels = getResources().getDimensionPixelSize(R.dimen.item_delete);
                    deleteItem.setWidth(pixels);
                    // set a icon
                    deleteItem.setIcon(android.R.drawable.ic_menu_delete);
                    // add to menu
                    menu.addMenuItem(deleteItem);
                }
            }
        };

// set creator
        listView.setMenuCreator(creator);

        listView.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);

        listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        EDBIngredient model = (EDBIngredient) IngredientScanActivity.this.listView.getAdapter().getItem(position);
                        ingredientsList.remove(model.getKey());
                        adapter.remove(model);
                        adapter.notifyDataSetChanged();
//                        IngredientScanActivity.this.listView.setAdapter(adapter); // hack for now until I figure out how to refresh correctly
                        break;
                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });

    }

    String mCurrentPhotoPath;


    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putString("mCurrentPhotoPath", mCurrentPhotoPath);
        outState.putInt("mOrientation", mOrientation);
        outState.putStringArrayList("ingredientsList", ingredientsList);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mCurrentPhotoPath = savedInstanceState.getString("mCurrentPhotoPath");
        ingredientsList = savedInstanceState.getStringArrayList("ingredientsList");
        mOrientation = savedInstanceState.getInt(("mOrientation"));

        if (ingredientsList == null) {
            if (mCurrentPhotoPath != null)
                setPic(true);
        } else {
            if (mCurrentPhotoPath != null)
                setPic(false);
            SwipeMenuListView listView = (SwipeMenuListView) findViewById(R.id.frag_list);
            ArrayList<EDBIngredient> models = new ArrayList<>();
            if(ingredientsList.size()==1 && ingredientsList.get(0)==NOT_FOUND_STR)
                models.add(EDBIngredient.notFound);
            else {
                for (int i = 0; i < ingredientsList.size(); ++i) {
                    EDBIngredient ingredient = dbHandler.findIngredient(ingredientsList.get(i));
                    if (ingredient == null) {
                        // skip unknown ingredients
                        continue;
//                        ingredient = new EDBIngredient(ingredientsList.get(i));
//                        ingredient.setTitle("Unknown additive");
                    }
                    models.add(ingredient);
                }
            }
            adapter = new AdapterIngredientList(listView.getContext(), models);
            listView.setAdapter(adapter);
            setSwipe(listView);
        }


    }



    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp;
        File storageDir = getStorageDir(REQUEST_WRITE_PERMISSION_CAMERA); //new File(DATA_PATH+"tessdata");
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

        // Commit the edits!
        editor.commit();
    }

    private void restorePreferences() {
        SharedPreferences settings = getPreferences(MODE_PRIVATE);
        mCurrentPhotoPath = settings.getString("mCurrentPhotoPath", "");
        mOrientation = settings.getInt("mOrientation", 0);

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

            Uri imageUri = Uri.fromFile(new File(mCurrentPhotoPath));

            Crop.of(imageUri, imageUri).asSquare().start(this);

        } else if (requestCode == REQUEST_IMAGE_CROP && resultCode == RESULT_OK) {
            setPic(true);

        } else if (requestCode == Crop.REQUEST_CROP && resultCode == RESULT_OK) {
            setPic(true);
            // Erase all .jpg files in the results directory except the one pointed to by mCurrentPhotoPath to save storage
            // on the device (prevent storage leak)
            File datadir = new File(DATA_PATH + "/tessdata");
            File files[] = datadir.listFiles();
            for (File file: files) {
                String abspath = file.getAbsolutePath();
                if (!abspath.endsWith(".jpg"))
                    continue;
                if (abspath.equals(mCurrentPhotoPath))
                    continue;
                // delete the file here
                file.delete();
            }
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
        if(PackageManager.PERMISSION_GRANTED== ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)){
            dispatchTakePictureIntent();
        }else{
            requestPermission(this, REQUEST_WRITE_PERMISSION_CAMERA);
        }
    }


    private void setPic(boolean analyzeIt) {

        if (mCurrentPhotoPath == null) {
            return;
        }
        try {
            int targetW = 1024;
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inSampleSize = IMAGE_SCALE_FACTOR;
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
            if (origImage != null) {
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
        SwipeMenuListView listView = (SwipeMenuListView) findViewById(R.id.frag_list);
        AdapterIngredientList adapter = (AdapterIngredientList) ((SwipeMenuAdapter) listView.getAdapter()).getWrappedAdapter();

        {
            // FIXME should replace model entirely with EDBIngredient. The following code is for testing purpose only
            String key = adapter.getModel(position).getKey();
            EDBIngredient ing = dbHandler.findIngredient(key);
            if (ing != null) {
                Intent intent = new Intent(this, DetailsViewActivity.class);
                intent.putExtra("key", key);
                startActivity(intent);
            } else
                Log.d("EDB", "Did not find entry for key=" + key);
        }
    }


    public static Bitmap scaleBitmap(Bitmap bitmapToScale) {
        if(bitmapToScale == null)
            return null;
//get the original width and height
        int width = bitmapToScale.getWidth();
        int height = bitmapToScale.getHeight();
// create a matrix for the manipulation
        Matrix matrix = new Matrix();
// resize the bit map
        matrix.postScale(IMAGE_SCALE_FACTOR, IMAGE_SCALE_FACTOR);
// recreate the new Bitmap and set it back
        return Bitmap.createBitmap(bitmapToScale, 0, 0, bitmapToScale.getWidth(), bitmapToScale.getHeight(), matrix, true);
    }

    public void imageClick(View view) {
        if(origImage==null)
            return;
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("");///+++

            Bitmap b = origImage;
            try {
                b = scaleBitmap(b);
            } catch (OutOfMemoryError e) {
                b = origImage;
                System.gc();
                System.gc();
                Toast.makeText(getApplicationContext(), "OutOfMemory. Trying to handle", Toast.LENGTH_SHORT).show();
            }
            ImageViewTouch imageView = new ImageViewTouch(getApplicationContext(), null);

            Bitmap bmp=null;
            try {
                bmp = b.copy(b.getConfig(), true);
                Canvas canvas = new Canvas(bmp);

                Paint paint = new Paint();
                paint.setColor(Color.RED);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(4);
                for (Rect rect : rectangles) {
                    Rect r = new Rect(rect);
                    r.left *= IMAGE_SCALE_FACTOR;
                    r.top *= IMAGE_SCALE_FACTOR;
                    r.right *= IMAGE_SCALE_FACTOR;
                    r.bottom *= IMAGE_SCALE_FACTOR;
                    canvas.drawRect(r, paint);
                }
                Log.d("RECTS", rectangles.toString());
            }catch(OutOfMemoryError error){
                bmp=null; b=null;  System.gc();System.gc();
                Toast.makeText(getApplicationContext(), "OOM: failed to emphasize the picked ingredients", Toast.LENGTH_SHORT).show();
                bmp=origImage;
                Log.d("OOM", "bmp=b");
            }
            try {
                imageView.setImageBitmap(bmp);
            }catch(OutOfMemoryError error){bmp=b=null; System.gc();System.gc();Toast.makeText(getApplicationContext(), "OOM2", Toast.LENGTH_SHORT).show();}



            builder.setView(imageView);
            builder.show();
        }catch(Exception e){Toast.makeText(getApplicationContext(), "Unexpected error", Toast.LENGTH_SHORT).show();}
        catch(OutOfMemoryError error){System.gc();System.gc();Toast.makeText(getApplicationContext(), "OOM", Toast.LENGTH_SHORT).show();}
    }

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
            listView = (SwipeMenuListView) findViewById(R.id.frag_list);
            listView.setOnItemClickListener(this);
            //if(adapter==null) {//happen anyway. onRestoreInstanceState is responsible to populate the list
            //adapter = new AdapterIngredientList(listView.getContext(), new ArrayList<EDBIngredient>());
            setSwipe(listView);
            listView.setAdapter(adapter);

            ImageView iv = (ImageView) findViewById(R.id.origImage);
            TextView tv = (TextView)findViewById(R.id.cameraMessage);
            Bitmap b = origImage;
            if (origImage != null) {
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
        private HashMap<String, ArrayList<Rect>> rects = new HashMap<>();
        private Bitmap originalPixToBitmap;
        private ImageView originalImage;

        private void addIngredient(String ingredient, Rect rect){
            if (!list.contains(ingredient))
                list.add(ingredient);
            if (rect != null) {
                if (rects.containsKey(ingredient))
                    rects.get(ingredient).add(rect);
                else {
                    ArrayList<Rect> r = new ArrayList();
                    r.add(rect);
                    rects.put(ingredient, r);
                }
            }
        }
        @Override
        protected String doInBackground(Pix... params) {
            originalPixToBitmap = WriteFile.writeBitmap(params[0]);
            TessBaseAPI baseApi = new TessBaseAPI(this);
            baseApi.setDebug(false);
            baseApi.init(DATA_PATH, "eng+heb");

            Log.d("tessa api set image", params[0] == null ? "null" : "non null");
            try {
                baseApi.setImage(params[0]);
            }catch (RuntimeException e){
                Toast.makeText(IngredientScanActivity.this.getApplicationContext(), "OCR failed to read image. Please try again", Toast.LENGTH_SHORT).show();
                return "";
            }
            String recognizedText = baseApi.getUTF8Text();

            StringBuffer result = new StringBuffer();
            final ResultIterator iterator = baseApi.getResultIterator();
            iterator.begin(); //crashes my app
            do {
                String word = iterator.getUTF8Text(TessBaseAPI.PageIteratorLevel.RIL_WORD);
                Rect rect = iterator.getBoundingRect(TessBaseAPI.PageIteratorLevel.RIL_WORD);
                if(word==null) break;
                Log.d("word", "#" + word + "#");
                if (word.matches(".*\\([E£5₪]\\d\\d\\d[a-i]*\\).*")) {
                    String toAdd = word.replaceAll(".*\\([E£5₪](\\d\\d\\d[a-i]*)\\).*", "E$1");
                    Log.d("toAdd",toAdd);
                    addIngredient(toAdd, rect); //result.append(word.replaceAll(".*\\([E£5](\\d\\d\\d[a-i]*)\\).*", "E$1")).append("\n");
                } else {
                    String[] words = word.split(",");
                    for (int i=0; i < words.length; i++) {
                        Log.d("words","%"+words[i]+"%");
                        if (words[i].matches("^[^E£5₪]*[E£5₪]-?[\\doOSD][\\doOSD][\\doOSD][a-i]*([^0-9a-zA-Z].*|)$")) {
                            String toAdd = words[i].replaceAll("^[^E£5₪]*[E5£₪]-?([\\doOSD][\\doOSD][\\doOSD][a-i]*).*", "E$1").
                                    replaceAll("[oOD]", "0").
                                    replaceAll("S","5");
                            Log.d("toAdd",toAdd);
                            addIngredient(toAdd, rect);
                        } else if (words[i].matches("^[\\doOSD][\\doOSD][\\doOSD][a-i]*-?[E£5₪]$")) {
                            String toAdd = words[i].replaceAll("^([\\doOSD][\\doOSD][\\doOSD][a-i]*).*", "E$1").
                                    replaceAll("[oOD]", "0").
                                    replaceAll("S","5");
                            Log.d("toAdd",toAdd);
                            addIngredient(toAdd, rect);
                        }
                    }
                }
            } while (iterator.next(TessBaseAPI.PageIteratorLevel.RIL_WORD));

            String res = result.toString();
            baseApi.end();
            Log.d("word", "Analysis complete");
            return res;
        }

        static final String TITLE="Extracting text from the picture";
        static final String MESSAGE_BASE="This may take a while, please be patient.";
        @Override
        protected void onPreExecute() {
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
                SwipeMenuListView listView = (SwipeMenuListView) findViewById(R.id.frag_list);
                ArrayList<EDBIngredient> models = new ArrayList<>();
                ArrayList<Rect> rectangles = new ArrayList<>();
                for(int i=0; i<list.size(); ++i) {
                    EDBIngredient ingredient = dbHandler.findIngredient(list.get(i));
                    if(ingredient==null) {
                        // skip misidentified ingredients
                        continue;
//                        ingredient = new EDBIngredient(list.get(i));
//                        ingredient.setTitle("Unknown ingredient");
                    }
                    models.add(ingredient);
                    rectangles.addAll(rects.get(list.get(i)));
                }
                if(list.isEmpty()){
                    models.add(EDBIngredient.notFound);
                    list.add(NOT_FOUND_STR);
                }
                IngredientScanActivity cur =  IngredientScanActivity.this;
                cur.ingredientsList = list;
                cur.rectangles = rectangles;
                cur.adapter = new AdapterIngredientList(listView.getContext(), models);
                Log.d("setting a new adapter", String.format("size=%d",cur.adapter.getSize()));
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
                    publishProgress(progressValues.getPercent());
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
        if (binarizedPixImage != null)
        new Analyzer().execute(binarizedPixImage);
    }

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        try {
            int id = item.getItemId();

            //noinspection SimplifiableIfStatement
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


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("onResume", "main activity");
        adapter.notifyDataSetChanged();
        if (getRequestedPermission() == null) {
            File storageDir = getStorageDir(REQUEST_WRITE_PERMISSION_FIRST_RUN);
            if (storageDir != null)
                firstRun();
        }
    }


}
