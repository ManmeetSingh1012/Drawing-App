package com.example.drawingapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View

class DrawingView(context: Context, attrs: AttributeSet): View(context,attrs) {

    // here bitmap is used to create 2d cordinate system(x and y) where we can draw
    // each point in bitmap is made up of pixel and pixel contains bits that represents the color RGB
    // and canvas is used to  to create bitmap and to  draw on the  bitmap
    // where paint() is used to style the path on canvas

    private var mDrawPath:CustomPath?=null
    private var canvasBitmap: Bitmap?=null
    private var brushsize:Float=0.toFloat()
    private var color= Color.BLACK
    private var canvasPaint:Paint?=null
    private var drawPaint: Paint?=null
    private var canvas: Canvas?=null
    private var paths=ArrayList<CustomPath>()
    private var undopaths=ArrayList<CustomPath>()

    init {
        SetUpthevalue()
    }

    fun undomethod()
    {
        if(paths.isNotEmpty())
        {
            // remove at will remove data at last position and it will give this data to add in undopahts
            undopaths.add(paths.removeAt(paths.size-1))
            invalidate() // i think to call on draw again
        }
    }

    private fun SetUpthevalue()
    {
        mDrawPath=CustomPath(color,brushsize)
        drawPaint=Paint()
        drawPaint!!.color=color
        drawPaint!!.style=Paint.Style.STROKE
        drawPaint!!.strokeCap=Paint.Cap.ROUND
        drawPaint!!.strokeJoin=Paint.Join.ROUND
        //brushsize=20.toFloat()
        canvasPaint=Paint(Paint.DITHER_FLAG)
    }
// use to make a  bitmap of certain hight and width and config == color because bitmap contains color
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        canvasBitmap= Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888) // here config use to tell the pixel that  how it is stored
        canvas= Canvas(canvasBitmap!!)
    }


    // it will draw the bitmap at specific position
    // Bitmap contains pixel of the screen
    // canvas on wich we draw and to hold the pixel we use bitmap
    // paint specify the color size style of the drawing

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas!!.drawBitmap(canvasBitmap!!,0f,0f,canvasPaint)

        for (path in paths)
        {
            drawPaint!!.strokeWidth=path!!.BrushSize
            drawPaint!!.color=path!!.color
            canvas.drawPath(path!!,drawPaint!!)
        }

        if(!mDrawPath!!.isEmpty)
        {
            drawPaint!!.strokeWidth=mDrawPath!!.BrushSize
            drawPaint!!.color=mDrawPath!!.color
            canvas.drawPath(mDrawPath!!,drawPaint!!)
        }
    }
// when screen touches this activities happened
    override fun onTouchEvent(event: MotionEvent?): Boolean {

        var touchX=event?.x
        var touchY=event?.y
        when(event?.action) {
            MotionEvent.ACTION_DOWN -> {
                mDrawPath!!.color = color
                mDrawPath!!.BrushSize = brushsize

                mDrawPath!!.reset()
                if (touchY != null) {
                    if (touchX != null) {
                        mDrawPath!!.moveTo(touchX, touchY)
                    }
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (touchX != null) {
                    if (touchY != null) {
                        mDrawPath!!.lineTo(touchX, touchY)
                    }
                }
            }

            MotionEvent.ACTION_UP -> {
                paths.add(mDrawPath!!)
                mDrawPath = CustomPath(color, brushsize)
            }
            else -> return false
        }
        invalidate()

        return true
    }
    fun setBrushSize(newsize:Float)
    {
        brushsize=TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,newsize,resources.displayMetrics)
        drawPaint!!.strokeWidth==brushsize
    }

    // parse color means change the color string into color int
    fun color(newColor: String)
    {
        color=Color.parseColor(newColor)
    }

    internal inner class CustomPath(var color:Int,var BrushSize:Float): Path() {

    }

}