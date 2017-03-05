package com.birdsquest.videorecorder;

import java.io.IOException;
import java.io.OutputStream;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class Recorder extends RelativeLayout{
	private Camera cameraObject;
	private CameraSurface showCamera;
	private SurfaceHolder holdMe;
	private ImageView pic;
	Context context;

	private Camera.PictureCallback capturedIt = new Camera.PictureCallback(){
		@Override public void onPictureTaken(byte[] data, Camera camera){
			Bitmap bitmap=BitmapFactory.decodeByteArray(data , 0, data .length);
			if(bitmap==null){
				Toast.makeText(context, "not taken", Toast.LENGTH_SHORT).show();}
			else{Toast.makeText(context, "taken", Toast.LENGTH_SHORT).show();}
			cameraObject.release();
		}	};

	OrientationEventListener mOrientationEventListener=null;
	int rotation =  -1;
	public void setOrientationEventListener(){
		if (mOrientationEventListener == null) {
			mOrientationEventListener = new OrientationEventListener(context, SensorManager.SENSOR_DELAY_NORMAL) {
				private static final int PORTRAIT_NORMAL =  0;
				private static final int LANDSCAPE_NORMAL =  90;
				private static final int PORTRAIT_INVERTED =  180;
				private static final int LANDSCAPE_INVERTED =  270;

				@Override
				public void onOrientationChanged(int orientation){
					int lastOrientation=rotation;
					rotation=
							(orientation>=315||orientation<45?PORTRAIT_NORMAL:
							(orientation<315&&orientation>=225?LANDSCAPE_NORMAL:
							(orientation<225&&orientation>=135?PORTRAIT_INVERTED:
							LANDSCAPE_INVERTED)));//orientation<135&&orientation>45
					if(lastOrientation!=rotation){changeRotation();}
				}
				private void changeRotation() {
					shutterButton.setImageDrawable(rotateImage(android.R.drawable.ic_menu_camera,rotation));
				}

				private Drawable rotateImage(int drawableId, int degrees) {
					/*Matrix matrix = new Matrix();
imageView.setScaleType(ImageView.ScaleType.MATRIX);   //required
matrix.postRotate((float) angle, imageView.getDrawable().getBounds().width()/2, imageView.getDrawable().getBounds().height()/2);
imageView.setImageMatrix(matrix);*/

					Bitmap original = BitmapFactory.decodeResource(getResources(), drawableId);
					Matrix matrix = new Matrix();
					matrix.postRotate(degrees);

					Bitmap rotated = Bitmap.createBitmap(original, 0, 0, original.getWidth(), original.getHeight(), matrix, true);
					return new BitmapDrawable(rotated);
				}
			};
		}
		if (mOrientationEventListener.canDetectOrientation()) {
			mOrientationEventListener.enable();
		}
	}
	public Recorder(Context context, AttributeSet attrs){super(context, attrs);init(context, attrs);}
	public void init(Context context, AttributeSet attrs){
		this.context=context;
		AddCamera();
		AddShutter();
		setOrientationEventListener();

	}

	private void AddCamera(){
		try{cameraObject=Camera.open();}
		catch(Exception e){e.printStackTrace();};
		showCamera=new CameraSurface(context, cameraObject);
		addView(showCamera);
	}

	FloatingActionButton shutterButton;
	private void AddShutter(){
		shutterButton=new FloatingActionButton(context);
		shutterButton.setImageResource(R.drawable.ic_menu_camera);
		shutterButton.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View view){
				snapIt(view);
			}
		});
		addView(shutterButton);
	}

	public void ReverseCamera(){

	}

	private int getDegrees(){
		switch(((Activity)context).getWindowManager().getDefaultDisplay().getRotation()){
			case Surface.ROTATION_0:   return 90;
			case Surface.ROTATION_90:  return 0;
			case Surface.ROTATION_180: return 270;
			case Surface.ROTATION_270: return 180;
		}
		return 0;
	}
	public void snapIt(View view){cameraObject.takePicture(null, null, capturedIt);}


	class CameraSurface extends SurfaceView implements SurfaceHolder.Callback{
		public CameraSurface(Context context, Camera camera){
			super(context);
			((Activity)context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			holdMe=getHolder();
			holdMe.addCallback(this);

			//If closing the camera within the application, don't forget to call thecamera.finalize()
		}

		@Override
		public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3){
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder){
			//theCamera = Camera.open();
			try{
				cameraObject.setDisplayOrientation(getDegrees());
				cameraObject.setPreviewDisplay(holder);
				cameraObject.startPreview();
			}catch(IOException e){e.printStackTrace();}
		}



		@Override
		public void surfaceDestroyed(SurfaceHolder arg0){
			if(cameraObject!=null){
				cameraObject.stopPreview();
				cameraObject.setPreviewCallback(null);
				cameraObject.release();
				cameraObject=null;
			}
		}

	}




	private Camera.PictureCallback mJpegCallback = new Camera.PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			try {
				// Populate image metadata

				ContentValues image = new ContentValues();
				// additional picture metadata
				//image.put(MediaStore.Images.Media.DISPLAY_NAME, [picture name]);
				//image.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");
				//image.put(MediaStore.Images.Media.TITLE, [picture title]);
				//image.put(MediaStore.Images.Media.DESCRIPTION, [picture description]);
				//image.put(MediaStore.Images.Media.DATE_ADDED, [some time]);
				//image.put(MediaStore.Images.Media.DATE_TAKEN, [some time]);
				//image.put(MediaStore.Images.Media.DATE_MODIFIED, [some time]);

				// do not rotate image, just put rotation info in
				switch (rotation) {
					//case ORIENTATION_PORTRAIT_NORMAL:image.put(MediaStore.Images.Media.ORIENTATION, 90);break;
					//case ORIENTATION_LANDSCAPE_NORMAL:image.put(MediaStore.Images.Media.ORIENTATION, 0);break;
					//case ORIENTATION_PORTRAIT_INVERTED:image.put(MediaStore.Images.Media.ORIENTATION, 270);break;
					//case ORIENTATION_LANDSCAPE_INVERTED:image.put(MediaStore.Images.Media.ORIENTATION, 180);break;
				}

				// store the picture
				Uri uri = context.getContentResolver().insert(
						MediaStore.Images.Media.EXTERNAL_CONTENT_URI, image);

				try {
					Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0,
							data.length);
					OutputStream out=context.getContentResolver().openOutputStream(
							uri);
					boolean success = bitmap.compress(
							Bitmap.CompressFormat.JPEG, 75, out);
					out.close();
					if (!success) {
						((Activity)context).finish(); // image output failed without any error,
						// silently finish
					}

				} catch (Exception e) {
					e.printStackTrace();
					// handle exceptions
				}

				//mResultIntent = new Intent();
				//mResultIntent.setData(uri);
			} catch (Exception e) {
				e.printStackTrace();
			}

			((Activity)context).finish();
		}
	};
}

/*public void GalleryPhotoPicker(){
		Intent pickPhoto = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		//Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);//Takes a photo with other camera software
		((Activity)context).startActivityForResult(pickPhoto , 0);//one can be replaced with any action code

	}

	protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
		super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
		switch(requestCode) {
			case 0:
				if(resultCode == RESULT_OK){
					Uri selectedImage = imageReturnedIntent.getData();
					//imageview.setImageURI(selectedImage);
				}

				break;
		}
	}
	*/