
package android.ssd;

import java.util.List;

import android.graphics.Point;
import android.graphics.Rect;


public final class OcrResultText {
  private final String text;
  
  private final int[] wordConfidences;
  private final int meanConfidence;
  private final Point bitmapDimensions;
  private final List<Rect> wordBoundingBoxes;
  private final List<Rect> characterBoundingBoxes;
  private final List<Rect> textlineBoundingBoxes;
  private final List<Rect> regionBoundingBoxes;
  
  public OcrResultText(String text,
                   int[] wordConfidences,
                   int meanConfidence,
                   Point bitmapDimensions,
                   List<Rect> characterBoundingBoxes,
                   List<Rect> wordBoundingBoxes,
                   List<Rect> textlineBoundingBoxes,
                   List<Rect> regionBoundingBoxes) {
    this.text = text;
    this.wordConfidences = wordConfidences;
    this.meanConfidence = meanConfidence;
    this.bitmapDimensions = bitmapDimensions;
    this.characterBoundingBoxes = characterBoundingBoxes;
    this.wordBoundingBoxes = wordBoundingBoxes;
    this.textlineBoundingBoxes = textlineBoundingBoxes;
    this.regionBoundingBoxes = regionBoundingBoxes;
  }

  public String getText() {
    return text;
  }

  public Point getBitmapDimensions() {
    return bitmapDimensions;
  }
  
  public int[] getWordConfidences() {
    return wordConfidences;
  }

  public int getMeanConfidence() {
    return meanConfidence;
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
  
  @Override
  public String toString() {
    return text + " " + meanConfidence;
  }
}