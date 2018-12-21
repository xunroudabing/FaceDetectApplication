package com.hanzheng.facedetectapplication.log;

import android.util.Log;

import com.hanzheng.facedetectapplication.config.Constant;


public final class HmctLog {
	private static final String TAG = "HmctLog";

	private HmctLog() {
	}

	public static int d(String tag, String msg) {
		if(Constant.DEBUG){
			AppLogger.d("[" + tag + "]---" + msg);
		}
		return Log.d(TAG, "[" + tag + "]---" + msg);
	}

	public static int d(String tag, Exception e) {
		if(Constant.DEBUG){
			AppLogger.d("[" + tag + "]---" + e.toString());
		}
		return Log.d(TAG, "[" + tag + "]---" + e);
	}

	public static int e(String tag, String msg) {
		if(Constant.DEBUG){
			AppLogger.d("[" + tag + "]---" + msg);
		}
		return Log.e(TAG, "[" + tag + "]---" + msg);
	}

	public static int e(String tag, Exception e) {
		return Log.e(TAG, "[" + tag + "]---" + e);
	}

	public static int i(String tag, String msg) {
		if(Constant.DEBUG){
			AppLogger.d("[" + tag + "]---" + msg);
		}
		return Log.i(TAG, "[" + tag + "]---" + msg);
	}

	public static int v(String tag, String msg) {
		return Log.v(TAG, "[" + tag + "]---" + msg);
	}
}
