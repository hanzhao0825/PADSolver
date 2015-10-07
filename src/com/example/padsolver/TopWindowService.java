package com.example.padsolver;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

public class TopWindowService extends Service
{
	public static final String OPERATION = "operation";
	public static final int OPERATION_SHOW = 100;
	public static final int OPERATION_HIDE = 101;

	private static final int HANDLE_CHECK_ACTIVITY = 200;

	private boolean isAdded = false; // �Ƿ�������������
	private static WindowManager wm;
	private static WindowManager.LayoutParams params;
	private ImageView floatView;
	public Bitmap bitmap;
	private List<String> homeList; // ����Ӧ�ó�������б�
	private ActivityManager mActivityManager;

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		Log.v("debug", "On Create");
		homeList = getHomes();
		bitmap = null;
		createFloatView(bitmap);
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}

	@Override
	public void onStart(Intent intent, int startId)
	{
		super.onStart(intent, startId);
		bitmap = BitmapFactory.decodeFile("/sdcard/tmp.png");
		floatView.setImageBitmap(bitmap);
		int operation = intent.getIntExtra(OPERATION, OPERATION_SHOW);
		switch (operation)
		{
		case OPERATION_SHOW:
        	Log.v("debug", "SHOWING");
			mHandler.removeMessages(HANDLE_CHECK_ACTIVITY);
			mHandler.sendEmptyMessage(HANDLE_CHECK_ACTIVITY);
			floatView.setVisibility(View.VISIBLE);
			break;
		case OPERATION_HIDE:
        	Log.v("debug", "HIDING");
			mHandler.removeMessages(HANDLE_CHECK_ACTIVITY);
			floatView.setVisibility(View.INVISIBLE);
			break;
		}
	}

	private Handler mHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
			case HANDLE_CHECK_ACTIVITY:
				if (isHome())
				{
					if (!isAdded)
					{
						wm.addView(floatView, params);
						isAdded = true;
					}
				} else
				{
					if (isAdded)
					{
						wm.removeView(floatView);
						isAdded = false;
					}
				}
				mHandler.sendEmptyMessageDelayed(HANDLE_CHECK_ACTIVITY, 1000);
				break;
			}
		}
	};

	/**
	 * ����������
	 */
	@SuppressLint("RtlHardcoded")
	private void createFloatView(Bitmap bitmap)
	{
		floatView = new ImageView(getApplicationContext());
		floatView.setImageBitmap(bitmap);


		wm = (WindowManager) getApplicationContext().getSystemService(
				Context.WINDOW_SERVICE);
		params = new WindowManager.LayoutParams();
		params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
		
		params.format = PixelFormat.RGBA_8888;
		params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
				| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE 
				| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE 
				| WindowManager.LayoutParams.FLAG_FULLSCREEN;
		params.gravity = Gravity.LEFT | Gravity.TOP; 
		params.width = Global.Instance.width * Global.Instance.cols;
		params.height = Global.Instance.height * Global.Instance.rows;
		params.x = Global.Instance.left;
		params.y = Global.Instance.top;
		

		wm.addView(floatView, params);
		isAdded = true;
	}

	/**
	 * ������������Ӧ�õ�Ӧ�ð�����
	 * 
	 * @return ���ذ������а������ַ����б�
	 */
	private List<String> getHomes()
	{
		List<String> names = new ArrayList<String>();
		PackageManager packageManager = this.getPackageManager();
		// ����
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(
				intent, PackageManager.MATCH_DEFAULT_ONLY);
		for (ResolveInfo ri : resolveInfo)
		{
			names.add(ri.activityInfo.packageName);
		}
		return names;
	}

	/**
	 * �жϵ�ǰ�����Ƿ�������
	 */
	public boolean isHome()
	{
		if (mActivityManager == null)
		{
			mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		}
		List<RunningTaskInfo> rti = mActivityManager.getRunningTasks(1);
		return true;
		// return homeList.contains(rti.get(0).topActivity.getPackageName());
	}

}
