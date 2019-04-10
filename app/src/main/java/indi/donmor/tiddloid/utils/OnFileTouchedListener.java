package indi.donmor.tiddloid.utils;

import android.content.DialogInterface;

import java.io.File;

public interface OnFileTouchedListener {

    public void onFileTouched(DialogInterface dialog, File file);

}