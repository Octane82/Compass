package com.example.octane.maycompass;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;

/**
 * Created by octane on 02.06.15.
 */
public class CompassView extends View {

    private float bearing;

    private Paint markerPaint;
    private Paint textPaint;
    private Paint circlePaint;
    private String northString;
    private String eastString;
    private String southString;
    private String westString;
    private int textHeight;


    public CompassView(Context context) {
        super(context);
        initCompassView();
    }

    public CompassView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initCompassView();
    }

    public CompassView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initCompassView();
    }


    protected void initCompassView(){
        setFocusable(true);

        Resources r = getResources();
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(r.getColor(R.color.background_color));
        circlePaint.setStrokeWidth(1);
        circlePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        northString = r.getString(R.string.cardinal_north);
        eastString = r.getString(R.string.cardinal_east);
        southString = r.getString(R.string.cardinal_south);
        westString = r.getString(R.string.cardinal_west);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(r.getColor(R.color.text_color));

        textHeight = (int)textPaint.measureText("yY");

        markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        markerPaint.setColor(r.getColor(R.color.marker_color));
    }

    // В этом методе происходит отрисовка
    @Override
    protected void onDraw(Canvas canvas) {
        // Находим центральную точку элемента управления
        int mMeasuredWidth = getMeasuredWidth();
        int mMeasuredHeight = getMeasuredHeight();

        int px = mMeasuredWidth / 2;
        int py = mMeasuredHeight / 2;

        int radius = Math.min(px, py);

        // Нарисуем фон
        canvas.drawCircle(px, py, radius, circlePaint);

        // Поворачиваем ракурс таким образом, чтобы "верх" всегда указывал на
        // текущее направление
        canvas.save();
        canvas.rotate(-bearing, px, py);


        int textWidth = (int)textPaint.measureText("W");
        int cardinalX = px-textWidth / 2;
        int cardinalY = py-radius+textHeight;

        // Рисуем отметки каждый 15 градусов и текст 45
        for(int i=0; i<24; i++){
            // Нарисуем метку
            canvas.drawLine(px, py-radius, px, py-radius+10, markerPaint);
            canvas.save();
            canvas.translate(0, textHeight);

            // Нарисуем основные точки
            if(i%6 == 0){
                String dirString = "";
                switch (i){
                    case (0) : {
                        dirString = northString;
                        int arrowY = 2*textHeight;
                        canvas.drawLine(px, arrowY, px-5, 3*textHeight, markerPaint);
                        canvas.drawLine(px, arrowY, px+5, 3+textHeight, markerPaint);
                        break;
                    }
                    case (6) : dirString = eastString; break;
                    case (12) : dirString = southString; break;
                    case (18) : dirString = westString; break;
                }
                canvas.drawText(dirString, cardinalX, cardinalY, textPaint);
            }else if (i%3 == 0){
                // Отображаем текст каждые 45 градусов
                String angle = String.valueOf(i*15);
                float angleTextWidth = textPaint.measureText(angle);

                int angleTextX = (int)(px-angleTextWidth/2);
                int angleTextY = py-radius+textHeight;
                canvas.drawText(angle, angleTextX, angleTextY, textPaint);
            }
            canvas.restore();
            canvas.rotate(15, px, py);

        }
        canvas.restore();

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Компасс представляет собой окружность,
        // занимающее всё доступное пространство
        // Устанавливаем размеры элемента , вычислив короткую грань (высоту или ширину)
        int measuredWidth = measure(widthMeasureSpec);
        int measureHeight = measure(heightMeasureSpec);

        int d = Math.min(measuredWidth, measureHeight);

        // Устанавливаем высоту и ширину
        setMeasuredDimension(d, d);

    }


    private  int  measure(int  measureSpec)  {
        int  result  =  0;
        //  Декодируйте  параметр  measureSpec.
        int  specMode  =  MeasureSpec.getMode(measureSpec);
        int  specSize  =  MeasureSpec.getSize(measureSpec);
        if  (specMode  ==  MeasureSpec.UNSPECIFIED)  {
            //  Если  границы  не  указаны,  верните  размер  по  умолчанию  (200).
            result  =  200;
        }  else  {
            //  Так  как  вам  нужно  заполнить  все  доступное  пространство,
            //  всегда  возвращайте  максимальный  доступный  размер,
            result  =  specSize;
        }
        return  result;
    }


    public float getBearing() {
        return bearing;
    }

    public void setBearing(float _bearing) {
        bearing = _bearing;
        // Текст изменяется вместе с направлением
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED);
    }

    // Переопределяем этот метод, чтобы в качестве содержимого он использовал текущее направление
    @Override
    public boolean dispatchPopulateAccessibilityEvent(final AccessibilityEvent event) {
        super.dispatchPopulateAccessibilityEvent(event);

       if  (isShown()) 	{
            String bearingStr = String.valueOf(bearing);
            if(bearingStr.length() > AccessibilityEvent.MAX_TEXT_LENGTH){
                bearingStr = bearingStr.substring(0, AccessibilityEvent.MAX_TEXT_LENGTH);
                event.getText().add(bearingStr);

            }
            return  true;
        }
        else
            return  false;
    }


}
