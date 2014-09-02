package android.ssd;

import android.app.Activity;
import android.app.AlertDialog;

import android.content.DialogInterface;
import android.content.Intent;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;

import android.view.MotionEvent;
import android.view.View;

import android.view.View.OnTouchListener;

import android.widget.ImageView;

import android.widget.TextView;
import android.widget.Toast;

public class HelloActivity extends Activity implements OnTouchListener {
	
	ImageView view;
	TextView tv;
	 private static final int ABOUT_ID = Menu.FIRST + 1;

	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		this.getIntent().setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		tv = (TextView) findViewById(R.id.lb1);
		tv.setTypeface(Typeface.SANS_SERIF);
		tv.setTextSize(35);
		tv.setPadding(40, 0, 0, 0);
		tv = (TextView) findViewById(R.id.textView1);
		tv.setTypeface(Typeface.SANS_SERIF);
		tv.setTextSize(25);
		tv.setPadding(60, 0, 0, 0);

		view = (ImageView) findViewById(R.id.imageView2);
		view.getLayoutParams().height = 320;
		view.setOnTouchListener(this);

	}

	public boolean onTouch(View v, MotionEvent event) {
		HelloActivity.this.finish();
		Intent intent = new Intent();
		try {
			intent.setClass(HelloActivity.this, PhotoOpt.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		} catch (Exception e) {
			Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG)
					.show();
		}

		return false;
	}
	
	@Override
	public void onBackPressed() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Are you sure you want to exit?").setCancelable(
				false).setPositiveButton("Yes",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						HelloActivity.this.finish();
					}
				}).setNegativeButton("No",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();

	}
}
