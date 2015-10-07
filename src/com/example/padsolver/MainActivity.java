package com.example.padsolver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;


public class MainActivity extends Activity implements OnClickListener, TextWatcher{
	View btnCalc, btnHide, btnShow, btnLoad, btnLoadNewest;
	TextView tvResult;
	EditText etCols, etRows, etMaxLen, etGroupSize, etIteration, etMode;
	ImageView iv;
	ImageIO imageIO;
	String filename = "";
	private final String IMAGE_TYPE = "image/*";
	private final int IMAGE_CODE = 0;  
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);  
		btnCalc = this.findViewById(R.id.buttonCalc);  
		btnCalc.setOnClickListener(this);  
		btnHide = this.findViewById(R.id.buttonHide);  
		btnHide.setOnClickListener(this);  
		btnShow = this.findViewById(R.id.buttonShow);  
		btnShow.setOnClickListener(this);  
		btnLoad = this.findViewById(R.id.buttonLoad);  
		btnLoad.setOnClickListener(this);  
		btnLoadNewest = this.findViewById(R.id.buttonLoadNewest);
		btnLoadNewest.setOnClickListener(this);
		etCols = (EditText) this.findViewById(R.id.editTextCols);
		etCols.addTextChangedListener(this);
		etRows = (EditText) this.findViewById(R.id.editTextRows);
		etRows.addTextChangedListener(this);
		etMaxLen = (EditText) this.findViewById(R.id.editTextMaxLen);
		etMaxLen.addTextChangedListener(this);
		etGroupSize = (EditText) this.findViewById(R.id.editTextGroupSize);
		etGroupSize.addTextChangedListener(this);
		etIteration = (EditText) this.findViewById(R.id.editTextIteration);
		etIteration.addTextChangedListener(this);
		etMode = (EditText) this.findViewById(R.id.editTextMode);
		etMode.addTextChangedListener(this);
		tvResult = (TextView) this.findViewById(R.id.textViewResult);
		iv = (ImageView) this.findViewById(R.id.imageView1);
		
		Global.Instance = new Global(6, 5, 25);
		Global.Instance.mode = 1;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@SuppressLint("SdCardPath")
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

        if (v.getId() == R.id.buttonCalc) {  
            Log.v("debug", "Click!");

    		Global.Instance.getWidthAndHeight();
    		Global.Instance.footprint = new int[Global.Instance.cols][Global.Instance.rows];
    		Global.Instance.footprintOwner = new int[Global.Instance.cols][Global.Instance.rows];
    		
    		Board.startBoard = new Board();
    		
    		
    		imageIO = new ImageIO();
    		Board.startBoard.setOrbs(imageIO.Parse(filename));
    		Board.oprBoard = new Board();
    		Log.v("debug", Board.startBoard.print());
    		
    		GAOptimizer gao = new GAOptimizer();
    		gao.init(Global.Instance.groupSize);
    		gao.iterate(Global.Instance.iteration);
    		Global.state = gao.group.get(0);
    		Global.state.value = -1;
    		Global.state.getValue();
    		tvResult.setText("Result : " + Global.Instance.strResult);
    		imageIO.SaveToBitmap(Global.state);
    		
    		
			Intent show = new Intent(this, TopWindowService.class);
			show.putExtra(TopWindowService.OPERATION,
					TopWindowService.OPERATION_SHOW);
			startService(show);
        }  
        if (v.getId() == R.id.buttonHide) {  
			Intent show = new Intent(this, TopWindowService.class);
			show.putExtra(TopWindowService.OPERATION,
					TopWindowService.OPERATION_HIDE);
			startService(show);
        } 
        if (v.getId() == R.id.buttonShow) {  
			Intent show = new Intent(this, TopWindowService.class);
			show.putExtra(TopWindowService.OPERATION,
					TopWindowService.OPERATION_SHOW);
			startService(show);
        }
        if (v.getId() == R.id.buttonLoad) {
        	Intent getAlbum = new Intent(Intent.ACTION_GET_CONTENT);
        	getAlbum.setType(IMAGE_TYPE);
        	startActivityForResult(getAlbum, IMAGE_CODE);
        }
        if (v.getId() == R.id.buttonLoadNewest) {
        	ArrayList<File> images = new ArrayList<File>();  
            getFiles(images, "/sdcard/Pictures/screenshots");  
            if (!images.isEmpty()) {
                File latestSavedImage = images.get(0);  
                if (latestSavedImage.exists()) {  
                    for (int i = 1; i < images.size(); i++) {  
                        File nextFile = images.get(i);  
                        if (nextFile.lastModified() > latestSavedImage.lastModified()) {  
                            latestSavedImage = nextFile;  
                        }  
                    }  
                }
                filename = latestSavedImage.getAbsolutePath();
                Bitmap bm = new ImageIO().getDiskBitmap(filename);
	            iv.setImageBitmap(bm);
	            Global.Instance.imgWidth = bm.getWidth();
	            Global.Instance.imgHeight = bm.getHeight();
	            Global.Instance.bitmap = bm;
            }  
        }
	}  
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
	    if (resultCode != RESULT_OK) {        
	        return;
	    }
	    Bitmap bm = null;
	    ContentResolver resolver = getContentResolver();
	    if (requestCode == IMAGE_CODE) {
	        try {
	            Uri originalUri = data.getData();        //获得图片的uri 
	            bm = MediaStore.Images.Media.getBitmap(resolver, originalUri);        //显得到bitmap图片
	            String[] proj = {MediaStore.Images.Media.DATA};
	            @SuppressWarnings("deprecation")
				Cursor cursor = managedQuery(originalUri, proj, null, null, null); 
	            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
	            cursor.moveToFirst();
	            iv.setImageBitmap(bm);
	            Global.Instance.imgWidth = bm.getWidth();
	            Global.Instance.imgHeight = bm.getHeight();
	            Global.Instance.bitmap = bm;
	            filename = cursor.getString(column_index);
	        }catch (IOException e) { 
	        }
	    }
	}

	@Override
	public void afterTextChanged(Editable arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
			int arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		Global.Instance.cols = !TextUtils.isEmpty(etCols.getText().toString()) ? Integer.parseInt(etCols.getText().toString()) : 0;
		Global.Instance.rows = !TextUtils.isEmpty(etRows.getText().toString()) ? Integer.parseInt(etRows.getText().toString()) : 0;
		Global.Instance.maxLength = !TextUtils.isEmpty(etMaxLen.getText().toString()) ? Integer.parseInt(etMaxLen.getText().toString()) : 0;
		Global.Instance.groupSize = !TextUtils.isEmpty(etGroupSize.getText().toString()) ? Integer.parseInt(etGroupSize.getText().toString()) : 0;
		Global.Instance.iteration = !TextUtils.isEmpty(etIteration.getText().toString()) ? Integer.parseInt(etIteration.getText().toString()) : 0;
		Global.Instance.mode = !TextUtils.isEmpty(etMode.getText().toString()) ? Integer.parseInt(etMode.getText().toString()) : 0;
	}
	
	private void getFiles(ArrayList<File> fileList, String path) {  
	    File[] allFiles = new File(path).listFiles();  
	    for (int i = 0; i < allFiles.length; i++) {  
	        File file = allFiles[i];  
	        if (file.isFile() && file.getAbsolutePath().contains(".png")) {  
	            fileList.add(file);  
	        }  
	    }  
	}  
	
}
