
package android.ssd;

import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;


public final class OcrResult {
  private final Bitmap bitmap;
  private final String text;
  
  private final int[] wordConfidences;
  private final int meanConfidence;
  
  private final List<Rect> wordBoundingBoxes;
  private final List<Rect> characterBoundingBoxes;
  private final List<Rect> textlineBoundingBoxes;
  private final List<Rect> regionBoundingBoxes;
  
  private final long timestamp;
  private final long recognitionTimeRequired;

  private final Paint paint;
  
  public OcrResult(Bitmap bitmap,
                   String text,
                   int[] wordConfidences,
                   int meanConfidence,
                   List<Rect> characterBoundingBoxes,
                   List<Rect> textlineBoundingBoxes,
                   List<Rect> wordBoundingBoxes,
                   List<Rect> regionBoxes, 
                   long recognitionTimeRequired) {
    this.bitmap = bitmap;
    this.text = text;
    this.wordConfidences = wordConfidences;
    this.meanConfidence = meanConfidence;
    this.characterBoundingBoxes = characterBoundingBoxes;
    this.textlineBoundingBoxes = textlineBoundingBoxes;
    this.wordBoundingBoxes = wordBoundingBoxes;
    this.regionBoundingBoxes = regionBoxes;
    this.recognitionTimeRequired = recognitionTimeRequired;
    this.timestamp = System.currentTimeMillis();
    
    this.paint = new Paint();
  }

  public Bitmap getBitmap() {
    if (characterBoundingBoxes.isEmpty()) {
      return bitmap;
    } else {
      return getAnnotatedBitmap();
    }
  }
  
  private Bitmap getAnnotatedBitmap() {
    Canvas canvas = new Canvas(bitmap);
    
    // Draw bounding boxes around each word
    for (int i = 0; i < wordBoundingBoxes.size(); i++) {
      paint.setAlpha(0xA0);
      paint.setColor(0xFF00CCFF);
      paint.setStyle(Style.STROKE);
      paint.setStrokeWidth(3);
      Rect r = wordBoundingBoxes.get(i);
      canvas.drawRect(r, paint);
    }    
    

    
    return bitmap;
  }
  
  public String getText() {
    return text;
  }

  public int[] getWordConfidences() {
    return wordConfidences;
  }

  public int getMeanConfidence() {
    return meanConfidence;
  }

  public long getRecognitionTimeRequired() {
    return recognitionTimeRequired;
  }

  public Point getBitmapDimensions() {
    return new Point(bitmap.getWidth(), bitmap.getHeight()); 
  }
  
  public List<Rect> getCharacterBoundingBoxes() {
    return characterBoundingBoxes;
  }
  
  public List<Rect> getTextlineBoundingBoxes() {
    return textlineBoundingBoxes;
  }
  
  public List<Rect> getWordBoundingBoxes() {
    return wordBoundingBoxes;
  }
  
  public List<Rect> getRegionBoundingBoxes() {
    return regionBoundingBoxes;
  }
  
  public long getTimestamp() {
    return timestamp;
  }
  
  @Override
  public String toString() {
    return text + " " + meanConfidence + " " + recognitionTimeRequired + " " + timestamp;
  }
}
