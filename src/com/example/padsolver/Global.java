package com.example.padsolver;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.util.Log;

import java.lang.Math;
import java.util.HashMap;


@SuppressLint("UseSparseArrays")
public class Global {
	public final int dx[];
	public final int dy[];
	public final char dirName[];
	public final char orbName[][];
	public int footprint[][], footprintOwner[][];
	public int cols;
	public int rows;
	public int maxLength;
	public final HashMap<Integer, Integer> hashCnt;
	public int left, top, right, bottom, width, height;
	public static Global Instance;
	public static State state;
	public int imgWidth, imgHeight;
	public Bitmap bitmap;
	public String strResult;
	public int groupSize = 1000, iteration = 50;
	public int mode = 0;
	
	public Global(int cols, int rows, int maxLength) {
		dx = new int[]{1, 0, -1, 0};
		dy = new int[]{0, 1, 0, -1};
		dirName = new char[]{'R', 'D', 'L', 'U'};
		orbName = new char[][]{{'\n','R','B','G','L','D','H','P','X'}, {'\n','R','B','G','Y','P','H','J','X'}};
		this.cols = cols;
		this.rows = rows;
		this.maxLength = maxLength;
		hashCnt = new HashMap<Integer, Integer>();
	}
	
	public boolean isBlack(int pixel) {
		int c[] = new int[]{0,0,0};
		c[0] = (pixel & 0xff);
		c[1] = (pixel & 0xff00) >> 8;
		c[2] = (pixel & 0xff0000) >> 16;
		return c[0]<=20 && c[1]<=20 && c[2]<=20;
		
	}

	public void getWidthAndHeight() {
		if (cols == 0 || rows == 0) return;
		left = -1; right = imgWidth; bottom = imgHeight;
		boolean flag;
		flag = false;
		do {
			left ++;
			int pixel = bitmap.getPixel(left, imgHeight*3/4);
			flag = isBlack(pixel);
		} while(flag);
		flag = false;
		do {
			right --;
			int pixel = bitmap.getPixel(right, imgHeight*3/4);
			flag = isBlack(pixel);
		} while(flag);
		flag = false;
		do {
			bottom --;
			int pixel = bitmap.getPixel(imgWidth/2, bottom);
			flag = isBlack(pixel);
		} while(flag);
		flag = false; top = bottom - 1;
		do {
			top --;
			boolean flag2 = true;
			for (int i = 0; i < imgWidth; i ++) {
				int pixel = bitmap.getPixel(i, top);
				flag2 = flag2 && isBlack(pixel);
			}
			flag = !flag2;
		} while(flag);
		
		Log.v("debug", "Left, Right, Top, Bottom = "+left+" "+right+" "+top+" "+bottom);
		left ++;
		top ++;

		width = (right - left) / cols;
		height = (bottom - top) / rows;
	}
	
	boolean isValidPosition(int tx, int ty) {
	    return (tx >= 0 && ty >= 0 && tx < cols && ty < rows);
	}
	
	boolean isValidDirs(int startX, int startY, Deque dirs) {
	    for (int i = 0; i < dirs.size(); i ++) {
	        startX += dx[dirs.get(i)];
	        startY += dy[dirs.get(i)];
	        if (!isValidPosition(startX, startY)) return false;
	        if (i > 0 && Math.abs(dirs.get(i-1)-dirs.get(i)) == 2) return false;
	    }
	    return true;
	}
	
	boolean isValidDirPair(int a, int b) {
	    return Math.abs(a-b) != 2;
	}
	
	int getHash(int x, int y, Deque dirs) {
	    int ans = x + y * cols;
	    for (int i = 0; i < dirs.size(); i ++) {
	        ans = ans * 4 + dirs.get(i);
	    }
	    return ans;
	}
	

}
