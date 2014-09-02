package android.ssd;



//package com.javacodegeeks.android.tts;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class TtsActivity extends Activity implements OnInitListener {
	
	private int MY_DATA_CHECK_CODE = 0;
	
	private TextToSpeech tts;
	 private static final int ABOUT_ID = Menu.FIRST + 1;
	private EditText inputText;
	private Button speakButton,emailButton,msgButton;
	
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
		
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tts);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        inputText = (EditText) findViewById(R.id.editText1);
        inputText.setText(CaptureActivity.ocrText);
        msgButton = (Button) findViewById(R.id.button2);
       msgButton.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				Intent sendIntent = new Intent(Intent.ACTION_VIEW);         
				sendIntent.setData(Uri.parse("sms:"));
				sendIntent.putExtra("sms_body",inputText.getText().toString());
				startActivity(sendIntent);
			}
		});
       emailButton = (Button) findViewById(R.id.button3);
       emailButton.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				try{
					Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

					/* Fill it with Data */
					emailIntent.setType("plain/text");
					emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"to@email.com"});
					emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject");
					emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, inputText.getText().toString());

					/* Send it off to the Activity-Chooser */
					startActivity(Intent.createChooser(emailIntent, "Send mail..."));
				}
				catch(Exception e){
					Toast.makeText(getBaseContext(),e.getMessage(),Toast.LENGTH_LONG).show();
				}
			}
		});
        
        speakButton = (Button) findViewById(R.id.button1);
       speakButton.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				String text = inputText.getText().toString();
				if (text!=null && text.length()>0) {
					Toast.makeText(TtsActivity.this, "Saying: " + text, Toast.LENGTH_LONG).show();
					tts.setPitch(1.4f);
					tts.setSpeechRate(0.9f);
					tts.speak(text, TextToSpeech.QUEUE_ADD, null);
				}
			}
		});
        
        Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);
		
    }
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == MY_DATA_CHECK_CODE) {
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
				// success, create the TTS instance
				tts = new TextToSpeech(this, this);
			} 
			else {
				// missing data, install it
				Intent installIntent = new Intent();
				installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(installIntent);
			}
		}

	}

	@Override
	public void onInit(int status) {		
		if (status == TextToSpeech.SUCCESS) {
			Toast.makeText(TtsActivity.this, 
					"Text-To-Speech engine is initialized", Toast.LENGTH_LONG).show();
		}
		else if (status == TextToSpeech.ERROR) {
			Toast.makeText(TtsActivity.this, 
					"Error occurred while initializing Text-To-Speech engine", Toast.LENGTH_LONG).show();
		}
	}
	
	@Override
	public void onBackPressed() {
		Intent intent = new Intent();
		try {
		intent.setClass(TtsActivity.this,PhotoOpt.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		}
		catch (Exception e) {
			Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG)
					.show();
		}
		
	}
	
}