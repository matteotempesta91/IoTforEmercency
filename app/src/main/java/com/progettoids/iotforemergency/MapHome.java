package com.progettoids.iotforemergency;

import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
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
        pixelPos = new int[]{0,0};

        bmPos = BitmapFactory.decodeResource(getResources(), R.drawable.posizione);
        bmInc = BitmapFactory.decodeResource(getResources(), R.drawable.incendio);
        bmCrollo = BitmapFactory.decodeResource(getResources(), R.drawable.crollo);
        bmAffollato = BitmapFactory.decodeResource(getResources(), R.drawable.affollamento);

        LoadMap lmap = new LoadMap(150);
        lmap.execute(getResources());

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
    }

// ------------------------------------ CONVERSIONE -------------------------------------------------------------

// Conversioni dalle coordinate in metri con cui sono memorizzate le posizioni sul database
// alle coordinate in pixel per disegnare sulle mappe bitmap
// (l'origine del sistema di riferimento in pixel è in alto a sinistra,
// l'origine del sistema di riferimento in metri è in basso a sinistra)
    private int[] conversioneCoordQ155(int x, int y) {
        // dimensione immagine 1184 1600
        // 15A5     X=91m  242px
        //          Y=484m 154px
        int[] coord = new int[2];
        float scala = 6.4f;             // 345px/54m [pixel/metri] è il fattore di scala tra le coordinate in metri del file excell e i pixel dell'immagine bitmap
        float offX = (91 - 242 / scala);    // (91m-242px/scala) è la distanza rispetto a X in metri tra l'origine dell'immagine bitmap e l'origine delle coordinate del file excell
        float offY = (154 / scala + 484);   // (154px/scala+484m) è la distanza rispetto a Y in metri tra l'origine dell'immagine bitmap e l'origine delle coordinate del file excell (507.2 metri)
        float convX = (x - offX) * scala;
        float convY = (offY - y) * scala;
        coord[0] = (int) convX;
        coord[1] = (int) convY;
        return coord;
    }

    private int[] conversioneCoordQ150(int x, int y) {
        // dim immagine 990x1572
        // G2      X=485px Y=270px
        //          X=129m  Y=465m
        int[] coord = new int[2];
        float scala = 6.4f;             // 345px/54m [pixel/metri] è il fattore di scala tra le coordinate in metri del file excell e i pixel dell'immagine bitmap
        float offX = (129 - 485 / scala);   // (129metri-485px/scala) = -53.2 metri è la distanza rispetto a X in metri tra l'origine dell'immagine bitmap e l'origine delle coordinate del file excell
        //(bmMap.getWidth()-bm145.getWidth())/2 è stato introdotto perchè l'immagine è leggermente traslata verso destra per far si che sia centrata
        float offY = (270 / scala) + 465;   // (270/scala)+465 è la distanza rispetto a Y in metri tra l'origine dell'immagine bitmap e l'origine delle coordinate del file excell (507.2 metri)
        float convX = (x - offX) * scala;
        float convY = (offY - y) * scala;
        coord[0] = (int) convX;
        coord[1] = (int) convY;
        return coord;
    }

    private int[] conversioneCoordQ145(int x, int y) {      // X perfetta, la Y dovrebbe stare un po' più in basso
        // dim immagine 926x1600
        // 145A5 jpg X = 255px; excell X = 91m
        //           Y = 150px; excell Y =484m
        // 145S3 jpg X = 600px; excell X = 145m
        // OFFSET Y (150px/6.46)+484m*6.4 = 507.4m
        //Bitmap bm145 = bmVarie[0];
        int[] coord = new int[2];
        float scala = 6.4f;             // 345px/54m [pixel/metri] è il fattore di scala tra le coordinate in metri del file excell e i pixel dell'immagine bitmap
        float offX = (91 - 255 / scala);    // (91metri-255px/scala) = 51.2 metri è la distanza rispetto a X in metri tra l'origine dell'immagine bitmap e l'origine delle coordinate del file excell ()
        //(bmMap.getWidth()-bm145.getWidth())/2 è stato introdotto perchè l'immagine è leggermente traslata verso destra per far si che sia centrata
        float offY = (150 / scala) + 484;   // (150px/scala)+484m = 507.4 metri è la distanza rispetto a Y in metri tra l'origine dell'immagine bitmap e l'origine delle coordinate del file excell (507.2 metri)
        float convX = (x - offX) * scala;             // 70.06 sono i metri di offset della x, 6.39 è il fattore di scala per passare da metri a pixel
        float convY = (offY - y) * scala;
        coord[0] = (int) convX;
        coord[1] = (int) convY;
        return coord;
    }

// ------------------------------------------------------------------------------------------------------------
    public void disegnaPosizione(int x, int y, int quota) {
        // La mappa è ricaricata solo se necessario
        if (quota != mappa) {
            LoadMap lmap = new LoadMap(quota);
            lmap.execute(getResources());
        }
        switch (quota) {
            case 145:
                pixelPos = conversioneCoordQ145(x, y);
                break;
            case 150:
                pixelPos = conversioneCoordQ150(x, y);
                break;
            case 155:
                pixelPos = conversioneCoordQ155(x, y);
                break;
        }
        //per centrare il bitmap dell'icona
        pixelPos[0] = pixelPos[0] - bmPos.getWidth() / 2;
        pixelPos[1] = pixelPos[1] - bmPos.getHeight();
        //canvas.drawBitmap(bmPos, pos[0], pos[1], null); //720px 435px su gimp origine in alto a sinistra su canvas 690,390
    }

    // Disegna la cornice_red intorno alla mappa in base allo stato di emergenza che riceve in ingresso
    public void disegnaEmergenza(int stato, RelativeLayout layout) {
        //Paint linePaint = new Paint();              // linePaint definisce lo stile della cornice_red
        //linePaint.setStrokeWidth(30);               // Spessore della cornice_red
        //linePaint.setColor( Color.GREEN );          // Colore di default della cornice_red = verde
        // Cambia il colore della cornice_red in base allo stato che riceve in ingresso
        switch (stato) {
            case 1:
                layout.setBackgroundResource(R.drawable.cornice_green);         // Colore per nessuna emergenza (verde)
                break;
            case 2:
                layout.setBackgroundResource(R.drawable.cornice_red);           // Colore per incendio
                break;
            case 3:
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

    // Disegna la notifica di pericolo sul nodo corrispondente
    public void disegnaStatoNodo(int stato, int x, int y, int z) {
        int[] pixelCoord = {0, 0};
        Canvas canvas = new Canvas(bmNodi);
        // In base alla quota in cui si trova il nodo effettua la giusta onversione di coordinate da metri in pixel
        switch (z) {
            case 145:
                pixelCoord = conversioneCoordQ145(x, y);
                break;
            case 150:
                pixelCoord = conversioneCoordQ150(x, y);
                break;
            case 155:
                pixelCoord = conversioneCoordQ155(x, y);
                break;
        }
        // prende come riferimento il centro dell'icona anzichè l'angolo in alto a sinistra
        pixelCoord[0] = pixelCoord[0] - bmInc.getWidth() / 2;
        pixelCoord[1] = pixelCoord[1] - bmInc.getHeight() / 2;

        // In base al tipo di notifica che riceve disegna la corrispondente icona sulla mappa
        switch (stato) {
            case 0:
                int w = bmInc.getWidth();
                int h = bmInc.getHeight();
                int[] pix = new int[w*h];
                Arrays.fill(pix, 0x0000);
                bmNodi.setPixels(pix, 0, w, pixelCoord[0], pixelCoord[1], w, h);
                break;
            case 1:
                canvas.drawBitmap(bmInc, pixelCoord[0], pixelCoord[1], null);
                break;
            case 2:
                canvas.drawBitmap(bmCrollo, pixelCoord[0], pixelCoord[1], null);
                break;
            case 3:
                canvas.drawBitmap(bmAffollato, pixelCoord[0], pixelCoord[1], null);
                break;
        }
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
//  ---------------------------------------------------------------

    // Inner asyncTask per caricare la Mappa
    private class LoadMap extends AsyncTask<Object, Void, Bitmap> {
        int quota;

        private LoadMap(int quota) {
            super();
            this.quota = quota;
        }

        @Override
        protected Bitmap doInBackground(Object... params) {

            Resources res = (Resources) params[0];
            Bitmap bmPiano = null;
            switch (quota) {
                case 145:
                    bmPiano = BitmapFactory.decodeResource(res, R.drawable.map145);
                    break;
                case 150:
                    bmPiano = BitmapFactory.decodeResource(res, R.drawable.map150);
                    break;
                case 155:
                    bmPiano = BitmapFactory.decodeResource(res, R.drawable.map155);
                    break;
            }
            return bmPiano;
        }

        @Override
        protected void onPostExecute(Bitmap bmPiano) {
            bmMap = bmPiano;
            bmNodi = Bitmap.createBitmap(bmPiano.getWidth(), bmPiano.getHeight(), bmPiano.getConfig());
            mappa = this.quota;
            /* debug code
            disegnaStatoNodo(1,143,473,145); // 145A3 (vicino le scale)
            disegnaStatoNodo(2,90,480,145);  // 145RG1 (sinistra di G1 sotto le scale)
            disegnaStatoNodo(3,133,465,145); // 145WC1
            disegnaStatoNodo(3,119,465,145);
            */
            Cursor cr = DBManager.getStatoNodi();
            while (cr.moveToNext()) {
                int stato = cr.getInt(0);
                int x = cr.getInt(1);
                int y = cr.getInt(2);
                int z = cr.getInt(3);
                disegnaStatoNodo(stato, x, y, z);
            }
            setBitmap();
        }
    }
}