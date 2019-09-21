package com.example.imageuploader.Utils;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class InterfaceUtil {

    private static Toast toast = null;
    private static boolean INVISIBLE_TOAST = false;

    //showing the toast view only if there is no other toast still running
    public static void showSynchronizedToast(Context c , String Message){
        if(toast != null)
            INVISIBLE_TOAST = !toast.getView().isShown();
        if( INVISIBLE_TOAST || toast == null){
            toast = Toast.makeText(c,Message,Toast.LENGTH_SHORT);
            toast.show();
        }
    }

}
