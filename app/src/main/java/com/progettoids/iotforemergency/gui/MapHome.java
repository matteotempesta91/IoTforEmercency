package com.progettoids.iotforemergency.gui;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.support.v7.widget.AppCompatImageView;
import android.widget.RelativeLayout;

import com.progettoids.iotforemergency.db.DBManager;
import com.progettoids.iotforemergency.gestionedati.DriverServer;
import com.progettoids.iotforemergency.R;

import java.util.Arrays;

public class MapHome extends AppCompatImageView {

    private Matrix matrix = new Matrix();

    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    static final int CLICK = 3;
    private int mode = NONE;

    private PointF last = new PointF();
    private PointF start = new PointF();
    private float minScale = 1f;
    private float maxScale = 4f;
    private float[] m;

    private float redundantXSpace, redundantYSpace;
    private float width, height;
    private float saveScale = 1f;
    private float right, bottom, origWidth, origHeight, bmWidth, bmHeight;
    private ScaleGestureDetector mScaleDetector;
    private Context context;

    private Bitmap bmMap, bmNodi, bmPos, bmInc, bmCrollo, bmAffollato;
    private int mappa;
    private int[] pixelPos;

    public MapHome(final Context context, AttributeSet attr) {
        super(context, attr);
        super.setClickable(true);
        this.context = context;
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        matrix.setTranslate(1f, 1f);
        m = new float[9];
        setImageMatrix(matrix);
        setScaleType(ScaleType.MATRIX);
        setMaxZoom(4f);

        pixelPos = new int[]{0,0,0};
        mappa = -1; // non impedisce il reset in disegnaPosizione se quota=0
        // Si registra per ricevere update dal server
        DriverServer mDriverServer = DriverServer.getInstance(null);
        mDriverServer.mFromServer.mapHomeAlive(this);

        bmPos = BitmapFactory.decodeResource(getResources(), R.drawable.posizione);
        bmInc = BitmapFactory.decodeResource(getResources(), R.drawable.incendio);
        bmCrollo = BitmapFactory.decodeResource(getResources(), R.drawable.crollo);
        bmAffollato = BitmapFactory.decodeResource(getResources(), R.drawable.affollamento);

        // Recupero l'ultima posizione nota se presente
        final SharedPreferences reader = context.getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        int x = reader.getInt("pos_x", 0);
        int y = reader.getInt("pos_y", 0);
        int z = reader.getInt("pos_z", 0);
        // Se 0 0 0 => pos sconosciuta
        disegnaPosizione(x,y,z);

        setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mScaleDetector.onTouchEvent(event);

                matrix.getValues(m);
                float x = m[Matrix.MTRANS_X];
                float y = m[Matrix.MTRANS_Y];
                PointF curr = new PointF(event.getX(), event.getY());

                switch (event.getAction()) {
                    //when one finger is touching
                    //set the mode to DRAG
                    case MotionEvent.ACTION_DOWN:
                        last.set(event.getX(), event.getY());
                        start.set(last);
                        mode = DRAG;
                        break;
                    //when two fingers are touching
                    //set the mode to ZOOM
                    case MotionEvent.ACTION_POINTER_DOWN:
                        last.set(event.getX(), event.getY());
                        start.set(last);
                        mode = ZOOM;
                        break;
                    //when a finger moves
                    //If mode is applicable move image
                    case MotionEvent.ACTION_MOVE:
                        //if the mode is ZOOM or
                        //if the mode is DRAG and already zoomed
                        if (mode == ZOOM || (mode == DRAG && saveScale > minScale)) {
                            float deltaX = curr.x - last.x;// x difference
                            float deltaY = curr.y - last.y;// y difference
                            float scaleWidth = Math.round(origWidth * saveScale);// width after applying current scale
                            float scaleHeight = Math.round(origHeight * saveScale);// height after applying current scale
                            //if scaleWidth is smaller than the views width
                            //in other words if the image width fits in the view
                            //limit left and right movement
                            if (scaleWidth < width) {
                                deltaX = 0;
                                if (y + deltaY > 0)
                                    deltaY = -y;
                                else if (y + deltaY < -bottom)
                                    deltaY = -(y + bottom);
                            }
                            //if scaleHeight is smaller than the views height
                            //in other words if the image height fits in the view
                            //limit up and down movement
                            else if (scaleHeight < height) {
                                deltaY = 0;
                                if (x + deltaX > 0)
                                    deltaX = -x;
                                else if (x + deltaX < -right)
                                    deltaX = -(x + right);
                            }
                            //if the image doesnt fit in the width or height
                            //limit both up and down and left and right
                            else {
                                if (x + deltaX > 0)
                                    deltaX = -x;
                                else if (x + deltaX < -right)
                                    deltaX = -(x + right);

                                if (y + deltaY > 0)
                                    deltaY = -y;
                                else if (y + deltaY < -bottom)
                                    deltaY = -(y + bottom);
                            }
                            //move the image with the matrix
                            matrix.postTranslate(deltaX, deltaY);
                            //set the last touch location to the current
                            last.set(curr.x, curr.y);
                        }
                        break;
                    //first finger is lifted
                    case MotionEvent.ACTION_UP:
                        mode = NONE;
                        int xDiff = (int) Math.abs(curr.x - start.x);
                        int yDiff = (int) Math.abs(curr.y - start.y);
                        if (xDiff < CLICK && yDiff < CLICK)
                            performClick();
                        break;
                    // second finger is lifted
                    case MotionEvent.ACTION_POINTER_UP:
                        mode = NONE;
                        break;
                }
                setImageMatrix(matrix);
                return true;
            }
        });
    }

    // Metodo per svuotare la memoria alla distruzione della home
    public void emptyBM() {
        bmInc.recycle();
        bmCrollo.recycle();
        bmAffollato.recycle();
        bmPos.recycle();
        bmMap.recycle();
        bmNodi.recycle();
        setImageResource(android.R.color.transparent);
        // Ricorda di notificare FromServer della chiusura della mappa
        DriverServer mDriverServer = DriverServer.getInstance(null);
        mDriverServer.mFromServer.mapHomeAlive(null);
    }

// ------------------------------------ CONVERSIONE E SELEZIONE MAPPA -----------------------------------------

// Conversioni dalle coordinate in metri con cui sono memorizzate le posizioni sul database
// alle coordinate in pixel per disegnare sulle mappe bitmap
// (l'origine del sistema di riferimento in pixel è in alto a sinistra,
// l'origine del sistema di riferimento in metri è in basso a sinistra)
// per aggiungere una mappa, specificare id risorsa, scala e offset delle coordinate
    private int[] mappeEconversione(int x, int y, int quota) {
        int[] coord = new int[3];
        float scala, offX, offY;
        switch (quota) {
            case 145:
                /*
                *dim immagine 926x1600
                *145A5 jpg X = 255px; excell X = 91m
                *          Y = 150px; excell Y =484m
                *145S3 jpg X = 600px; excell X = 145m
                *OFFSET Y (150px/6.46)+484m*6.4 = 507.4m
                */
                scala = 6.4f;             // 345px/54m [pixel/metri] è il fattore di scala tra le coordinate in metri del file excell e i pixel dell'immagine bitmap
                offX = (91 - 255 / scala);    // (91metri-255px/scala) = 51.2 metri è la distanza rispetto a X in metri tra l'origine dell'immagine bitmap e l'origine delle coordinate del file excell ()
                offY = (150 / scala) + 484;   // (150px/scala)+484m = 507.4 metri è la distanza rispetto a Y in metri tra l'origine dell'immagine bitmap e l'origine delle coordinate del file excell (507.2 metri)
                coord[2] = R.drawable.map145;
                break;
            case 150:
                /*
                * dim immagine 990x1572
                * G2      X=485px Y=270px
                *          X=129m  Y=465m
                */
                scala = 6.4f;             // 345px/54m [pixel/metri] è il fattore di scala tra le coordinate in metri del file excell e i pixel dell'immagine bitmap
                offX = (129 - 485 / scala);   // (129metri-485px/scala) = -53.2 metri è la distanza rispetto a X in metri tra l'origine dell'immagine bitmap e l'origine delle coordinate del file excell
                offY = (270 / scala) + 465;   // (270/scala)+465 è la distanza rispetto a Y in metri tra l'origine dell'immagine bitmap e l'origine delle coordinate del file excell (507.2 metri)
                coord[2] = R.drawable.map150;
                break;
            case 155:
                /*
                * dimensione immagine 1184 1600
                * 15A5     X=91m  242px
                *          Y=484m 154px
                */
                scala = 6.4f;             // 345px/54m [pixel/metri] è il fattore di scala tra le coordinate in metri del file excell e i pixel dell'immagine bitmap
                offX = (91 - 242 / scala);    // (91m-242px/scala) è la distanza rispetto a X in metri tra l'origine dell'immagine bitmap e l'origine delle coordinate del file excell
                offY = (154 / scala + 484);   // (154px/scala+484m) è la distanza rispetto a Y in metri tra l'origine dell'immagine bitmap e l'origine delle coordinate del file excell (507.2 metri)
                coord[2] = R.drawable.map155;
                break;
            default:
                scala = 1;
                offX = 0;
                offY = 0;
                coord[2] = R.drawable.nomap;
                break;
        }
        float convX = (x - offX) * scala;             // 70.06 sono i metri di offset della x, 6.39 è il fattore di scala per passare da metri a pixel
        float convY = (offY - y) * scala;
        coord[0] = (int) convX;
        coord[1] = (int) convY;
        return coord;
    }

// ------------------------------------------------------------------------------------------------------------

    public void disegnaPosizione(int x, int y, int quota) {
        pixelPos = mappeEconversione(x, y, quota);
        // La mappa è ricaricata solo se necessario
        if (quota != mappa) {
            LoadMap lmap = new LoadMap(quota);
            Object[] params = {getResources(), pixelPos[2]};
            lmap.execute(params);
        }
        //per centrare il bitmap dell'icona
        pixelPos[0] = pixelPos[0] - bmPos.getWidth() / 2;
        pixelPos[1] = pixelPos[1] - bmPos.getHeight();
    }

    // Disegna la cornice_red intorno alla mappa in base allo stato di emergenza che riceve in ingresso
    // lo stato è il tipo di emergenza, layout è il layout della home
    public void disegnaEmergenza(int stato) {
        RelativeLayout layout;
        layout = ((HomeActivity)context).layoutHome;
    //public void disegnaEmergenza(int stato) {
        //Paint linePaint = new Paint();              // linePaint definisce lo stile della cornice_red
        //linePaint.setStrokeWidth(30);               // Spessore della cornice_red
        //linePaint.setColor( Color.GREEN );          // Colore di default della cornice_red = verde
        // Cambia il colore della cornice_red in base allo stato che riceve in ingresso
        //RelativeLayout layout =(RelativeLayout)findViewById(R.id.activity_home);
        switch (stato) {
            case 0:
                layout.setBackgroundResource(R.drawable.cornice_green);         // Colore per nessuna emergenza (verde)
                break;
            case 1:
                layout.setBackgroundResource(R.drawable.cornice_red);           // Colore per incendio
                break;
            case 2:
                layout.setBackgroundResource(R.drawable.cornice_terremoto);        // Colore per terremoto
                break;
        }

        //RelativeLayout layout =(RelativeLayout)findViewById(R.id.activity_home);
        //layout.setBackgroundColor(Color.RED);
        //layout.setBackgroundResource(R.drawable.cornice_green);

        // Disegna le quattro linee della cornice_red
    /*    canvas.drawLine(0,0,0,bmMap.getHeight(),linePaint);
        canvas.drawLine(0,0,bmMap.getWidth(),0,linePaint);
        canvas.drawLine(bmMap.getWidth(),0,bmMap.getWidth(),bmMap.getHeight(),linePaint);
        canvas.drawLine(0,bmMap.getHeight(),bmMap.getWidth(),bmMap.getHeight(),linePaint);
    */
        //this.setImageBitmap(bmMap);
    }

    // Disegna la notifica di pericolo sui nodi corrispondenti
    public void updateStatoNodi() {
        Cursor cr = DBManager.getStatoNodi(mappa);
        // Svuota il layer nodi
        if (bmNodi != null) { bmNodi.recycle(); }
        bmNodi = Bitmap.createBitmap(bmMap.getWidth(), bmMap.getHeight(), bmMap.getConfig());
        Canvas canvas = new Canvas(bmNodi);
        // Riempie il layer nodi
        while (cr.moveToNext()) {
            int stato = cr.getInt(0);
            int x = cr.getInt(1);
            int y = cr.getInt(2);
            int z = cr.getInt(3);
            int[] pixelCoord;
            pixelCoord = mappeEconversione(x, y, z);
            // prende come riferimento il centro dell'icona anzichè l'angolo in alto a sinistra
            pixelCoord[0] = pixelCoord[0] - bmInc.getWidth() / 2;
            pixelCoord[1] = pixelCoord[1] - bmInc.getHeight() / 2;

            // In base al tipo di notifica che riceve disegna l'icona dello stato del nodo (incendio, crollo, o affollamento) sulla mappa
            switch (stato) {
                case 0:
                    // se si vuole prevedere cambi di stato a 0, questo case elimina l'immagine del nodo dal layer
                    int w = bmInc.getWidth();
                    int h = bmInc.getHeight();
                    int[] pix = new int[w*h];
                    Arrays.fill(pix, 0x0000);
                    bmNodi.setPixels(pix, 0, w, pixelCoord[0], pixelCoord[1], w, h);
                    break;
                case 1:                 // Incendio
                    canvas.drawBitmap(bmInc, pixelCoord[0], pixelCoord[1], null);
                    break;
                case 2:                 // Crollo
                    canvas.drawBitmap(bmCrollo, pixelCoord[0], pixelCoord[1], null);
                    break;
                case 3:                 // Affollato
                    canvas.drawBitmap(bmAffollato, pixelCoord[0], pixelCoord[1], null);
                    break;
            }
        }
        setBitmap();
    }

//  -------------------------------------------------------------------------------------
    // Metodi per set, pan e zoom
    public void setBitmap() {
        Bitmap bmOverlay = Bitmap.createBitmap(bmMap.getWidth(), bmMap.getHeight(), bmMap.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(bmMap, 0, 0, null);
        canvas.drawBitmap(bmNodi, 0, 0, null);
        canvas.drawBitmap(bmPos, pixelPos[0], pixelPos[1], null);
        setImageBitmap(bmOverlay);
        bmWidth = bmMap.getWidth();
        bmHeight = bmMap.getHeight();
    }

    public void setMaxZoom(float x) {
        maxScale = x;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            mode = ZOOM;
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float mScaleFactor = detector.getScaleFactor();
            float origScale = saveScale;
            saveScale *= mScaleFactor;
            if (saveScale > maxScale) {
                saveScale = maxScale;
                mScaleFactor = maxScale / origScale;
            } else if (saveScale < minScale) {
                saveScale = minScale;
                mScaleFactor = minScale / origScale;
            }
            right = width * saveScale - width - (2 * redundantXSpace * saveScale);
            bottom = height * saveScale - height - (2 * redundantYSpace * saveScale);
            if (origWidth * saveScale <= width || origHeight * saveScale <= height) {
                matrix.postScale(mScaleFactor, mScaleFactor, width / 2, height / 2);
                if (mScaleFactor < 1) {
                    matrix.getValues(m);
                    float x = m[Matrix.MTRANS_X];
                    float y = m[Matrix.MTRANS_Y];
                    if (mScaleFactor < 1) {
                        if (Math.round(origWidth * saveScale) < width) {
                            if (y < -bottom)
                                matrix.postTranslate(0, -(y + bottom));
                            else if (y > 0)
                                matrix.postTranslate(0, -y);
                        } else {
                            if (x < -right)
                                matrix.postTranslate(-(x + right), 0);
                            else if (x > 0)
                                matrix.postTranslate(-x, 0);
                        }
                    }
                }
            } else {
                matrix.postScale(mScaleFactor, mScaleFactor, detector.getFocusX(), detector.getFocusY());
                matrix.getValues(m);
                float x = m[Matrix.MTRANS_X];
                float y = m[Matrix.MTRANS_Y];
                if (mScaleFactor < 1) {
                    if (x < -right)
                        matrix.postTranslate(-(x + right), 0);
                    else if (x > 0)
                        matrix.postTranslate(-x, 0);
                    if (y < -bottom)
                        matrix.postTranslate(0, -(y + bottom));
                    else if (y > 0)
                        matrix.postTranslate(0, -y);
                }
            }
            return true;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);
        //Fit to screen.
        float scale;
        float scaleX = width / bmWidth;
        float scaleY = height / bmHeight;
        scale = Math.min(scaleX, scaleY);
        matrix.setScale(scale, scale);
        setImageMatrix(matrix);
        saveScale = 1f;

        // Center the image
        redundantYSpace = height - (scale * bmHeight);
        redundantXSpace = width - (scale * bmWidth);
        redundantYSpace /= 2;
        redundantXSpace /= 2;

        matrix.postTranslate(redundantXSpace, redundantYSpace);

        origWidth = width - 2 * redundantXSpace;
        origHeight = height - 2 * redundantYSpace;
        right = width * saveScale - width - (2 * redundantXSpace * saveScale);
        bottom = height * saveScale - height - (2 * redundantYSpace * saveScale);
        setImageMatrix(matrix);
    }

    //Questo metodo serve per controllare se la posizione utente è stata inizializzata
    public boolean posIsZero() {
      boolean isZero=false;
        if (pixelPos[0]==0 && pixelPos[1]==0){
            isZero = true;
        }
        return  isZero;
    }
//  ---------------------------------------------------------------

    // Inner asyncTask per caricare la Mappa senza bloccare l'app
    private class LoadMap extends AsyncTask<Object, Void, Bitmap> {
        int quota;

        private LoadMap(int quota) {
            super();
            this.quota = quota;
        }

        @Override
        protected Bitmap doInBackground(Object... params) {

            Resources res = (Resources) params[0];
            int mapResId = (int) params[1];
            Bitmap bmPiano;
            bmPiano = BitmapFactory.decodeResource(res, mapResId);
            if (quota == 0) {
                Bitmap bmScritta = Bitmap.createBitmap(bmPiano.getWidth(), bmPiano.getHeight(), bmPiano.getConfig());
                Paint paint = new Paint();
                Canvas mcanvas = new Canvas(bmScritta);
                mcanvas.drawBitmap(bmPiano, 0, 0, null);
                paint.setColor(Color.BLACK);
                paint.setTextSize(70);
                paint.setTextAlign(Paint.Align.LEFT);
                mcanvas.drawText("    Ricerca Posizione in Corso",
                        0, bmPiano.getHeight() / 2, paint);
                mcanvas.drawText("Attendere...", bmPiano.getWidth() / 3, bmPiano.getHeight() / 2 + 72, paint);
                bmPiano = bmScritta;
            }
            return bmPiano;
        }

        @Override
        protected void onPostExecute(Bitmap bmPiano) {
            if (quota == 0) {
                pixelPos[0] = bmPiano.getWidth()/2 - bmPos.getWidth()/2;
                pixelPos[1] = bmPiano.getHeight()/2 - bmPos.getHeight() - 72;
            }
            bmMap = bmPiano;
            mappa = this.quota;
            // Include il refresh della mappa
            updateStatoNodi();
        }
    }
}