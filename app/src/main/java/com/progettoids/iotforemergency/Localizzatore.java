package com.progettoids.iotforemergency;

import android.util.Log;
import android.widget.ImageView;

public class Localizzatore {
    private static int x, y, quota;

    Localizzatore() {
        x=0;
        y=0;
        quota=155;
    }

    public static int getX() {
        return x;
    }

    public static int getY() {
        return y;
    }

    public static int getQuota() {
        return quota;
    }

 /*   public void spostamento() {
            x=x+5;
            y=y+10;
//          mapHome.inizializza(image,x,y,quota);
        Log.i("Localizzatore","Spostamento");

    }*/

}
