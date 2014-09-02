package android.ssd;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

import com.googlecode.tesseract.android.TessBaseAPI;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class EditSnap extends Activity {
	
	Button ok,can;
	static ImageView iv;
	static Bitmap b;
	private TextToSpeech mTts;
	String _path=PhotoOpt.imagepath;
	
	
	public static final String PACKAGE_NAME = "com.datumdroid.android.ocr.simple";
	public static final String DATA_PATH = Environment
			.getExternalStorageDirectory().toString() + "/SimpleAndroidOCR/";

	// You should have the trained data file in assets folder
	// You can get them at:
	// http://code.google.com/p/tesseract-ocr/downloads/list
	public static final String lang = "eng";

	private static final String TAG = "SimpleAndroidOCR.java";

	
	public void onCreate(Bundle savedInstanceState) {
		
		
		String[] paths = new String[] { DATA_PATH, DATA_PATH + "tessdata/" };

		for (String path : paths) {
			File dir = new File(path);
			if (!dir.exists()) {
				if (!dir.mkdirs()) {
					Log.v(TAG, "ERROR: Creation of directory " + path + " on sdcard failed");
					return;
				} else {
					Log.v(TAG, "Created directory " + path + " on sdcard");
				}
			}

		}

		// lang.traineddata file with the app (in assets folder)
		// You can get them at:
		// http://code.google.com/p/tesseract-ocr/downloads/list
		// This area needs work and optimization
		if (!(new File(DATA_PATH + "tessdata/" + lang + ".traineddata")).exists()) {
			try {

				AssetManager assetManager = getAssets();
				InputStream in = assetManager.open("tessdata/eng.traineddata");
				//GZIPInputStream gin = new GZIPInputStream(in);
				OutputStream out = new FileOutputStream(DATA_PATH+ "tessdata/eng.traineddata");

				// Transfer bytes from in to out
				byte[] buf = new byte[1024];
				int len;
				//while ((lenf = gin.read(buff)) > 0) {
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				in.close();
				//gin.close();
				out.close();

				Log.v(TAG, "Copied " + lang + " traineddata");
			} catch (IOException e) {
				Log.e(TAG, "Was unable to copy " + lang + " traineddata " + e.toString());
			}
		}

		
		super.onCreate(savedInstanceState);
        setContentView(R.layout.editimg);
        
        Toast.makeText(this.getBaseContext(),DATA_PATH,Toast.LENGTH_LONG).show();
              
        iv=(ImageView) findViewById(R.id.imageView1);
        iv.getLayoutParams().height=350;
        iv.setImageBitmap(b);
        ok=(Button)findViewById(R.id.button1);
        ok.getLayoutParams().width=160;
        ok.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Bitmap bitmap = null;
				try {
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inSampleSize = 4;

				bitmap = BitmapFactory.decodeFile(_path, options);
				
					ExifInterface exif = new ExifInterface(_path);
					int exifOrientation = exif.getAttributeInt(
							ExifInterface.TAG_ORIENTATION,
							ExifInterface.ORIENTATION_NORMAL);

					Log.v(TAG, "Orient: " + exifOrientation);

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

					Log.v(TAG, "Rotation: " + rotate);

					if (rotate != 0) {

						// Getting width & height of the given image.
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
					// Toast.makeText(this.getBaseContext(),DATA_PATH,Toast.LENGTH_LONG).show();
					
					Log.e(TAG, "Couldn't correct orientation: " + e.toString());
				}

				// _image.setImageBitmap( bitmap );

				Log.e(TAG, "Before baseApi");

				TessBaseAPI baseApi = new TessBaseAPI();
				baseApi.setDebug(true);
				baseApi.init(DATA_PATH, lang);
				baseApi.setImage(bitmap);

				String recognizedText = baseApi.getUTF8Text();

				baseApi.end();

				// You now have the text in recognizedText var, you can do anything with it.
				// We will display a stripped out trimmed alpha-numeric version of it (if lang is eng)
				// so that garbage doesn't make it to the display.

				Log.e(TAG, "OCRED TEXT: " + recognizedText);

				if ( lang.equalsIgnoreCase("eng") ) {
					//recognizedText = recognizedText.replaceAll("[^a-zA-Z0-9]+", " ");
				}

				recognizedText = recognizedText.trim();
				try {
					  if(recognizedText.isEmpty())
						   throw new Exception();
				      // Test whether the result is null
				      
				    } catch (Exception e) {
				      Toast toast = Toast.makeText(getBaseContext(), "OCR failed. Please try again.", Toast.LENGTH_SHORT);
				    
				      toast.setGravity(Gravity.TOP, 0, 0);
				      toast.show();
				      return;
				     
				    }
				    Intent intent = new Intent();
					try {
						CaptureActivity.ocrText=recognizedText;
						intent.setClass(EditSnap.this,TtsActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);
					} catch (Exception e) {
						Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG)
								.show();
					}
				    // Turn off capture-related UI elements
				   

				
			
			}
		});
      
        can=(Button)findViewById(R.id.button2);
        can.getLayoutParams().width=160;

        can.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Intent intent = new Intent();
				try{
				intent.setClass(EditSnap.this,PhotoOpt.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(intent);
				}
				catch(Exception e){
					Toast.makeText(getBaseContext(),e.getMessage(),Toast.LENGTH_LONG).show();
				}

			}
		});
        
	}
        protected void onActivityResult(
                int requestCode, int resultCode, Intent data) {
            if (requestCode == 1) {
                if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                    // success, create the TTS instance
                    mTts = new TextToSpeech(this, (OnInitListener) this);
                    mTts.setLanguage(Locale.US);
                    String myText1 = "Did you sleep well?";
                    String myText2 = "I hope so, because it's time to wake up.";
                    mTts.speak(myText1, TextToSpeech.QUEUE_FLUSH, null);
                    mTts.speak(myText2, TextToSpeech.QUEUE_ADD, null);
                } else {
                    // missing data, install it
                    Intent installIntent = new Intent();
                    installIntent.setAction(
                        TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                    startActivity(installIntent);
                }
            }
        }
		
        
		


}
