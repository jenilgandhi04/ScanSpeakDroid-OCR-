package android.ssd;

import android.ssd.BeepManager;
import com.googlecode.tesseract.android.TessBaseAPI;
import android.ssd.camera.CameraManager;
import android.ssd.camera.ShutterButton;
import android.ssd.OcrResult;
import android.ssd.language.LanguageCodeHelper;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;

import android.text.SpannableStringBuilder;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;

import android.view.Gravity;
import android.view.KeyEvent;

import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public final class CaptureActivity extends Activity implements SurfaceHolder.Callback, 
  ShutterButton.OnShutterButtonListener {

  private static final String TAG = CaptureActivity.class.getSimpleName();
  public static String ocrText=" ";
  
  public static final String DEFAULT_SOURCE_LANGUAGE_CODE = "eng";
  
  
  public static final String DEFAULT_TARGET_LANGUAGE_CODE = "es";
  
  
  public static final String DEFAULT_OCR_ENGINE_MODE = " ";
  
  
  public static final String DEFAULT_PAGE_SEGMENTATION_MODE = "Auto";
  
  
  public static final boolean DEFAULT_TOGGLE_BEEP = true;
  
 
  public static final boolean DEFAULT_TOGGLE_CONTINUOUS = false;
  
  
  public static final boolean DEFAULT_TOGGLE_REVERSED_IMAGE = false;
  
  
  public static final boolean DEFAULT_TOGGLE_LIGHT = false;

  
  
  private static final boolean CONTINUOUS_DISPLAY_RECOGNIZED_TEXT = true;
  
  
  private static final boolean CONTINUOUS_DISPLAY_METADATA = true;
  
  
  private static final boolean DISPLAY_SHUTTER_BUTTON = true;
  
 
  static final String[] CUBE_SUPPORTED_LANGUAGES = { 
    "eng" // English
    
  };

  
  private static final String[] CUBE_REQUIRED_LANGUAGES = { 
    "ara" // Arabic
  };
  
  /** Resource to use for data file downloads. */
  static final String DOWNLOAD_BASE = "http://tesseract-ocr.googlecode.com/files/";
  
  /** Download filename for orientation and script detection (OSD) data. */
  static final String OSD_FILENAME = "tesseract-ocr-3.01.osd.tar";
  
  /** Destination filename for orientation and script detection (OSD) data. */
  static final String OSD_FILENAME_BASE = "osd.traineddata";
  
  /** Minimum mean confidence score necessary to not reject single-shot OCR result. Currently unused. */
  static final int MINIMUM_MEAN_CONFIDENCE = 0; // 0 means don't reject any scored results
  
  /** Length of time before the next autofocus request, if the last one was successful. Used in CaptureActivityHandler. */
  static final long AUTOFOCUS_SUCCESS_INTERVAL_MS = 3000L;
  
  /** Length of time before the next autofocus request, if the last request failed. Used in CaptureActivityHandler. */
  static final long AUTOFOCUS_FAILURE_INTERVAL_MS = 1000L;
  
  

  private CameraManager cameraManager;
  private CaptureActivityHandler handler;
  private ViewfinderView viewfinderView;
  private SurfaceView surfaceView;
  private SurfaceHolder surfaceHolder;
  private TextView statusViewBottom;
  private TextView statusViewTop;
  private TextView ocrResultView;
  private View cameraButtonView;
  private View resultView;
  private View progressView;
  private OcrResult lastResult;
  private Bitmap lastBitmap;
  private boolean hasSurface;
  private BeepManager beepManager;
  public TessBaseAPI baseApi; 
  private String sourceLanguageCodeOcr; 
  private String sourceLanguageReadable; 
  private String sourceLanguageCodeTranslation; 
  private String targetLanguageCodeTranslation; 
  private String targetLanguageReadable; 
  private int pageSegmentationMode = TessBaseAPI.PSM_AUTO;
  private int ocrEngineMode = TessBaseAPI.OEM_TESSERACT_ONLY;
  private String characterBlacklist;
  private String characterWhitelist;
  private ShutterButton shutterButton;
  private Button done_Btn;

  private boolean isTranslationActive; 
  private boolean isContinuousModeActive; 
  private SharedPreferences prefs;
  private OnSharedPreferenceChangeListener listener;
  private ProgressDialog dialog;
  private ProgressDialog indeterminateDialog; 
  private boolean isEngineReady;
  private boolean isPaused;
  private static boolean isFirstLaunch; 
  Handler getHandler() {
    return handler;
  }

  CameraManager getCameraManager() {
    return cameraManager;
  }
  
  @Override
  public void onCreate(Bundle icicle) {
	  try{
    super.onCreate(icicle);
   
   
    Window window = getWindow();
    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    setContentView(R.layout.capture);
  
    viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
    
    cameraButtonView = findViewById(R.id.camera_button_view);
    resultView = findViewById(R.id.result_view);
   
   
   
   
   
    statusViewBottom = (TextView) findViewById(R.id.status_view_bottom);
    registerForContextMenu(statusViewBottom);
    statusViewTop = (TextView) findViewById(R.id.status_view_top);
    registerForContextMenu(statusViewTop);
    
    handler = null;
    lastResult = null;
    hasSurface = false;
    beepManager = new BeepManager(this);
    
    // Camera shutter button
    if (DISPLAY_SHUTTER_BUTTON) {
      shutterButton = (ShutterButton) findViewById(R.id.shutter_button);
      shutterButton.setOnShutterButtonListener(this);
      done_Btn=(Button) findViewById(R.id.done_btn);
      done_Btn.setOnClickListener(new OnClickListener() {
  		
  		public void onClick(View v) {
  			done_Btn.setVisibility(View.GONE);
  			Intent intent = new Intent();
  			intent.setClass(CaptureActivity.this,TtsActivity.class);
  			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
  			//intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
  			startActivity(intent);
  		   
  		}
  	});
      done_Btn.setVisibility(View.GONE);
    }
   
    ocrResultView = (TextView) findViewById(R.id.ocr_result_text_view);
    registerForContextMenu(ocrResultView);
 
    progressView = (View) findViewById(R.id.indeterminate_progress_indicator_view);

    cameraManager = new CameraManager(getApplication());
    viewfinderView.setCameraManager(cameraManager);
    
    // Set listener to change the size of the viewfinder rectangle.
    viewfinderView.setOnTouchListener(new View.OnTouchListener() {
      int lastX = -1;
      int lastY = -1;

      @Override
      public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
          lastX = -1;
          lastY = -1;
          return true;
        case MotionEvent.ACTION_MOVE:
          int currentX = (int) event.getX();
          int currentY = (int) event.getY();

          try {
            Rect rect = cameraManager.getFramingRect();

            final int BUFFER = 50;
            final int BIG_BUFFER = 60;
            if (lastX >= 0) {
              // Adjust the size of the viewfinder rectangle. Check if the touch event occurs in the corner areas first, because the regions overlap.
              if (((currentX >= rect.left - BIG_BUFFER && currentX <= rect.left + BIG_BUFFER) || (lastX >= rect.left - BIG_BUFFER && lastX <= rect.left + BIG_BUFFER))
                  && ((currentY <= rect.top + BIG_BUFFER && currentY >= rect.top - BIG_BUFFER) || (lastY <= rect.top + BIG_BUFFER && lastY >= rect.top - BIG_BUFFER))) {
                // Top left corner: adjust both top and left sides
                cameraManager.adjustFramingRect( 2 * (lastX - currentX), 2 * (lastY - currentY));
                viewfinderView.removeResultText();
              } else if (((currentX >= rect.right - BIG_BUFFER && currentX <= rect.right + BIG_BUFFER) || (lastX >= rect.right - BIG_BUFFER && lastX <= rect.right + BIG_BUFFER)) 
                  && ((currentY <= rect.top + BIG_BUFFER && currentY >= rect.top - BIG_BUFFER) || (lastY <= rect.top + BIG_BUFFER && lastY >= rect.top - BIG_BUFFER))) {
                // Top right corner: adjust both top and right sides
                cameraManager.adjustFramingRect( 2 * (currentX - lastX), 2 * (lastY - currentY));
                viewfinderView.removeResultText();
              } else if (((currentX >= rect.left - BIG_BUFFER && currentX <= rect.left + BIG_BUFFER) || (lastX >= rect.left - BIG_BUFFER && lastX <= rect.left + BIG_BUFFER))
                  && ((currentY <= rect.bottom + BIG_BUFFER && currentY >= rect.bottom - BIG_BUFFER) || (lastY <= rect.bottom + BIG_BUFFER && lastY >= rect.bottom - BIG_BUFFER))) {
                // Bottom left corner: adjust both bottom and left sides
                cameraManager.adjustFramingRect(2 * (lastX - currentX), 2 * (currentY - lastY));
                viewfinderView.removeResultText();
              } else if (((currentX >= rect.right - BIG_BUFFER && currentX <= rect.right + BIG_BUFFER) || (lastX >= rect.right - BIG_BUFFER && lastX <= rect.right + BIG_BUFFER)) 
                  && ((currentY <= rect.bottom + BIG_BUFFER && currentY >= rect.bottom - BIG_BUFFER) || (lastY <= rect.bottom + BIG_BUFFER && lastY >= rect.bottom - BIG_BUFFER))) {
                // Bottom right corner: adjust both bottom and right sides
                cameraManager.adjustFramingRect(2 * (currentX - lastX), 2 * (currentY - lastY));
                viewfinderView.removeResultText();
              } else if (((currentX >= rect.left - BUFFER && currentX <= rect.left + BUFFER) || (lastX >= rect.left - BUFFER && lastX <= rect.left + BUFFER))
                  && ((currentY <= rect.bottom && currentY >= rect.top) || (lastY <= rect.bottom && lastY >= rect.top))) {
                // Adjusting left side: event falls within BUFFER pixels of left side, and between top and bottom side limits
                cameraManager.adjustFramingRect(2 * (lastX - currentX), 0);
                viewfinderView.removeResultText();
              } else if (((currentX >= rect.right - BUFFER && currentX <= rect.right + BUFFER) || (lastX >= rect.right - BUFFER && lastX <= rect.right + BUFFER))
                  && ((currentY <= rect.bottom && currentY >= rect.top) || (lastY <= rect.bottom && lastY >= rect.top))) {
                // Adjusting right side: event falls within BUFFER pixels of right side, and between top and bottom side limits
                cameraManager.adjustFramingRect(2 * (currentX - lastX), 0);
                viewfinderView.removeResultText();
              } else if (((currentY <= rect.top + BUFFER && currentY >= rect.top - BUFFER) || (lastY <= rect.top + BUFFER && lastY >= rect.top - BUFFER))
                  && ((currentX <= rect.right && currentX >= rect.left) || (lastX <= rect.right && lastX >= rect.left))) {
                // Adjusting top side: event falls within BUFFER pixels of top side, and between left and right side limits
                cameraManager.adjustFramingRect(0, 2 * (lastY - currentY));
                viewfinderView.removeResultText();
              } else if (((currentY <= rect.bottom + BUFFER && currentY >= rect.bottom - BUFFER) || (lastY <= rect.bottom + BUFFER && lastY >= rect.bottom - BUFFER))
                  && ((currentX <= rect.right && currentX >= rect.left) || (lastX <= rect.right && lastX >= rect.left))) {
                // Adjusting bottom side: event falls within BUFFER pixels of bottom side, and between left and right side limits
                cameraManager.adjustFramingRect(0, 2 * (currentY - lastY));
                viewfinderView.removeResultText();
              }     
            }
          } catch (NullPointerException e) {
            Log.e(TAG, "Framing rect not available", e);
          }
          v.invalidate();
          lastX = currentX;
          lastY = currentY;
          return true;
        case MotionEvent.ACTION_UP:
          lastX = -1;
          lastY = -1;
          return true;
        }
        return false;
      }
    });
    
    isEngineReady = false;
	  }
	  catch(Exception ex){Toast.makeText(this.getApplicationContext(),ex.getMessage(),Toast.LENGTH_LONG).show();}
  }

  @Override
  protected void onResume() {
    super.onResume();   
    resetStatusView();
 done_Btn.setOnClickListener(new OnClickListener() {
		
		public void onClick(View v) {
			done_Btn.setVisibility(View.GONE);
			Intent intent = new Intent();
			intent.setClass(CaptureActivity.this,TtsActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			//intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(intent);
		   
		}
	});
    
    String previousSourceLanguageCodeOcr = sourceLanguageCodeOcr;
    int previousOcrEngineMode = ocrEngineMode;
    
    retrievePreferences();
    
    // Set up the camera preview surface.
    surfaceView = (SurfaceView) findViewById(R.id.preview_view);
    surfaceHolder = surfaceView.getHolder();
    if (!hasSurface) {
      surfaceHolder.addCallback(this);
      surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }
    
    
    
    // Do OCR engine initialization, if necessary
    boolean doNewInit = (baseApi == null) || !sourceLanguageCodeOcr.equals(previousSourceLanguageCodeOcr) || 
        ocrEngineMode != previousOcrEngineMode;
    if (doNewInit) {      
      // Initialize the OCR engine
      File storageDirectory = getStorageDirectory();
      if (storageDirectory != null) {
        initOcrEngine(storageDirectory, sourceLanguageCodeOcr, sourceLanguageReadable);
      }
    } else {
      // We already have the engine initialized, so just start the camera.
      resumeOCR();
    }
  }
  
  
  void resumeOCR() {
    Log.d(TAG, "resumeOCR()");
    
   
    isEngineReady = true;
    
    isPaused = false;

    if (handler != null) {
      handler.resetState();
    }
    if (baseApi != null) {
      baseApi.setPageSegMode(pageSegmentationMode);
      baseApi.setVariable(TessBaseAPI.VAR_CHAR_BLACKLIST, characterBlacklist);
      baseApi.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, characterWhitelist);
    }

    if (hasSurface) {
     
      initCamera(surfaceHolder);
    }
  }
  
  
  void onShutterButtonPressContinuous() {
    isPaused = true;
    handler.stop();  
    beepManager.playBeepSoundAndVibrate();
    if (lastResult != null) {
      handleOcrDecode(lastResult);
    } else {
      Toast toast = Toast.makeText(this, "OCR failed. Please try again.", Toast.LENGTH_SHORT);
      toast.setGravity(Gravity.TOP, 0, 0);
      toast.show();
      resumeContinuousDecoding();
    }
  }

  @SuppressWarnings("unused")
  void resumeContinuousDecoding() {
    isPaused = false;
    resetStatusView();
    setStatusViewForContinuous();
    handler.resetState();
    if (shutterButton != null && DISPLAY_SHUTTER_BUTTON) {
      shutterButton.setVisibility(View.VISIBLE);
      done_Btn.setVisibility(View.GONE);
    }
  }

  @Override
  public void surfaceCreated(SurfaceHolder holder) {
    Log.d(TAG, "surfaceCreated()");
    
    if (holder == null) {
      Log.e(TAG, "surfaceCreated gave us a null surface");
    }
    
    
    if (!hasSurface && isEngineReady) {
      Log.d(TAG, "surfaceCreated(): calling initCamera()...");
      initCamera(holder);
    }
    hasSurface = true;
  }
  
 
  private void initCamera(SurfaceHolder surfaceHolder) {
    Log.d(TAG, "initCamera()");
    try {

     
      cameraManager.openDriver(surfaceHolder);
      
      
      handler = new CaptureActivityHandler(this, cameraManager, baseApi, isContinuousModeActive);
      
    } catch (IOException ioe) {
      showErrorMessage("Error", "Could not initialize camera. Please try restarting device.");
    } catch (RuntimeException e) {
      
      showErrorMessage("Error", "Could not initialize camera. Please try restarting device.");
    }   
  }
  
  @Override
  protected void onPause() {
    if (handler != null) {
      handler.quitSynchronously();
    }
    
    
    cameraManager.closeDriver();

    if (!hasSurface) {
      SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
      SurfaceHolder surfaceHolder = surfaceView.getHolder();
      surfaceHolder.removeCallback(this);
    }
    super.onPause();
  }

  void stopHandler() {
    if (handler != null) {
      handler.stop();
    }
  }

  @Override
  protected void onDestroy() {
    if (baseApi != null) {
      baseApi.end();
    }
    super.onDestroy();
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK) {

      
      if (isPaused) {
        Log.d(TAG, "only resuming continuous recognition, not quitting...");
        resumeContinuousDecoding();
        return true;
      }

     
      if (lastResult == null) {
        setResult(RESULT_CANCELED);
        finish();
        return true;
      } else {
        
        resetStatusView();
        if (handler != null) {
          handler.sendEmptyMessage(R.id.restart_preview);
        }
        return true;
      }
    } else if (keyCode == KeyEvent.KEYCODE_CAMERA) {
      if (isContinuousModeActive) {
        onShutterButtonPressContinuous();
      } else {
        handler.hardwareShutterButtonClick();
      }
      return true;
    } else if (keyCode == KeyEvent.KEYCODE_FOCUS) {      
      
      if (event.getRepeatCount() == 0) {
        handler.requestDelayedAutofocus(500L, R.id.user_requested_auto_focus);
      }
      return true;
    }
    return super.onKeyDown(keyCode, event);
  }

  

 

  public void surfaceDestroyed(SurfaceHolder holder) {
    hasSurface = false;
  }

  public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
  }

  /** Sets the necessary language code values for the given OCR language. */
  private boolean setSourceLanguage(String languageCode) {
    sourceLanguageCodeOcr = languageCode;
    sourceLanguageCodeTranslation = LanguageCodeHelper.mapLanguageCode(languageCode);
    sourceLanguageReadable = LanguageCodeHelper.getOcrLanguageName(this, languageCode);
    return true;
  }

  /** Sets the necessary language code values for the translation target language. */
  private boolean setTargetLanguage(String languageCode) {
    targetLanguageCodeTranslation = languageCode;
    targetLanguageReadable = LanguageCodeHelper.getTranslationLanguageName(this, languageCode);
    return true;
  }


  private File getStorageDirectory() {
   
    String state = null;
    try {
      state = Environment.getExternalStorageState();
    } catch (RuntimeException e) {
      Log.e(TAG, "Is the SD card visible?", e);
      showErrorMessage("Error", "Required external storage (such as an SD card) is unavailable.");
    }
    
    if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {

      
      
      try {
        return getExternalFilesDir(Environment.MEDIA_MOUNTED);
      } catch (NullPointerException e) {
        // We get an error here if the SD card is visible, but full
        Log.e(TAG, "External storage is unavailable");
        showErrorMessage("Error", "Required external storage (such as an SD card) is full or unavailable.");
      }
      
      
    
    } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
    	
    	Log.e(TAG, "External storage is read-only");
      showErrorMessage("Error", "Required external storage (such as an SD card) is unavailable for data storage.");
    } else {
    	
    	Log.e(TAG, "External storage is unavailable");
    	showErrorMessage("Error", "Required external storage (such as an SD card) is unavailable.");
    }
    return null;
  }

 
  private void initOcrEngine(File storageRoot, String languageCode, String languageName) {    
    isEngineReady = false;
    
    
    if (dialog != null) {
      dialog.dismiss();
    }
    dialog = new ProgressDialog(this);
    
    
    if (ocrEngineMode != TessBaseAPI.OEM_CUBE_ONLY) {
      for (String s : CUBE_REQUIRED_LANGUAGES) {
        if (s.equals(languageCode)) {
          ocrEngineMode = TessBaseAPI.OEM_CUBE_ONLY;
          SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
          prefs.edit().putString("preference_ocr_engine_mode", getOcrEngineModeName()).commit();
        }
      }
    }

   
    if (ocrEngineMode != TessBaseAPI.OEM_TESSERACT_ONLY) {
      boolean cubeOk = false;
      for (String s : CUBE_SUPPORTED_LANGUAGES) {
        if (s.equals(languageCode)) {
          cubeOk = true;
        }
      }
      
    }
    
    // Display the name of the OCR engine we're initializing in the indeterminate progress dialog box
    indeterminateDialog = new ProgressDialog(this);
    indeterminateDialog.setTitle("Please wait");
    String ocrEngineModeName = getOcrEngineModeName();
    if (ocrEngineModeName.equals("Both")) {
      indeterminateDialog.setMessage("Initializing OCR engines for " + languageName + "...");
    } else {
      indeterminateDialog.setMessage("Initializing OCR engine for " + languageName + "...");
    }
    indeterminateDialog.setCancelable(false);
    indeterminateDialog.show();
    
    if (handler != null) {
      handler.quitSynchronously();     
    }

    // Disable continuous mode if we're using Cube. This will prevent bad states for devices 
    // with low memory that crash when running OCR with Cube, and prevent unwanted delays.
    if (ocrEngineMode == TessBaseAPI.OEM_CUBE_ONLY || ocrEngineMode == TessBaseAPI.OEM_TESSERACT_CUBE_COMBINED) {
      Log.d(TAG, "Disabling continuous preview");
      isContinuousModeActive = false;
      SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
      prefs.edit().putBoolean("preference_capture_continuous", false);
    }
    
    // Start AsyncTask to install language data and init OCR
    baseApi = new TessBaseAPI();
    new OcrInitAsyncTask(this, baseApi, dialog, indeterminateDialog, languageCode, languageName, ocrEngineMode)
      .execute(storageRoot.toString());
  }
  
 
  boolean handleOcrDecode(OcrResult ocrResult) {
    lastResult = ocrResult;
    
    try {
      // Test whether the result is null
      ocrResult.getText();
    } catch (NullPointerException e) {
      Toast toast = Toast.makeText(this, "OCR failed. Please try again.", Toast.LENGTH_SHORT);
      toast.setGravity(Gravity.TOP, 0, 0);
      toast.show();
      resumeContinuousDecoding();
      return false;
    }
    
    // Turn off capture-related UI elements
    shutterButton.setVisibility(View.GONE);
    statusViewBottom.setVisibility(View.GONE);
    statusViewTop.setVisibility(View.GONE);
    cameraButtonView.setVisibility(View.GONE);
    viewfinderView.setVisibility(View.GONE);
    resultView.setVisibility(View.VISIBLE);
    done_Btn.setVisibility(View.VISIBLE);

    ImageView bitmapImageView = (ImageView) findViewById(R.id.image_view);
    lastBitmap = ocrResult.getBitmap();
    if (lastBitmap == null) {
      bitmapImageView.setImageBitmap(BitmapFactory.decodeResource(getResources(),
          R.drawable.ic_launcher));
    } else {
      bitmapImageView.setImageBitmap(lastBitmap);
    }

    // Display the recognized text
    TextView sourceLanguageTextView = (TextView) findViewById(R.id.source_language_text_view);
    sourceLanguageTextView.setText(sourceLanguageReadable);
    TextView ocrResultTextView = (TextView) findViewById(R.id.ocr_result_text_view);
    ocrResultTextView.setText(ocrResult.getText());
    ocrText=ocrResult.getText();
    
    // Crudely scale betweeen 22 and 32 -- bigger font for shorter text
    int scaledSize = Math.max(22, 32 - ocrResult.getText().length() / 4);
    ocrResultTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, scaledSize);

    TextView translationLanguageLabelTextView = (TextView) findViewById(R.id.translation_language_label_text_view);
    TextView translationLanguageTextView = (TextView) findViewById(R.id.translation_language_text_view);
    TextView translationTextView = (TextView) findViewById(R.id.translation_text_view);
    if (isTranslationActive) {
      // Handle translation text fields
      translationLanguageLabelTextView.setVisibility(View.VISIBLE);
      translationLanguageTextView.setText(targetLanguageReadable);
      translationLanguageTextView.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL), Typeface.NORMAL);
      translationLanguageTextView.setVisibility(View.VISIBLE);

      // Activate/re-activate the indeterminate progress indicator
      translationTextView.setVisibility(View.GONE);
      progressView.setVisibility(View.VISIBLE);
      setProgressBarVisibility(true);
      
    } else {
      translationLanguageLabelTextView.setVisibility(View.GONE);
      translationLanguageTextView.setVisibility(View.GONE);
      translationTextView.setVisibility(View.GONE);
      progressView.setVisibility(View.GONE);
      setProgressBarVisibility(false);
    }
    return true;
  }
  
 
  void handleOcrContinuousDecode(OcrResult ocrResult) {
   
    lastResult = ocrResult;
    
    // Send an OcrResultText object to the ViewfinderView for text rendering
    viewfinderView.addResultText(new OcrResultText(ocrResult.getText(), 
                                                   ocrResult.getWordConfidences(),
                                                   ocrResult.getMeanConfidence(),
                                                   ocrResult.getBitmapDimensions(),
                                                   ocrResult.getCharacterBoundingBoxes(),
                                                   ocrResult.getWordBoundingBoxes(),
                                                   ocrResult.getTextlineBoundingBoxes(),
                                                   ocrResult.getRegionBoundingBoxes()));

    Integer meanConfidence = ocrResult.getMeanConfidence();
    
    if (CONTINUOUS_DISPLAY_RECOGNIZED_TEXT) {
      // Display the recognized text on the screen
      statusViewTop.setText(ocrResult.getText());
      int scaledSize = Math.max(22, 32 - ocrResult.getText().length() / 4);
      statusViewTop.setTextSize(TypedValue.COMPLEX_UNIT_SP, scaledSize);
      statusViewTop.setTextColor(Color.BLACK);
      statusViewTop.setBackgroundResource(R.color.status_top_text_background);

      statusViewTop.getBackground().setAlpha(meanConfidence * (255 / 100));
    }

    if (CONTINUOUS_DISPLAY_METADATA) {
      // Display recognition-related metadata at the bottom of the screen
      long recognitionTimeRequired = ocrResult.getRecognitionTimeRequired();
      statusViewBottom.setTextSize(14);
      statusViewBottom.setText("OCR: " + sourceLanguageReadable + " - Mean confidence: " + 
          meanConfidence.toString() + " - Time required: " + recognitionTimeRequired + " ms");
    }
  }
  
 
  void handleOcrContinuousDecode(OcrResultFailure obj) {
    lastResult = null;
    viewfinderView.removeResultText();
    
    // Reset the text in the recognized text box.
    statusViewTop.setText("");

    if (CONTINUOUS_DISPLAY_METADATA) {
      // Color text delimited by '-' as red.
      statusViewBottom.setTextSize(14);
      CharSequence cs = setSpanBetweenTokens("OCR: " + sourceLanguageReadable + " - OCR failed - Time required: " 
          + obj.getTimeRequired() + " ms", "-", new ForegroundColorSpan(0xFFFF0000));
      statusViewBottom.setText(cs);
    }
  }
  
  
  private CharSequence setSpanBetweenTokens(CharSequence text, String token,
      CharacterStyle... cs) {
    // Start and end refer to the points where the span will apply
    int tokenLen = token.length();
    int start = text.toString().indexOf(token) + tokenLen;
    int end = text.toString().indexOf(token, start);

    if (start > -1 && end > -1) {
      // Copy the spannable string to a mutable spannable string
      SpannableStringBuilder ssb = new SpannableStringBuilder(text);
      for (CharacterStyle c : cs)
        ssb.setSpan(c, start, end, 0);
      text = ssb;
    }
    return text;
  }
  
  
 
  
  private void resetStatusView() {
    resultView.setVisibility(View.GONE);
    if (CONTINUOUS_DISPLAY_METADATA) {
      statusViewBottom.setText("");
      statusViewBottom.setTextSize(14);
      statusViewBottom.setTextColor(getResources().getColor(R.color.status_text));
      statusViewBottom.setVisibility(View.VISIBLE);
    }
    if (CONTINUOUS_DISPLAY_RECOGNIZED_TEXT) {
      statusViewTop.setText("");
      statusViewTop.setTextSize(14);
      statusViewTop.setVisibility(View.VISIBLE);
    }
    viewfinderView.setVisibility(View.VISIBLE);
    cameraButtonView.setVisibility(View.VISIBLE);
    if (DISPLAY_SHUTTER_BUTTON) {
      shutterButton.setVisibility(View.VISIBLE);
      done_Btn.setVisibility(View.GONE);
    }
    lastResult = null;
    viewfinderView.removeResultText();
  }
  
  
  void showLanguageName() {   
    Toast toast = Toast.makeText(this, "OCR: " + sourceLanguageReadable, Toast.LENGTH_LONG);
    toast.setGravity(Gravity.TOP, 0, 0);
    toast.show();
  }
  
  
  void setStatusViewForContinuous() {
    viewfinderView.removeResultText();
    if (CONTINUOUS_DISPLAY_METADATA) {
      statusViewBottom.setText("OCR: " + sourceLanguageReadable + " - waiting for OCR...");
    }
  }
  
  @SuppressWarnings("unused")
  void setButtonVisibility(boolean visible) {
    if (shutterButton != null && visible == true && DISPLAY_SHUTTER_BUTTON) {
      shutterButton.setVisibility(View.VISIBLE);
      done_Btn.setVisibility(View.GONE);
    } else if (shutterButton != null) {
      shutterButton.setVisibility(View.GONE);
      done_Btn.setVisibility(View.VISIBLE);
    }
  }
  
  
  void setShutterButtonClickable(boolean clickable) {
    shutterButton.setClickable(clickable);
  }

  
  void drawViewfinder() {
    viewfinderView.drawViewfinder();
  }
  
  @Override
  public void onShutterButtonClick(ShutterButton b) {
    if (isContinuousModeActive) {
      onShutterButtonPressContinuous();
    } else {
      if (handler != null) {
        handler.shutterButtonClick();
      } else {
        // Null handler. Why?
        showErrorMessage("Null handler error", "Please report this error along with what type of device you are using.");
      }
    }
  }

  @Override
  public void onShutterButtonFocus(ShutterButton b, boolean pressed) {
    requestDelayedAutofocus();
  }
  
  
  private void requestDelayedAutofocus() {
    
    if (handler != null) {
      handler.requestDelayedAutofocus(350L, R.id.user_requested_auto_focus);
    }
  }
  
  static boolean getFirstLaunch() {
    return isFirstLaunch;
  }
  
  
  String getOcrEngineModeName() {
    String ocrEngineModeName = "";
    String[] ocrEngineModes = getResources().getStringArray(R.array.ocrenginemodes);
    if (ocrEngineMode == TessBaseAPI.OEM_TESSERACT_ONLY) {
      ocrEngineModeName = ocrEngineModes[0];
    } else if (ocrEngineMode == TessBaseAPI.OEM_CUBE_ONLY) {
      ocrEngineModeName = ocrEngineModes[1];
    } else if (ocrEngineMode == TessBaseAPI.OEM_TESSERACT_CUBE_COMBINED) {
      ocrEngineModeName = ocrEngineModes[2];
    }
    return ocrEngineModeName;
  }
  
 
  private void retrievePreferences() {
      prefs = PreferenceManager.getDefaultSharedPreferences(this);
      
      // Retrieve from preferences, and set in this Activity, the language preferences
      PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
      setSourceLanguage(prefs.getString("sourceLanguageCodeOcrPref", CaptureActivity.DEFAULT_SOURCE_LANGUAGE_CODE));
      setTargetLanguage(prefs.getString("targetLanguageCodeTranslationPref", CaptureActivity.DEFAULT_TARGET_LANGUAGE_CODE));
      isTranslationActive = prefs.getBoolean("preference_translation_toggle_translation", false);
      
      // Retrieve from preferences, and set in this Activity, the capture mode preference
      if (prefs.getBoolean("preference_capture_continuous", CaptureActivity.DEFAULT_TOGGLE_CONTINUOUS)) {
        isContinuousModeActive = true;
      } else {
        isContinuousModeActive = false;
      }

      // Retrieve from preferences, and set in this Activity, the page segmentation mode preference
      String[] pageSegmentationModes = getResources().getStringArray(R.array.pagesegmentationmodes);
      String pageSegmentationModeName = prefs.getString("preference_page_segmentation_mode", pageSegmentationModes[0]);
      if (pageSegmentationModeName.equals(pageSegmentationModes[0])) {
        pageSegmentationMode = TessBaseAPI.PSM_AUTO_OSD;
      } else if (pageSegmentationModeName.equals(pageSegmentationModes[1])) {
        pageSegmentationMode = TessBaseAPI.PSM_AUTO;
      } else if (pageSegmentationModeName.equals(pageSegmentationModes[2])) {
        pageSegmentationMode = TessBaseAPI.PSM_SINGLE_BLOCK;
      } else if (pageSegmentationModeName.equals(pageSegmentationModes[3])) {
        pageSegmentationMode = TessBaseAPI.PSM_SINGLE_CHAR;
      } else if (pageSegmentationModeName.equals(pageSegmentationModes[4])) {
        pageSegmentationMode = TessBaseAPI.PSM_SINGLE_COLUMN;
      } else if (pageSegmentationModeName.equals(pageSegmentationModes[5])) {
        pageSegmentationMode = TessBaseAPI.PSM_SINGLE_LINE;
      } else if (pageSegmentationModeName.equals(pageSegmentationModes[6])) {
        pageSegmentationMode = TessBaseAPI.PSM_SINGLE_WORD;
      } else if (pageSegmentationModeName.equals(pageSegmentationModes[7])) {
        pageSegmentationMode = TessBaseAPI.PSM_SINGLE_BLOCK_VERT_TEXT;
      }
      
      // Retrieve from preferences, and set in this Activity, the OCR engine mode
      String[] ocrEngineModes = getResources().getStringArray(R.array.ocrenginemodes);
      String ocrEngineModeName = prefs.getString("preference_ocr_engine_mode", ocrEngineModes[0]);
      if (ocrEngineModeName.equals(ocrEngineModes[0])) {
        ocrEngineMode = TessBaseAPI.OEM_TESSERACT_ONLY;
      } else if (ocrEngineModeName.equals(ocrEngineModes[1])) {
        ocrEngineMode = TessBaseAPI.OEM_CUBE_ONLY;
      } else if (ocrEngineModeName.equals(ocrEngineModes[2])) {
        ocrEngineMode = TessBaseAPI.OEM_TESSERACT_CUBE_COMBINED;
      }
      
      // Retrieve from preferences, and set in this Activity, the character blacklist and whitelist
      characterBlacklist = OcrCharacterHelper.getBlacklist(prefs, sourceLanguageCodeOcr);
      characterWhitelist = OcrCharacterHelper.getWhitelist(prefs, sourceLanguageCodeOcr);
      
      prefs.registerOnSharedPreferenceChangeListener(listener);
      
      beepManager.updatePrefs();
  }
  
  /**
   * Sets default values for preferences. To be called the first time this app is run.
   */
  private void setDefaultPreferences() {
    prefs = PreferenceManager.getDefaultSharedPreferences(this);

    // Continuous preview
    prefs.edit().putBoolean("preference_capture_continuous", CaptureActivity.DEFAULT_TOGGLE_CONTINUOUS).commit();

    // Recognition language
    prefs.edit().putString("sourceLanguageCodeOcrPref", CaptureActivity.DEFAULT_SOURCE_LANGUAGE_CODE).commit();

   
    // Translation target language
    prefs.edit().putString("targetLanguageCodeTranslationPref", CaptureActivity.DEFAULT_TARGET_LANGUAGE_CODE).commit();

    
    // OCR Engine
    prefs.edit().putString("preference_ocr_engine_mode", CaptureActivity.DEFAULT_OCR_ENGINE_MODE).commit();

    // Beep
    prefs.edit().putBoolean("preferences_play_beep", CaptureActivity.DEFAULT_TOGGLE_BEEP).commit();

    // Character blacklist
    prefs.edit().putString("preference_character_blacklist", 
        OcrCharacterHelper.getDefaultBlacklist(CaptureActivity.DEFAULT_SOURCE_LANGUAGE_CODE)).commit();

    // Character whitelist
    prefs.edit().putString("preference_character_whitelist", 
        OcrCharacterHelper.getDefaultWhitelist(CaptureActivity.DEFAULT_SOURCE_LANGUAGE_CODE)).commit();

    // Page segmentation mode
    prefs.edit().putString("preference_page_segmentation_mode", CaptureActivity.DEFAULT_PAGE_SEGMENTATION_MODE).commit();

    // Reversed camera image
    prefs.edit().putBoolean("preferences_reverse_image", CaptureActivity.DEFAULT_TOGGLE_REVERSED_IMAGE).commit();
    
    // Light
    prefs.edit().putBoolean("preference_toggle_light", CaptureActivity.DEFAULT_TOGGLE_LIGHT).commit();
  }
  
  
  void showErrorMessage(String title, String message) {
	  new AlertDialog.Builder(this)
	    .setTitle(title)
	    .setMessage(message)
	    .setOnCancelListener(new FinishListener(this))
	    .setPositiveButton( "Done", new FinishListener(this))
	    .show();
  }
}
