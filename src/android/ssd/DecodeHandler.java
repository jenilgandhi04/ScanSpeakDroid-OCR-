

package android.ssd;

import android.ssd.BeepManager;
import com.googlecode.tesseract.android.TessBaseAPI;

import android.ssd.CaptureActivity;
import android.ssd.OcrRecognizeAsyncTask;
import android.ssd.R;

import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;


final class DecodeHandler extends Handler {

  private CaptureActivity activity;
  
  private boolean running = true;
  private final TessBaseAPI baseApi;
  private BeepManager beepManager;
 
  
  private static boolean isDecodePending;

  DecodeHandler(CaptureActivity activity, TessBaseAPI baseApi) {
    this.activity = activity;
    this.baseApi = baseApi;
    
    beepManager = new BeepManager(activity);
    beepManager.updatePrefs();
   
  }
 

  @Override
  public void handleMessage(Message message) {
    if (!running) {
      return;
    }
    switch (message.what) {        
      case R.id.ocr_continuous_decode:
        // Only request a decode if a request is not already pending.
        if (!isDecodePending) {
          isDecodePending = true;
          ocrContinuousDecode((byte[]) message.obj, message.arg1, message.arg2);
        }
        break;
      case R.id.ocr_decode:
        ocrDecode((byte[]) message.obj, message.arg1, message.arg2);
        break;
      case R.id.quit:
        running = false;
        Looper.myLooper().quit();
        break;
    }
  }
  
  static void resetDecodeState() {
    isDecodePending = false;
  }
  
 
  private void ocrDecode(byte[] data, int width, int height) {
    //Log.d(TAG, "ocrDecode: Got R.id.ocr_decode message.");
    //Log.d(TAG, "width: " + width + ", height: " + height);
 
    beepManager.playBeepSoundAndVibrate();
    
    
    // Set up the indeterminate progress dialog box
    ProgressDialog indeterminateDialog = new ProgressDialog(activity);
    indeterminateDialog.setTitle("Please wait");    		
    String ocrEngineModeName = activity.getOcrEngineModeName();
    if (ocrEngineModeName.equals("Both")) {
      indeterminateDialog.setMessage("Performing OCR...");
    } else {
      indeterminateDialog.setMessage("Performing OCR...");
    }
    indeterminateDialog.setCancelable(false);
    indeterminateDialog.show();
    
    // Asyncrhonously launch the OCR process
    PlanarYUVLuminanceSource source = activity.getCameraManager().buildLuminanceSource(data, width, height);
   
    new OcrRecognizeAsyncTask(activity, baseApi, indeterminateDialog, source.renderCroppedGreyscaleBitmap()).execute();
  }
  
  
  private void ocrContinuousDecode(byte[] data, int width, int height) {
    // Asyncrhonously launch the OCR process
    PlanarYUVLuminanceSource source = activity.getCameraManager().buildLuminanceSource(data, width, height);
    new OcrRecognizeAsyncTask(activity, baseApi, source.renderCroppedGreyscaleBitmap()).execute();
  }
}











