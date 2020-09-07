package net.sunniwell.app.press_key.service;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.widget.Toast;
import net.sunniwell.common.SWSystemProperties;
import net.sunniwell.common.log.SWLogger;

public class SWPressKeyService extends Service {
	private final SWLogger LOG = SWLogger.getLogger(getClass());

	private final static String KEY_PRESS = "net.sunniwell.action.KEY_PRESS_DOWN";

	private JSONArray mStartKeyJsonArray;
	private JSONArray mStopKeyJsonArray;
	private int mStartKeyIndex = 0;
	private int mStopKeyIndex = 0;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public void onCreate() {
		super.onCreate();
		initData();
		registerBroadCast();
	}

	private void initData() {
		try {
			Properties properties = new Properties();
			File contentFile = new File("/system/etc/swassemble_key.prop");
			if (contentFile.exists()) {
				properties.load(new FileInputStream(contentFile));
			} else {
				properties.load(getAssets().open("swassemble_key.prop"));
			}
			String start_key = properties.getProperty("START_KEY");
			if (start_key != null) {
				mStartKeyJsonArray = new JSONArray(start_key);
			}
			String stop_key = properties.getProperty("STOP_KEY");
			if (stop_key != null) {
				mStopKeyJsonArray = new JSONArray(stop_key);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onDestroy() {
		unregisterBroadCast();
		super.onDestroy();
	}

	private void registerBroadCast() {
		IntentFilter mFilter = new IntentFilter();
		mFilter.addAction(KEY_PRESS);
		registerReceiver(mReceiver, mFilter);
	}

	private void unregisterBroadCast() {
		unregisterReceiver(mReceiver);
	}

	private boolean getStartKeyCommand(JSONArray jsonArray, int keyCode) {
		boolean isComplete = false;

		if (jsonArray.length() < 3) {
			return false;
		}

		try {
			LOG.d("=====mStartKeyIndex:" + mStartKeyIndex + " =====:" + jsonArray.getInt(mStartKeyIndex));
			if (jsonArray.getInt(mStartKeyIndex) == keyCode) {
				mStartKeyIndex++;
			} else {
				mStartKeyIndex = 0;
			}
		} catch (JSONException e) {
			e.printStackTrace();
			isComplete = false;
			mStartKeyIndex = 0;
		}

		if (mStartKeyIndex == jsonArray.length()) {
			mStartKeyIndex = 0;
			isComplete = true;
		}

		return isComplete;
	}
	
	private boolean getStopKeyCommand(JSONArray jsonArray, int keyCode) {
		boolean isComplete = false;

		if (jsonArray.length() < 3) {
			return false;
		}

		try {
			LOG.d("===== mStopKeyIndex:" + mStopKeyIndex + " =====:" + jsonArray.getInt(mStopKeyIndex));
			if (jsonArray.getInt(mStopKeyIndex) == keyCode) {
				mStopKeyIndex++;
			} else {
				mStopKeyIndex = 0;
			}
		} catch (JSONException e) {
			e.printStackTrace();
			isComplete = false;
			mStopKeyIndex = 0;
		}

		if (mStopKeyIndex == jsonArray.length()) {
			mStopKeyIndex = 0;
			isComplete = true;
		}

		return isComplete;
	}
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				String action = intent.getAction();
				LOG.d("========receive action:" + action);
				if (KEY_PRESS.equals(action)) {
					int keyCode = intent.getIntExtra("keyCode", -1);
					if (getStartKeyCommand(mStartKeyJsonArray, keyCode)) {
						SWSystemProperties.set("persist.sys.start.stbagent", "true");
						Toast.makeText(context, "stbagent服务已启动", Toast.LENGTH_LONG).show();
					}
					
					if(getStopKeyCommand(mStopKeyJsonArray, keyCode)) {
						SWSystemProperties.set("persist.sys.start.stbagent", "false");
						Toast.makeText(context, "stbagent服务已关闭", Toast.LENGTH_LONG).show();
					}
				} 
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};
}
