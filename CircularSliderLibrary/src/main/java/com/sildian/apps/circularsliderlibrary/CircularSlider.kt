package com.sildian.apps.circularsliderlibrary

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.view.GestureDetectorCompat
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.round

/*************************************************************************************************
 * A view allowing to set a value by swiping the finger on a circular slider
 ************************************************************************************************/

@Suppress("unused")
class CircularSlider(context:Context, attrs:AttributeSet) : View(context, attrs) {

    /*************************************Callbacks**********************************************/

    interface OnValueChangedListener{
        fun onValueChanged(view:CircularSlider, value:Int)
    }

    /********************************Xml editable Attributes*************************************/

    private var minValue=0                          //Min value of the slider
    private var maxValue=100                        //Max value of the slider
    private var currentValue=this.minValue          //Current value of the slider
    private var stepValue=1                         //The step used to round the current value
    private var showValueText=true                  //Defines whether the value text is shown or not
    private var sliderWidth=5f                      //The slider's width
    private var sliderColor=Color.BLUE              //The slider's color
    private var valueTextSize=100f                  //The value text's size
    private var valueTextColor=Color.BLUE           //The value text's color

    /********************************Non editable attributes*************************************/

    private var centerX=0f                          //Center X of the view
    private var centerY=0f                          //Center Y of the view
    private val beginAngle=135f                     //The begin angle of the slider (in degrees)
    private val lengthAngle=270f                    //The length angle of the slider (in degrees)
    private val minAngleGesture=45.0                //The min angle allowed to intercept gestures
    private val maxAngleGesture=315.0               //The max angle allowed to intercept gestures

    /***************************************Paints***********************************************/

    //Paint for the back slider
    private val backSliderPaint=Paint(ANTI_ALIAS_FLAG).apply{
        color=Color.BLACK
        style=Paint.Style.STROKE
        alpha=51
        strokeWidth=sliderWidth
    }

    //Paint for the front slider
    private val frontSliderPaint=Paint(ANTI_ALIAS_FLAG).apply{
        color=sliderColor
        style=Paint.Style.STROKE
        alpha=204
        strokeWidth=sliderWidth
    }

    //Paint for the text
    private val valueTextPaint=Paint(ANTI_ALIAS_FLAG).apply{
        color=valueTextColor
        style=Paint.Style.FILL
        alpha=204
        textSize=valueTextSize
    }
    /************************************Gesture detector****************************************/

    private val gestureDetector=GestureDetectorCompat(this.context, GestureListener())

    /**********************************Callbacks listeners**************************************/

    private var onValueChangedListener:OnValueChangedListener?=null
    private var onValueChangedCallback:((view:CircularSlider, value:Int)->Unit)?=null

    /*************************************Value formater****************************************/

    var valueFormatter:ValueFormatter?=null           //Formats the valueText before displaying it

    /****************************************Init***********************************************/

    init{
        context.theme.obtainStyledAttributes(attrs, R.styleable.CircularSlider, 0, 0).apply{
            minValue=getInteger(R.styleable.CircularSlider_minValue, 0)
            maxValue=getInteger(R.styleable.CircularSlider_maxValue, 100)
            currentValue=getInteger(R.styleable.CircularSlider_currentValue, minValue)
            stepValue=getInteger(R.styleable.CircularSlider_stepValue, 1)
            showValueText=getBoolean(R.styleable.CircularSlider_showValueText, true)
            setSliderWidth(getDimension(R.styleable.CircularSlider_sliderWidth, 5f))
            setSliderColor(getColor(R.styleable.CircularSlider_sliderColor, Color.BLUE))
            setValueTextSize(getDimension(R.styleable.CircularSlider_valueTextSize, 100f))
            setValueTextColor(getColor(R.styleable.CircularSlider_valueTextColor, Color.BLUE))
        }
    }

    /*************************************Attributes update**************************************/

    fun setMinValue(value: Int){
        this.minValue=value
        invalidate()
    }

    fun setMaxValue(value:Int){
        this.maxValue=value
        invalidate()
    }

    fun setCurrentValue(value: Int){
        this.currentValue=value
        invalidate()
    }

    fun setStepValue(value:Int){
        this.stepValue=value
        invalidate()
    }

    fun setShowValueText(show:Boolean){
        this.showValueText=show
        invalidate()
    }

    fun setSliderWidth(width:Float){
        this.sliderWidth=width
        this.backSliderPaint.strokeWidth=this.sliderWidth
        this.frontSliderPaint.strokeWidth=this.sliderWidth
        invalidate()
    }

    fun setSliderColor(color:Int){
        this.sliderColor=color
        this.frontSliderPaint.color=this.sliderColor
        invalidate()
    }

    fun setValueTextSize(size:Float){
        this.valueTextSize=size
        this.valueTextPaint.textSize=this.valueTextSize
        invalidate()
    }

    fun setValueTextColor(color:Int){
        this.valueTextColor=color
        this.valueTextPaint.color=this.valueTextColor
        invalidate()
    }

    /****************************Callbacks listeners monitoring*********************************/

    fun addOnValueChangedListener(onValueChangedListener: OnValueChangedListener){
        this.onValueChangedListener=onValueChangedListener
    }

    fun addOnValueChangedListener(onValueChangedCallback:(view:CircularSlider, value:Int)->Unit){
        this.onValueChangedCallback=onValueChangedCallback
    }

    /****************************************Draw***********************************************/

    /**On draw**/

    override fun onDraw(canvas: Canvas?) {
        calculateCenter()
        drawBackSlider(canvas)
        drawFrontSlider(canvas)
        if(this.showValueText) {
            drawValueText(canvas)
        }
    }

    /**Refreshes the center of the view**/

    private fun calculateCenter(){
        this.centerX=this.width.toFloat()/2
        this.centerY=this.height.toFloat()/2
    }

    /**Draws the back slider**/

    private fun drawBackSlider(canvas: Canvas?){
        canvas?.drawArc(
            RectF(0f+paddingLeft,
                0f+paddingTop,
                width.toFloat()-paddingRight,
                height.toFloat()-paddingBottom),
            this.beginAngle,
            this.lengthAngle,
            false,
            this.backSliderPaint
        )
    }

    /**Draws the front slider**/

    private fun drawFrontSlider(canvas: Canvas?){
        canvas?.drawArc(
            RectF(0f+paddingLeft,
                0f+paddingTop,
                width.toFloat()-paddingRight,
                height.toFloat()-paddingBottom),
            this.beginAngle,
            this.lengthAngle*(this.currentValue-this.minValue)/(this.maxValue-this.minValue),
            false,
            this.frontSliderPaint
        )
    }

    /**Draws the value text**/

    private fun drawValueText(canvas: Canvas?){
        val valueToDisplay=
            if(this.valueFormatter==null){
                this.currentValue.toString()
            }else{
                this.valueFormatter!!.formatValue(this.currentValue, this.context)
            }
        val textWidth=this.valueTextPaint.measureText(valueToDisplay)
        val textX=this.centerX-textWidth/2
        val textY=this.centerY
        canvas?.drawText(
            valueToDisplay,
            textX,
            textY,
            this.valueTextPaint)
    }

    /**********************************Measure***************************************************/

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val minPadding = (TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            1f,
            context.resources.displayMetrics
        ) * this.sliderWidth)
            .toInt()

        setPadding(
            if(paddingLeft==0) minPadding else paddingLeft,
            if(paddingTop==0) minPadding else paddingTop,
            if(paddingStart==0) minPadding else paddingStart,
            if(paddingRight==0) minPadding else paddingRight
        )

        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)

        val h = w

        setMeasuredDimension(w, h)
    }

    /***********************************Gestures************************************************/

    /**Performs click**/

    override fun performClick(): Boolean {
        return super.performClick()
    }

    /**Prevents the parent layout from getting the touch event**/

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if(event?.action == MotionEvent.ACTION_DOWN){
            parent?.requestDisallowInterceptTouchEvent(true)
        }
        return super.dispatchTouchEvent(event)
    }

    /**Touch event**/

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        performClick()
        return gestureDetector.onTouchEvent(event).let { result ->
            if (!result) {
                if (event?.action == MotionEvent.ACTION_MOVE) {
                    val angle=calculateGestureAngle(event)
                    updateCurrentValue(angle)
                    invalidate()
                    true
                } else false
            } else true
        }
    }

    /**Calculates the gesture angle**/

    private fun calculateGestureAngle(event:MotionEvent):Double{
        var angle=Math.toDegrees(atan2((this.centerX-event.x).toDouble(), (this.centerY-event.y).toDouble()))
        if(event.x>this.centerX){
            angle=(360.0+angle)
        }
        angle-=180.0
        if(angle<0.0){
            angle*=-1.0
        }else{
            angle=180.0+(180.0-angle)
        }
        return angle
    }

    /**Updates the current value with the gesture angle**/

    private fun updateCurrentValue(angle:Double) {
        val progress = angle - this.minAngleGesture
        val maxProgress = this.maxAngleGesture - this.minAngleGesture
        this.currentValue =
            this.minValue + round((progress / maxProgress * (this.maxValue.toDouble() - this.minValue.toDouble()))).toInt()
        if (this.currentValue < this.minValue) this.currentValue = this.minValue
        if (this.currentValue > this.maxValue) this.currentValue = this.maxValue
        roundCurrentValueWithStep()
        this.onValueChangedListener?.onValueChanged(this, this.currentValue)
        this.onValueChangedCallback?.invoke(this, this.currentValue)
    }

    /**Rounds the current value using the step**/

    private fun roundCurrentValueWithStep(){
        val floor=floor(this.currentValue.toDouble()/this.stepValue.toDouble())*this.stepValue.toDouble()
        val cap=ceil(this.currentValue.toDouble()/this.stepValue.toDouble())*this.stepValue.toDouble()
        val middle=(cap-floor)/2
        if((this.currentValue-floor)<middle) this.currentValue=floor.toInt()
        else this.currentValue=cap.toInt()
    }

    /**Gesture detector**/

    inner class GestureListener:GestureDetector.SimpleOnGestureListener(){
        override fun onDown(e: MotionEvent?): Boolean {
            return true
        }
    }
}