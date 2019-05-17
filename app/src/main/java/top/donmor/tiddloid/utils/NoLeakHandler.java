/*
 * top.donmor.tiddloid.utils.NoLeakHandler <= [P|Tiddloid]
 * Last modified: 23:14:01 2019/04/22
 * Copyright (c) 2019 donmor
 */

package top.donmor.tiddloid.utils;

import android.os.Handler;
import android.os.Message;

public class NoLeakHandler extends Handler {
	public NoLeakHandler(MessageHandledListener listener) {
		this.listener = listener;
	}

	private final MessageHandledListener listener;

	public interface MessageHandledListener {
		void onMessageHandled(Message msg);
	}

	@Override
	public void handleMessage(Message msg) {
		super.handleMessage(msg);
		listener.onMessageHandled(msg);
	}
}