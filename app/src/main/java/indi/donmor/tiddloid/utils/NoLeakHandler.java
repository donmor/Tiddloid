package indi.donmor.tiddloid.utils;

import android.os.Handler;
import android.os.Message;

import indi.donmor.tiddloid.MainActivity;

public class NoLeakHandler extends Handler {
    public NoLeakHandler(MainActivity activity, MessageHandledListener listener) {
        this.listener = listener;
    }

    private MessageHandledListener listener;

    public interface MessageHandledListener {
        void onMessageHandled(Message msg);
    }

    @Override
    public void handleMessage(Message msg) {
        listener.onMessageHandled(msg);
    }
}