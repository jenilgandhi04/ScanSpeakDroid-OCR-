
package android.ssd;

import java.util.List;

//import com.googlecode.eyesfree.textdetect.Thresholder;
import com.googlecode.leptonica.android.Binarize;
import com.googlecode.leptonica.android.Pix;
import com.googlecode.leptonica.android.ReadFile;
import com.googlecode.leptonica.android.WriteFile;
import com.googlecode.tesseract.android.TessBaseAPI;

//import edu.sfsu.cs.orange.ocr.language.PseudoTranslator;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


final class OcrRecognizeAsyncTask extends AsyncTask<String, String, Boolean> {

  
  private CaptureActivity activity;
  
  private TessBaseAPI baseApi;
  private Bitmap bitmap;
  private OcrResult ocrResult;
  private OcrResultFailure ocrResultFailure;
  private boolean isContinuous;
  private ProgressDialog indeterminateDialog;
  private long start;
  private long end;

  
  // Constructor for single-shot mode
  OcrRecognizeAsyncTask(CaptureActivity activity, TessBaseAPI baseApi, 
      ProgressDialog indeterminateDialog, Bitmap bitmap) {
    this.activity = activity;
    this.baseApi = baseApi;
    this.indeterminateDialog = indeterminateDialog;
    this.bitmap = bitmap;
    isContinuous = false;
   
  }
 
  // Constructor for continuous recognition mode
  OcrRecognizeAsyncTask(CaptureActivity activity, TessBaseAPI baseApi, Bitmap bitmap) {
    this.activity = activity;
    this.baseApi = baseApi;
    this.bitmap = bitmap;
    isContinuous = true;
  
  }
  
  @Override
  protected Boolean doInBackground(String... arg0) {
    String textResult = null;   
    int[] wordConfidences = null;
    int overallConf = -1;
    start = System.currentTimeMillis();
    end = start;
    
    try {

      Log.e("OcrRecognizeAsyncTask", "converted to bitmap. doing setImage()...");
      Log.e("tess", "ayu...");
      baseApi.setImage(bitmap);
      
      Log.e("OcrRecognizeAsyncTask", "setImage() completed");
      textResult = baseApi.getUTF8Text();
      Log.e("OcrRecognizeAsyncTask", "getUTF8Text() completed");
      wordConfidences = baseApi.wordConfidences();
      overallConf = baseApi.meanConfidence();
      end = System.currentTimeMillis();
    } catch (RuntimeException e) {
      Log.e("OcrRecognizeAsyncTask", "Caught RuntimeException in request to Tesseract. Setting state to CONTINUOUS_STOPPED.");
      e.printStackTrace();
      try {
        baseApi.clear();
        
        	activity.stopHandler();
       
      } catch (NullPointerException e1) {
        // Continue
      }
      return false;
    }

    // Check for failure to recognize text
    if (textResult == null || textResult.equals("")) {
      ocrResultFailure = new OcrResultFailure(end - start);
      return false;
    }  
    
    // Get bounding boxes for characters and words
    List<Rect> wordBoxes = baseApi.getWords().getBoxRects();
    List<Rect> characterBoxes = baseApi.getCharacters().getBoxRects();
    List<Rect> textlineBoxes = baseApi.getTextlines().getBoxRects();
    List<Rect> regionBoxes = baseApi.getRegions().getBoxRects();

    ocrResult = new OcrResult(bitmap, textResult, wordConfidences, overallConf, characterBoxes, 
        textlineBoxes, wordBoxes, regionBoxes, (end - start));
    return true;
  }
  
  @Override
  protected void onPostExecute(Boolean result) {
    super.onPostExecute(result);
   
    Handler handler = null;
  
    handler = activity.getHandler();
   
    if (!isContinuous && handler != null) {
      // Send results for single-shot mode recognition.
      if (result) {
        Message message = Message.obtain(handler, R.id.ocr_decode_succeeded, ocrResult);
        message.sendToTarget();
      } else {
        bitmap.recycle();
        Message message = Message.obtain(handler, R.id.ocr_decode_failed, ocrResult);
        message.sendToTarget();
      }
     
      indeterminateDialog.dismiss();
    } else  if (handler != null) {
      // Send results for continuous mode recognition.
      if (result) {
        try {
          // Send the result to CaptureActivityHandler
          Message message = Message.obtain(handler, R.id.ocr_continuous_decode_succeeded, ocrResult);
          message.sendToTarget();
        } catch (NullPointerException e) {
       
          activity.stopHandler();
      
        }
      } else {
        bitmap.recycle();
        try {
          Message message = Message.obtain(handler, R.id.ocr_continuous_decode_failed, ocrResultFailure);
          message.sendToTarget();
        } catch (NullPointerException e) {
          activity.stopHandler();
        }
      }
    }
    if (baseApi != null) {
      baseApi.clear();
    }
  }
}
