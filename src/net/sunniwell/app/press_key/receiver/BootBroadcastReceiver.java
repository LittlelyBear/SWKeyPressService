package net.sunniwell.app.press_key.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import net.sunniwell.app.press_key.service.SWPressKeyService;

/**
 * 监听开关机状态的广播
 * 
 * @author linyanting
 * 
 */
public class BootBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(final Context context, Intent intent) {
		String action = intent.getAction();
		if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
			context.startService(new Intent(context, SWPressKeyService.class));
		}
	}
}
