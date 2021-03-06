package ba.ima.hepek;



import java.io.IOException;

import ba.ima.hepek.R;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;

public class MainActivity extends Activity implements SurfaceHolder.Callback{
	
	private Button hepekButton; 
	FlashThread flashThread=null;
	MediaPlayer mp;
	Camera mCamera;
	public static SurfaceView preview;
	public static SurfaceHolder mHolder;
	int dot = 200;      // Length of a Morse Code "dot" in milliseconds
	int dash = 500;     // Length of a Morse Code "dash" in milliseconds
	int short_gap = 200;    // Length of Gap Between dots/dashes
	int medium_gap = 500;   // Length of Gap Between Letters
	int long_gap = 1000;    // Length of Gap Between Words
	long[] pattern = {
	    0,  // Start immediately
	    dot, short_gap, dot, short_gap, dot,    // s
	    medium_gap,
	    dash, short_gap, dash, short_gap, dash, // o
	    medium_gap,
	    dot, short_gap, dot, short_gap, dot,    // s
	    long_gap
	};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        preview = (SurfaceView) findViewById(R.id.camSurface);
        mHolder = preview.getHolder();
        mHolder.addCallback(this);
        mCamera = Camera.open();
        try {
            mCamera.setPreviewDisplay(mHolder);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        
        this.hepekButton = (Button)this.findViewById(R.id.hepekBtn);
        this.hepekButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				
				playSound(R.raw.penzioner);
				Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
				v.vibrate(pattern, -1);
				
				boolean hasFlash = getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
                System.out.println("imal flash"+hasFlash);
				if (hasFlash){
					if (flashThread==null){
						flashThread = new FlashThread(pattern);
						flashThread.start();
					}
					if (!flashThread.isAlive()){
						flashThread = new FlashThread(pattern);
						flashThread.start();
					}
                }
				
			}
        	
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    /**
     * Manager of Sounds
     */
    protected void playSound(int uri) {

        
    	if (mp!=null){
    		if (!mp.isPlaying()){
    			mp = MediaPlayer.create(this, uri);
    		}
    	}else{
    		mp = MediaPlayer.create(this, uri);
    	}
        if (!mp.isPlaying()){
        	mp.start();
        }
    }

    
    
    public class FlashThread extends Thread {
    	
    	long[] pattern;
    	public FlashThread(long[] pattern){
    		this.pattern=pattern;
    	}
        @Override
        public void run() {
        	
        	Parameters params=null;
            // Simulate a slow network
            try {
            	
            	params = mCamera.getParameters();
                params.setFlashMode(Parameters.FLASH_MODE_TORCH);
                mCamera.setParameters(params);      
                mCamera.startPreview();   
            	
            	System.out.println("pocinjem flash pattern");
            	
            	boolean on=true;
            	for (long l:pattern){
            		if (on){
            			System.out.println("on"+l);
            			params.setFlashMode(Parameters.FLASH_MODE_TORCH);
                        mCamera.setParameters(params);
            			
            			on=false;
            		}else{
            			System.out.println("off"+l);
            			params.setFlashMode(Parameters.FLASH_MODE_OFF);
                        mCamera.setParameters(params);
            			on=true;
            		}
            		sleep(l);
            	}
              
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
             finally {
            	 System.out.println("finaly");
            	try{
            		params.setFlashMode(Parameters.FLASH_MODE_OFF);
                    mCamera.setParameters(params);
            	}
            	catch(Exception e){
            		System.out.println("camstop failed");
            	}

             }
        
      }
    }



	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		mHolder = holder;
        try {
            mCamera.setPreviewDisplay(mHolder);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		// TODO Auto-generated method stub
		 mCamera.stopPreview();
         mHolder = null;
	}
}
 