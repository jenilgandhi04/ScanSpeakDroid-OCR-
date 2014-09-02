
package android.ssd;

public abstract class LuminanceSource {

  private final int width;
  private final int height;

  protected LuminanceSource(int width, int height) {
    this.width = width;
    this.height = height;
  }

  
  public abstract byte[] getRow(int y, byte[] row);

  
  public abstract byte[] getMatrix();

  
  public final int getWidth() {
    return width;
  }

 
  public final int getHeight() {
    return height;
  }

 
  public boolean isCropSupported() {
    return true;
  }

 
  public LuminanceSource crop(int left, int top, int width, int height) {
    throw new RuntimeException("This luminance source does not support cropping.");
  }

  
  public boolean isRotateSupported() {
    return false;
  }

 
  public LuminanceSource rotateCounterClockwise() {
    throw new RuntimeException("This luminance source does not support rotation.");
  }

}
