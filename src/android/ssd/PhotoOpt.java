package android.ssd;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

public class PhotoOpt extends Activity{
	ImageButton fol,cam;
	ImageView iv;
	Uri imageUri;
	static Bitmap b;
    public static String	path ;
    static String imagepath= " ";
    
    
	 public void onCreate(Bundle savedInstanceState) {
    	 super.onCreate(savedInstanceState);
         setContentView(R.layout.imgopt);
         iv=(ImageView) findViewById(R.id.imageView2);
         iv.getLayoutParams().height=25;
         iv=(ImageView) findViewById(R.id.imageView1);
         iv.getLayoutParams().height=250;
        
       
         fol=(ImageButton)findViewById(R.id.imageButton1);
         fol.getLayoutParams().width=160;
        
        fol.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				
				try{
				Intent in=new Intent();
				in.setType("image/*");
				in.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(Intent.createChooser(in,"Select Picture"),1);
				}
				catch(Exception e){
					Toast.makeText(getBaseContext(),e.getMessage(),Toast.LENGTH_LONG).show();
				}
			}
		});
         cam=(ImageButton)findViewById(R.id.imageButton2);
         cam.getLayoutParams().width=160;
         cam.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				
				Intent intent = new Intent();
				try {
					intent.setClass(PhotoOpt.this, CaptureActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
				} catch (Exception e) {
					Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG)
							.show();
				}
				/*Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				startActivityForResult(i, 2);*/
			}
		});
	 }
	 
	 @Override
	 protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		 super.onActivityResult(requestCode, resultCode, data);
		 Intent intent = new Intent();
		 
		    switch (requestCode) {
		        case 1:
		       
		        
		            if(resultCode== RESULT_OK &&data != null && data.getData() != null){
		                Uri _uri = data.getData();

		                if (_uri != null) {
		                    //User had pick an image.
		                    Cursor cursor = getContentResolver().query(_uri, new String[] { android.provider.MediaStore.Images.ImageColumns.DATA }, null, null, null);
		                    cursor.moveToFirst();

		                    //Link to the image
		                    imagepath = cursor.getString(0);
		                    Log.v("imageFilePath", imagepath);
		                    File photos= new File(imagepath);
		                    b = decodeFile(photos);
		                    b = Bitmap.createScaledBitmap(b,150, 150, true);
		                    EditSnap.b=b;
		                    if(EditSnap.iv!=null){
			                    EditSnap.iv.setImageBitmap(b);
			                    }
		                    cursor.close();
		                }
		                try{
							intent.setClass(PhotoOpt.this,EditSnap.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
							
							startActivity(intent);
							}
							catch(Exception e){
								Toast.makeText(getBaseContext(),e.getMessage(),Toast.LENGTH_LONG).show();
							}
		            }
		            
					

		        
		            break;
		       /* case 2:
		        	if (resultCode == RESULT_OK) {
		        		  String[] projection = { MediaStore.Images.Media.DATA}; 
		                  Cursor cursor = managedQuery(imageUri, projection, null, null, null); 
		                  int column_index_data = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA); 
		                  cursor.moveToFirst(); 
		                  String capturedImageFilePath = cursor.getString(column_index_data);
		                  Log.v("imageFilePath", capturedImageFilePath);
		                    File photos= new File(capturedImageFilePath);
		                    b = decodeFile(photos);
		                    b = Bitmap.createScaledBitmap(b,150, 150, true);
		                    EditSnap.b=b;
		                    if(EditSnap.iv!=null){
		                    EditSnap.iv.setImageBitmap(b);
		                    }
		                    cursor.close();
		                    
		                	try{
		    					intent.setClass(PhotoOpt.this,EditSnap.class);
		    					intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		    					
		    					startActivity(intent);
		    					}
		    					catch(Exception e){
		    						Toast.makeText(getBaseContext(),e.getMessage(),Toast.LENGTH_LONG).show();
		    					}
		        	}
		   
	                 break;*/
		        }
		    
}
	 private Bitmap decodeFile(File f){
	        try {
	            //decode image size
	            BitmapFactory.Options o = new BitmapFactory.Options();
	            o.inJustDecodeBounds = true;
	            BitmapFactory.decodeStream(new FileInputStream(f),null,o);

	            //Find the correct scale value. It should be the power of 2.
	            final int REQUIRED_SIZE=70;
	            int width_tmp=o.outWidth, height_tmp=o.outHeight;
	            int scale=1;
	            while(true){
	                if(width_tmp/2<REQUIRED_SIZE || height_tmp/2<REQUIRED_SIZE)
	                    break;
	                width_tmp/=2;
	                height_tmp/=2;
	                scale++;
	            }

	            //decode with inSampleSize
	            BitmapFactory.Options o2 = new BitmapFactory.Options();
	            o2.inSampleSize=scale;
	            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
	        } catch (FileNotFoundException e) {}
	        return null;
	    }
	 @Override
	 public void onBackPressed() {
	     AlertDialog.Builder builder = new AlertDialog.Builder(this);
	     builder.setMessage("Are you sure you want to exit?")
	            .setCancelable(false)
	            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int id) {
	                	
	                    PhotoOpt.this.finish();
	                     
	  
	                }
	            })
	            .setNegativeButton("No", new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int id) {
	                     dialog.cancel();
	                }
	            });
	     AlertDialog alert = builder.create();
	     alert.show();

	 }
}