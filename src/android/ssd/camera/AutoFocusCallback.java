
package android.ssd.camera;

import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


final class AutoFocusCallback implements Camera.AutoFocusCallback {
  private static final String TAG = AutoFocusCallback.class.getSimpleName();

  private Handler autoFocusHandler;
  private int autoFocusMessage;

  void setHandler(Handler autoFocusHandler, int autoFocusMessage) {
    this.autoFocusHandler = autoFocusHandler;
    this.autoFocusMessage = autoFocusMessage;
  }

  @Override
  public void onAutoFocus(boolean success, Camera camera) {
    if (autoFocusHandler != null) {
      Message message = autoFocusHandler.obtainMessage(autoFocusMessage, success);
      Bundle bundle = new Bundle();
      bundle.putBoolean("success", success);
      message.setData(bundle);
      autoFocusHandler.sendMessage(message);
      autoFocusHandler = null;
    } else {
      Log.d(TAG, "Got auto-focus callback, but no handler for it");
    }
  }
}
