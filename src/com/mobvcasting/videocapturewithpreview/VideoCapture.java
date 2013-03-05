package com.mobvcasting.videocapturewithpreview;

import java.io.File;
import java.io.IOException;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;

public class VideoCapture extends Activity implements OnClickListener, SurfaceHolder.Callback {

	public static final String LOGTAG = "VIDEOCAPTURE";

	private MediaRecorder recorder;
	private SurfaceHolder holder;
	private CamcorderProfile camcorderProfile;
	private Camera camera;	
	
	boolean recording = false;
	boolean usecamera = true;
	boolean previewRunning = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
				
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		camcorderProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_LOW);

		setContentView(R.layout.main);

		SurfaceView cameraView = (SurfaceView) findViewById(R.id.CameraView);
		holder = cameraView.getHolder();
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		cameraView.setClickable(true);
		cameraView.setOnClickListener(this);
	}

	private void prepareRecorder() {
        recorder = new MediaRecorder();
		recorder.setPreviewDisplay(holder.getSurface());
		
		if (usecamera) {
			camera.unlock();
			recorder.setCamera(camera);
		}
		
		recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
		recorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);

		recorder.setProfile(camcorderProfile);

		// This is all very sloppy
		if (camcorderProfile.fileFormat == MediaRecorder.OutputFormat.THREE_GPP) {
        	try {
				File newFile = File.createTempFile("videocapture", ".3gp", Environment.getExternalStorageDirectory());
				recorder.setOutputFile(newFile.getAbsolutePath());
			} catch (IOException e) {
				Log.v(LOGTAG,"Couldn't create file");
				e.printStackTrace();
				finish();
			}
		} else if (camcorderProfile.fileFormat == MediaRecorder.OutputFormat.MPEG_4) {
        	try {
				File newFile = File.createTempFile("videocapture", ".mp4", Environment.getExternalStorageDirectory());
				recorder.setOutputFile(newFile.getAbsolutePath());
			} catch (IOException e) {
				Log.v(LOGTAG,"Couldn't create file");
				e.printStackTrace();
				finish();
			}
		} else {
        	try {
				File newFile = File.createTempFile("videocapture", ".mp4", Environment.getExternalStorageDirectory());
				recorder.setOutputFile(newFile.getAbsolutePath());
			} catch (IOException e) {
				Log.v(LOGTAG,"Couldn't create file");
				e.printStackTrace();
				finish();
			}

		}
		//recorder.setMaxDuration(50000); // 50 seconds
		//recorder.setMaxFileSize(5000000); // Approximately 5 megabytes
		
		try {
			recorder.prepare();
		} catch (IllegalStateException e) {
			e.printStackTrace();
			finish();
		} catch (IOException e) {
			e.printStackTrace();
			finish();
		}
	}

	public void onClick(View v) {
		if (recording) {
			recorder.stop();
			if (usecamera) {
				try {
					camera.reconnect();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}			
			// recorder.release();
			recording = false;
			Log.v(LOGTAG, "Recording Stopped");
			// Let's prepareRecorder so we can record again
			prepareRecorder();
		} else {
			recording = true;
			recorder.start();
			Log.v(LOGTAG, "Recording Started");
		}
	}

	public void surfaceCreated(SurfaceHolder holder) {
		Log.v(LOGTAG, "surfaceCreated");
		
		if (usecamera) {
			camera = Camera.open();
			
			try {
				camera.setPreviewDisplay(holder);
				camera.startPreview();
				previewRunning = true;
			}
			catch (IOException e) {
				Log.e(LOGTAG,e.getMessage());
				e.printStackTrace();
			}	
		}		
		
	}


	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		Log.v(LOGTAG, "surfaceChanged");

		if (!recording && usecamera) {
			if (previewRunning){
				camera.stopPreview();
			}

			try {
				Camera.Parameters p = camera.getParameters();

				 p.setPreviewSize(camcorderProfile.videoFrameWidth, camcorderProfile.videoFrameHeight);
			     p.setPreviewFrameRate(camcorderProfile.videoFrameRate);
				
				camera.setParameters(p);
				
				camera.setPreviewDisplay(holder);
				camera.startPreview();
				previewRunning = true;
			}
			catch (IOException e) {
				Log.e(LOGTAG,e.getMessage());
				e.printStackTrace();
			}	
			
			prepareRecorder();	
		}
	}

	
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.v(LOGTAG, "surfaceDestroyed");
		if (recording) {
			recorder.stop();
			recording = false;
		}
		recorder.release();
		if (usecamera) {
			previewRunning = false;
			//camera.lock();
			camera.release();
		}
		finish();
	}
}
