package com.SYDEprojects.matrixocr;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import com.googlecode.tesseract.android.TessBaseAPI;

import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Build;
import android.provider.MediaStore;

public class OCR extends Activity {
	
	protected Button button;
	protected Button searchButton;
	protected ImageView image;
	protected EditText text;
	
	protected String path;
	protected String croppedPath;
	protected String dataPath = Environment.getExternalStorageDirectory().toString()+"/OCRTest/";
	
	protected boolean photoTaken=false;

	public static final String lang = "eng";
	private Uri outputFileUri;
	private Uri croppedOutputFileUri;

	private static final int PICK_FROM_CAMERA = 1;
	private static final int CROP_FROM_CAMERA = 2;
	
	private String currentClass = "OCR.java";
	protected static final String IS_PHOTO_TAKEN= "photo_taken";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupActionBar();
		
		System.out.println("We made it!");
		
		Log.i(currentClass,"Let's DO THIS");
		
		createDirectory(dataPath);
		createDirectory(dataPath + "tessdata/");

		if (!(new File(dataPath + "tessdata/" + lang + ".traineddata")).exists()) {
			try {

				AssetManager assetManager = getAssets();
				InputStream in = assetManager.open("tessdata/eng.traineddata");
				OutputStream out = new FileOutputStream(dataPath + "tessdata/eng.traineddata");
				
				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0) 
				{
					out.write(buf, 0, len);
				}
				in.close();
				out.close();
			} catch (IOException e) {
			}
		}

        path =  dataPath+"OCR_image.jpg";
        croppedPath = dataPath+"OCR_image_cropped";

        startCamera();
	}

	public static void createDirectory(String dirName)
	{
		File dir = new File(dirName);
		if(!dir.exists())
		{
			System.out.println("Path doesn't exist!");
			if(dir.mkdir())
				System.out.println("Path created successfully at "+dir);
			else
				System.out.println("Path not created, failure");
		}
		else
			System.out.println("Path does indeed exist at "+dir);
	}

	
	public OnClickListener searchButtonClickHandler = new OnClickListener(){

		@Override
		public void onClick(View v) {
			Log.i(currentClass,"Search Button is clicked");
			String query = text.getText().toString();
			
			String urlString = "https://www.google.ca/search?q=" + query;
			
			Intent getURL = new Intent (Intent.ACTION_VIEW,Uri.parse(urlString));
			
			startActivity(getURL);
		}
	};
	
	private void startCamera() {
		Log.i(currentClass,"Starting camera...");
		
    	File file = new File(path);
    	outputFileUri = Uri.fromFile(file);
    	
    	Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE );
    	intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
    	
    	startActivityForResult(intent, PICK_FROM_CAMERA);
		
    	
	}
	
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){	
    	Log.i(currentClass,"onActivityResult...");
    	Log.i(currentClass,"request code :"+requestCode);
    	
    	if(resultCode==-1)
    	{
	    	if(requestCode==PICK_FROM_CAMERA)
	    	{
	    		Log.i(currentClass,"Just took a photo");
	    		doCrop();
	    	}
	    	else if(requestCode==CROP_FROM_CAMERA)
	    	{
	    		Log.i(currentClass,"Just cropped");
	    		imageTaken();
	    	}
	    	else
	    		Log.e(currentClass,"IDK what happened TBH...");
    	}
    	else
    		Log.w(currentClass,"User quit");
    }
    

	private void imageTaken() {
		Log.i(currentClass,"ImageTaken method...");
		photoTaken=true;
		
    	BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;

    	Bitmap bitmap = BitmapFactory.decodeFile(croppedPath, options );

    	try {
			ExifInterface exif = new ExifInterface(croppedPath);
			int exifOrientation = exif.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);
	
			int rotate = 0;

			switch (exifOrientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				rotate = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				rotate = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				rotate = 270;
				break;
			}


			if (rotate != 0) {
				int w = bitmap.getWidth();
				int h = bitmap.getHeight();

				// Setting pre rotate
				Matrix mtx = new Matrix();
				mtx.preRotate(rotate);
				
				// Rotating Bitmap
				bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, false);
			}

			// Convert to ARGB_8888, required by tess
			bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

		} catch (IOException e) {
			Log.e(currentClass, "Something went wrong...");
		}

    	Log.i(currentClass,"Starting TessBaseAPI");
		TessBaseAPI baseApi = new TessBaseAPI();
		baseApi.setDebug(true);
		baseApi.init(dataPath, lang);
		baseApi.setImage(bitmap);
		
		String OCRoutput = baseApi.getUTF8Text();
		
		baseApi.end();
		Log.i(currentClass,"Done TessBaseAPI!");
    	
		System.out.println("word is "+OCRoutput);
		
		Intent returnIntent = new Intent();
		 returnIntent.putExtra("result",OCRoutput);
		 setResult(RESULT_OK,returnIntent);     
		 finish();
	}

	private void doCrop() 
	{
    	
    	Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setType("image/*");
        
    	File file = new File(croppedPath);
    	croppedOutputFileUri = Uri.fromFile(file);
    	
        List<ResolveInfo> list = getPackageManager().queryIntentActivities( intent, 0 );
        
        int size = list.size();
        System.out.println("size is "+size);
        if (size == 0) {	        
        	Toast.makeText(this, "Can not find image crop app", Toast.LENGTH_SHORT).show();
            return;
            
        } 
        else 
        {
        	intent.setData(outputFileUri);
        	intent.putExtra("outputX", 200);
        	intent.putExtra("outputY", 200);
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("scale", true);
            intent.putExtra("return-data", true);

        	Intent i 		= new Intent(intent);
	        ResolveInfo res	= list.get(0);
	        
	        i.setComponent( new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
	        i.putExtra(MediaStore.EXTRA_OUTPUT, croppedOutputFileUri);
	        
	        startActivityForResult(i, CROP_FROM_CAMERA);
        }
	}
	
	
    protected void onSaveInstanceState( Bundle outState ) {
    	System.out.println("Save Instance State function");
    	outState.putBoolean(IS_PHOTO_TAKEN, photoTaken );
    }
    
    protected void onRestoreInstanceState( Bundle savedInstanceState){
    	System.out.println("Restore Instance State function");
    	if( savedInstanceState.getBoolean(IS_PHOTO_TAKEN) ) {
    		System.out.println("derp mcderpson");
    		imageTaken();
    	}
    }
	
	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.ocr, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
