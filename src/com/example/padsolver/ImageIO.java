package com.example.padsolver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

public class ImageIO {
	int color[][][] = new int[][][]{{{0,0,0}, {76,96,231}, {223,162,76}, {109,174,74}, {84,207,222}, {176,82,164}, {167,77,230}, {0, 0, 0}, {99,79,55}},
									{{0,0,0}, {0,0,0}, {0,0,0}, {0,0,0}, {0,0,0}, {0,0,0}, {0,0,0}, {160,159,174}, {90,89,73}}};
	Global global = Global.Instance;
	
	public int[] Parse(String filename) {

	    
	    int orbs[] = new int[global.cols*global.rows];
	    Bitmap bitmap = getDiskBitmap(filename);
	    
	    for (int j = 0; j < global.rows; j ++) {
	        for (int i = 0; i < global.cols; i ++) {
	            int sumColor[] = new int[]{0,0,0};
	            for (int x = (int) (global.left + global.width * i + global.width * 0.25); x < global.left + global.width * i + global.width * 0.75; x ++) {
	                for (int y = (int) (global.top + global.height * j + global.height * 0.25); y < global.top + global.height * j + global.height * 0.75; y ++) {
	                    //printf("%d %d\n", x, y);
	                	int pixel = bitmap.getPixel(x, y);
	                	sumColor[0] += (pixel & 0xff);
	                	sumColor[1] += (pixel & 0xff00) >> 8;
	        			sumColor[2] += (pixel & 0xff0000) >> 16;
	                }
	            }
	            for (int k = 0; k < 3; k ++) {
	                sumColor[k] /= (global.width * global.height / 4);
	            }
	            //printf("%d %d %d\n", sumColor[0], sumColor[1], sumColor[2]);
	            int bv = 2000000000, bi = 0, bpin = 0;
	            for (int pin = 0; pin < 2; pin ++){
		            for (int t = 0; t < 9; t ++) {
		                for (int offset = -255; offset <= 0; offset ++) {
		                    int tmpColor[] = new int[3];
		                    int dis = 0;
		                    for (int k = 0; k < 3; k ++) {
		                        tmpColor[k] = sumColor[k] + offset;
		                        dis += (tmpColor[k] - color[pin][t][k]) * (tmpColor[k] - color[pin][t][k]);
		                    }
		                    if (dis < bv) {
		                        bv = dis;
		                        bi = t;
		                        bpin = pin;
		                    }
		                }
		            }
	            }
	            if (color[bpin][bi][0] == 0) {
	                Log.v("debug", ""+i+" "+j+" "+sumColor[0]+" "+sumColor[1]+" "+sumColor[2]);
	            }
	            orbs[i+j*global.cols] = bi;
	        }
	    }
	    return orbs;
	}
	
	public Bitmap getDiskBitmap(String pathString)  
	{  
	    Bitmap bitmap = null;  
	    try {  
	        File file = new File(pathString);  
	        if (file.exists())  {  
	            bitmap = BitmapFactory.decodeFile(pathString);  
	        }  else {
	        	Log.v("debug", "File not exist! " + pathString);
	        }
	    } catch (Exception e)  {  
	    	Log.v("debug", "Cannot read Bitmap from" + pathString);
	        // TODO: handle exception  
	    }
	    return bitmap;  
	} 
	
	public Bitmap SaveToBitmap(State s) {
		Bitmap bitmap = Bitmap.createBitmap(global.width*global.cols, global.height*global.rows, Bitmap.Config.ARGB_8888);
		Log.v("debug", "cols = "+global.cols);
		Canvas canvas = new Canvas(bitmap);
		Paint painter = new Paint(); 

	    painter.setStyle(Paint.Style.STROKE);
	    painter.setStrokeWidth((float) (global.height/20)); 
	    painter.setARGB(255, 50, 50, 50);
	    int cnt[] = new int[global.rows*global.cols];
	    int x, y;
	    
	    for (int i = 0; i < global.rows*global.cols; i ++) {
	    	cnt[i] = 0;
	    }
	    x = s.startX; y = s.startY;
		canvas.drawCircle((float)((x+0.5)*global.width), (float)((y+0.5)*global.height), (float)(global.height/6), painter);
	    int tx, ty;
	    cnt[x+y*global.cols] = 1;
	    int dt = global.height / 10;
	    for (int i = 0; i < s.dirs.size(); i ++) {
	        tx = x + global.dx[s.dirs.get(i)];
	        ty = y + global.dy[s.dirs.get(i)];

		    painter.setStrokeWidth((float) (global.height/15)); 
		    painter.setARGB(255, 50, 50, 50);
	        canvas.drawLine((float)((x+0.5)*global.width + (cnt[x+y*global.cols]-1)*dt), (float)((y+0.5)*global.height + (cnt[x+y*global.cols]-1)*dt),
	        		(float)((tx+0.5)*global.width + cnt[tx+ty*global.cols]*dt), (float)((ty+0.5)*global.height + cnt[tx+ty*global.cols]*dt), painter);

		    painter.setStrokeWidth((float) (global.height/40)); 
		    painter.setARGB(255, 255, 255, 255);
	        canvas.drawLine((float)((x+0.5)*global.width + (cnt[x+y*global.cols]-1)*dt), (float)((y+0.5)*global.height + (cnt[x+y*global.cols]-1)*dt),
	        		(float)((tx+0.5)*global.width + cnt[tx+ty*global.cols]*dt), (float)((ty+0.5)*global.height + cnt[tx+ty*global.cols]*dt), painter);
	        cnt[tx+ty*global.cols] ++;
	        x = tx;
	        y = ty;
	    }
	    saveBitmap(bitmap);
	    return bitmap;
	}
	
	public void saveBitmap(Bitmap mBitmap) {
		File f = new File("/sdcard/tmp.png");
		try {
			f.createNewFile();
		} catch (IOException e) {
			Log.v("debug", "saveBitmap, Error : " + e.toString());
		}
		FileOutputStream fOut = null;
		try {
			fOut = new FileOutputStream(f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
		try {
			fOut.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			fOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
