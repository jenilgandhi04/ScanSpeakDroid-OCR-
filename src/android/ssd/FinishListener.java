package android.ssd;


import android.app.Activity;
import android.content.DialogInterface;

final class FinishListener
    implements DialogInterface.OnClickListener, DialogInterface.OnCancelListener, Runnable {

  private final Activity activityToFinish;

  FinishListener(Activity activityToFinish) {
    this.activityToFinish = activityToFinish;
  }

  public void onCancel(DialogInterface dialogInterface) {
    run();
  }

  public void onClick(DialogInterface dialogInterface, int i) {
    run();
  }

  public void run() {
    activityToFinish.finish();
  }

}