package com.progettoids.iotforemergency;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.DisplayMetrics;
import android.widget.ImageView;
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

public class MapHome extends AppCompatImageView {

    Matrix matrix = new Matrix();

    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    static final int CLICK = 3;
    int mode = NONE;

    PointF last = new PointF();
    PointF start = new PointF();
    float minScale = 1f;
    float maxScale = 4f;
    float[] m;

    float redundantXSpace, redundantYSpace;
    float width, height;
    float saveScale = 1f;
    float right, bottom, origWidth, origHeight, bmWidth, bmHeight;
    ScaleGestureDetector mScaleDetector;
    Context context;

    Canvas canvas;
    Bitmap bmOverlay, bm145, bm150, bm155, bmPos, bmInc, bmCrollo, bmAffollato ;
    int altezza, larghezza;
    ImageView i;

    public MapHome(Context context, AttributeSet attr)
    {
        super(context, attr);
        super.setClickable(true);
        this.context = context;
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        matrix.setTranslate(1f, 1f);
        m = new float[9];
        setImageMatrix(matrix);
        setScaleType(ScaleType.MATRIX);

// ------- prova per misurare le dimensioni dello schermo --------
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager()
                .getDefaultDisplay()
                .getMetrics(displayMetrics);

        altezza = displayMetrics.heightPixels;
        larghezza = displayMetrics.widthPixels;
// ----------------------------------------------------------------

        bm145 = BitmapFactory.decodeResource(getResources(), R.drawable.map145);
        bm150 = BitmapFactory.decodeResource(getResources(), R.drawable.map150);
        bm155 = BitmapFactory.decodeResource(getResources(), R.drawable.map155);
        bmPos = BitmapFactory.decodeResource(getResources(), R.drawable.posizione);
        bmInc = BitmapFactory.decodeResource(getResources(), R.drawable.incendio);
        bmCrollo = BitmapFactory.decodeResource(getResources(), R.drawable.crollo);
        bmAffollato = BitmapFactory.decodeResource(getResources(), R.drawable.affollamento);
        bmOverlay = Bitmap.createBitmap(bm155.getWidth(), bm155.getHeight(), bm155.getConfig());

        //this.setImageBitmap(bmOverlay);
        canvas = new Canvas(bmOverlay);

        setOnTouchListener(new OnTouchListener()
        {

            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                mScaleDetector.onTouchEvent(event);

                matrix.getValues(m);
                float x = m[Matrix.MTRANS_X];
                float y = m[Matrix.MTRANS_Y];
                PointF curr = new PointF(event.getX(), event.getY());

                switch (event.getAction())
                {
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
                        if (mode == ZOOM || (mode == DRAG && saveScale > minScale))
                        {
                            float deltaX = curr.x - last.x;// x difference
                            float deltaY = curr.y - last.y;// y difference
                            float scaleWidth = Math.round(origWidth * saveScale);// width after applying current scale
                            float scaleHeight = Math.round(origHeight * saveScale);// height after applying current scale
                            //if scaleWidth is smaller than the views width
                            //in other words if the image width fits in the view
                            //limit left and right movement
                            if (scaleWidth < width)
                            {
                                deltaX = 0;
                                if (y + deltaY > 0)
                                    deltaY = -y;
                                else if (y + deltaY < -bottom)
                                    deltaY = -(y + bottom);
                            }
                            //if scaleHeight is smaller than the views height
                            //in other words if the image height fits in the view
                            //limit up and down movement
                            else if (scaleHeight < height)
                            {
                                deltaY = 0;
                                if (x + deltaX > 0)
                                    deltaX = -x;
                                else if (x + deltaX < -right)
                                    deltaX = -(x + right);
                            }
                            //if the image doesnt fit in the width or height
                            //limit both up and down and left and right
                            else
                            {
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
                invalidate();
                return true;
            }
        });
    }
// ------------------------------------ CONVERSIONE -------------------------------------------------------------
    // Conversioni dalle coordinate in metri con cui sono memorizzate le posizioni sul database
    // alle coordinate in pixel per disegnare sulle mappe bitmap

// (l'origine del sistema di riferimento in pixel è in alto a sinistra,
// l'origine del sistema di riferimento in metri è in basso a sinistra)

    int[] conversioneCoordQ155(int x, int y) {
        // dimensione immagine 1184 1600
        // 15A5     X=91m  242px
        //          Y=484m 154px
        int[] coord=new int[2];
        float scala = 6.4f;             // 345px/54m [pixel/metri] è il fattore di scala tra le coordinate in metri del file excell e i pixel dell'immagine bitmap
        float offX = (91-242/scala);    // (91m-242px/scala) è la distanza rispetto a X in metri tra l'origine dell'immagine bitmap e l'origine delle coordinate del file excell
        float offY = (154/scala+484);   // (154px/scala+484m) è la distanza rispetto a Y in metri tra l'origine dell'immagine bitmap e l'origine delle coordinate del file excell (507.2 metri)
        float convX = (x-offX)*scala;
        float convY = (offY-y)*scala;
        coord[0] = (int)convX;
        coord[1] = (int)convY;
        return coord;
    }

    int[] conversioneCoordQ150(int x, int y) {
        // dim immagine 990x1572
         // G2      X=485px Y=270px
        //          X=129m  Y=465m
        int[] coord=new int[2];
        float scala = 6.4f;             // 345px/54m [pixel/metri] è il fattore di scala tra le coordinate in metri del file excell e i pixel dell'immagine bitmap
        float offX = (129-485/scala-(bmOverlay.getWidth()-bm145.getWidth())/(2*scala));   // (129metri-485px/scala) = -53.2 metri è la distanza rispetto a X in metri tra l'origine dell'immagine bitmap e l'origine delle coordinate del file excell
        //(bmOverlay.getWidth()-bm145.getWidth())/2 è stato introdotto perchè l'immagine è leggermente traslata verso destra per far si che sia centrata
        float offY = (270/scala)+465;   // (270/scala)+465 è la distanza rispetto a Y in metri tra l'origine dell'immagine bitmap e l'origine delle coordinate del file excell (507.2 metri)
        float convX = (x-offX)*scala;
        float convY = (offY-y)*scala;
        coord[0] = (int)convX;
        coord[1] = (int)convY;
        return coord;
    }

    int[] conversioneCoordQ145(int x, int y) {      // X perfetta, la Y dovrebbe stare un po' più in basso
        // dim immagine 926x1600
        // 145A5 jpg X = 255px; excell X = 91m
        //           Y = 150px; excell Y =484m
        // 145S3 jpg X = 600px; excell X = 145m
        // OFFSET Y (150px/6.46)+484m*6.4 = 507.4m
        int[] coord=new int[2];
        float scala = 6.4f;             // 345px/54m [pixel/metri] è il fattore di scala tra le coordinate in metri del file excell e i pixel dell'immagine bitmap
        float offX = (91-255/scala-(bmOverlay.getWidth()-bm145.getWidth())/(2*scala));    // (91metri-255px/scala) = 51.2 metri è la distanza rispetto a X in metri tra l'origine dell'immagine bitmap e l'origine delle coordinate del file excell ()
        //(bmOverlay.getWidth()-bm145.getWidth())/2 è stato introdotto perchè l'immagine è leggermente traslata verso destra per far si che sia centrata
        float offY = (150/scala)+484;   // (150px/scala)+484m = 507.4 metri è la distanza rispetto a Y in metri tra l'origine dell'immagine bitmap e l'origine delle coordinate del file excell (507.2 metri)
        float convX = (x-offX)*scala;             // 70.06 sono i metri di offset della x, 6.39 è il fattore di scala per passare da metri a pixel
        float convY = (offY-y)*scala;
        coord[0] = (int)convX;
        coord[1] = (int)convY;
        return coord;
    }

// ------------------------------------------------------------------------------------------------------------
    public  void disegnaPosizione(int x, int y, int quota) {
        int[] pos;
        pos = new int[2];
        pos[0] = 0;
        pos[1] = 0;

        switch (quota) {
            case 145:  canvas.drawBitmap(bm145,(bmOverlay.getWidth()-bm145.getWidth())/2, 0, null);  //(bmOverlay.getWidth()-bm145.getWidth())/2 è usato per centrare l'immagine
                pos = conversioneCoordQ145(x,y);
                break;
            case 150:  canvas.drawBitmap(bm150,(bmOverlay.getWidth()-bm150.getWidth())/2, 0, null);  //(bmOverlay.getWidth()-bm155.getWidth())/2 è usato per centrare l'immagine
                pos = conversioneCoordQ150(x,y);
                break;
            case 155:  canvas.drawBitmap(bm145, 0, 0, null);  //(bmOverlay.getWidth()-bm145.getWidth())/2 è usato per centrare l'immagine
                pos = conversioneCoordQ155(x,y);
                break;
        }
        //per centrare il bitmap dell'icona
        pos[0] = pos[0] - bmPos.getWidth()/2;
        pos[1] = pos[1] - bmPos.getHeight();

        canvas.drawBitmap(bmPos,pos[0],pos[1],null); //720px 435px su gimp origine in alto a sinistra su canvas 690,390
      //  this.setImageBitmap(bmOverlay);
      //  setVisibility(VISIBLE);
        invalidate();
}

    // Disegna la cornice_red intorno alla mappa in base allo stato di emergenza che riceve in ingresso
    public void disegnaEmergenza(int stato, RelativeLayout layout) {
        //Paint linePaint = new Paint();              // linePaint definisce lo stile della cornice_red
        //linePaint.setStrokeWidth(30);               // Spessore della cornice_red
        //linePaint.setColor( Color.GREEN );          // Colore di default della cornice_red = verde
        // Cambia il colore della cornice_red in base allo stato che riceve in ingresso
        switch(stato) {
            case 1: layout.setBackgroundResource(R.drawable.cornice_green);         // Colore per nessuna emergenza (verde)
                break;
            case 2: layout.setBackgroundResource(R.drawable.cornice_red);           // Colore per incendio
                break;
            case 3: layout.setBackgroundResource(R.drawable.cornice_terremoto);        // Colore per terremoto
                break;
        }

        //RelativeLayout layout =(RelativeLayout)findViewById(R.id.activity_home);
        //layout.setBackgroundColor(Color.RED);
        //layout.setBackgroundResource(R.drawable.cornice_green);

        // Disegna le quattro linee della cornice_red
    /*    canvas.drawLine(0,0,0,bmOverlay.getHeight(),linePaint);
        canvas.drawLine(0,0,bmOverlay.getWidth(),0,linePaint);
        canvas.drawLine(bmOverlay.getWidth(),0,bmOverlay.getWidth(),bmOverlay.getHeight(),linePaint);
        canvas.drawLine(0,bmOverlay.getHeight(),bmOverlay.getWidth(),bmOverlay.getHeight(),linePaint);
    */
        //this.setImageBitmap(bmOverlay);
    }

    // Disegna la notifica di pericolo sul nodo corrispondente
    public void disegnaStatoNodo(int stato, int x, int y, int z)
    {
        int[] pixelCoord = {0,0};
        // In base alla quota in cui si trova il nodo effettua la giusta onversione di coordinate da metri in pixel
        switch (z) {
            case 145: pixelCoord=conversioneCoordQ145(x, y);
                break;
            case 150: pixelCoord=conversioneCoordQ150(x, y);
                break;
            case 155: pixelCoord=conversioneCoordQ155(x, y);
                break;
        }

        // prende come riferimento il centro dell'icona anzichè l'angolo in alto a sinistra
        pixelCoord[0] = pixelCoord[0]- bmInc.getWidth()/2;
        pixelCoord[1] = pixelCoord[1]- bmInc.getHeight()/2;

        // In base al tipo di notifica che riceve disegna la corrispondente icona sulla mappa
        switch (stato) {
            case 1: canvas.drawBitmap(bmInc,pixelCoord[0],pixelCoord[1],null);
                break;
            case 2: canvas.drawBitmap(bmCrollo,pixelCoord[0],pixelCoord[1],null);
                break;
            case 3: canvas.drawBitmap(bmAffollato,pixelCoord[0],pixelCoord[1],null);
                break;
        }
    }

    @Override
    public void setImageBitmap(Bitmap bm)
    {
        super.setImageBitmap(bm);
        bmWidth = bm.getWidth();
        bmHeight = bm.getHeight();
    }

    public void setMaxZoom(float x)
    {
        maxScale = x;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener
    {

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector)
        {
            mode = ZOOM;
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector)
        {
            float mScaleFactor = detector.getScaleFactor();
            float origScale = saveScale;
            saveScale *= mScaleFactor;
            if (saveScale > maxScale)
            {
                saveScale = maxScale;
                mScaleFactor = maxScale / origScale;
            }
            else if (saveScale < minScale)
            {
                saveScale = minScale;
                mScaleFactor = minScale / origScale;
            }
            right = width * saveScale - width - (2 * redundantXSpace * saveScale);
            bottom = height * saveScale - height - (2 * redundantYSpace * saveScale);
            if (origWidth * saveScale <= width || origHeight * saveScale <= height)
            {
                matrix.postScale(mScaleFactor, mScaleFactor, width / 2, height / 2);
                if (mScaleFactor < 1)
                {
                    matrix.getValues(m);
                    float x = m[Matrix.MTRANS_X];
                    float y = m[Matrix.MTRANS_Y];
                    if (mScaleFactor < 1)
                    {
                        if (Math.round(origWidth * saveScale) < width)
                        {
                            if (y < -bottom)
                                matrix.postTranslate(0, -(y + bottom));
                            else if (y > 0)
                                matrix.postTranslate(0, -y);
                        }
                        else
                        {
                            if (x < -right)
                                matrix.postTranslate(-(x + right), 0);
                            else if (x > 0)
                                matrix.postTranslate(-x, 0);
                        }
                    }
                }
            }
            else
            {
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
    protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);
        //Fit to screen.
        float scale;
        float scaleX =  width / bmWidth;
        float scaleY = height / bmHeight;
        scale = Math.min(scaleX, scaleY);
        matrix.setScale(scale, scale);
        setImageMatrix(matrix);
        saveScale = 1f;

        // Center the image
        redundantYSpace = height - (scale * bmHeight) ;
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

    @Override
    protected  void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        this.setImageBitmap(bmOverlay);
    }

}
