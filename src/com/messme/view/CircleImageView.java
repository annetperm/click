package com.messme.view;

import com.messme.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;


public class CircleImageView extends ImageView 
{
    private static final ScaleType SCALE_TYPE = ScaleType.CENTER_CROP;

    private static final Bitmap.Config BITMAP_CONFIG = Bitmap.Config.ARGB_8888;
    private static final int COLORDRAWABLE_DIMENSION = 2;

    private static final boolean DEFAULT_BORDER_OVERLAY = false;

    private final RectF _DrawableRect = new RectF();
    private final RectF _BorderRect = new RectF();

    private final Matrix _ShaderMatrix = new Matrix();
    private final Paint _BitmapPaint = new Paint();
    private final Paint _BitmapPaintPressed = new Paint();
    private final Paint _BorderPaint = new Paint();

    private int _BorderColor;
    private int _BorderWidth;

    private Bitmap _Bitmap;
    private boolean _Pressed = false;
    private BitmapShader _BitmapShader;
    private BitmapShader _BitmapShaderPressed;
    private int _BitmapWidth;
    private int _BitmapHeight;

    private float _DrawableRadius;
    private float _BorderRadius;

    private ColorFilter _ColorFilter;

    private boolean _Ready;
    private boolean _SetupPending;
    private boolean _BorderOverlay;

    public CircleImageView(Context context) 
    {
        super(context);
        init();
    }

    public CircleImageView(Context context, AttributeSet attrs) 
    {
        this(context, attrs, 0);
    }

    public CircleImageView(Context context, AttributeSet attrs, int defStyle) 
    {
        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircleImageView, defStyle, 0);

        _BorderWidth = 2;
        _BorderColor = Color.argb(8, 0, 0, 0);
        _BorderOverlay = a.getBoolean(R.styleable.CircleImageView_civ_border_overlay, DEFAULT_BORDER_OVERLAY);

        a.recycle();

        init();
    }

    private void init() 
    {
        super.setScaleType(SCALE_TYPE);
        _Ready = true;

        if (_SetupPending) 
        {
            setup();
            _SetupPending = false;
        }
    }

    @Override
    public ScaleType getScaleType() 
    {
        return SCALE_TYPE;
    }

    @Override
    public void setScaleType(ScaleType scaleType) 
    {
        if (scaleType != SCALE_TYPE) 
        {
            throw new IllegalArgumentException(String.format("ScaleType %s not supported.", scaleType));
        }
    }

    @Override
    public void setAdjustViewBounds(boolean adjustViewBounds) 
    {
        if (adjustViewBounds) 
        {
            throw new IllegalArgumentException("adjustViewBounds not supported.");
        }
    }

    @Override
    protected void onDraw(Canvas canvas) 
    {
        if (_Bitmap == null) 
        {
            return;
        }

        if (_Pressed)
        {
            canvas.drawCircle(getWidth() / 2.0f, getHeight() / 2.0f, _DrawableRadius, _BitmapPaintPressed);
        }
        else
        {
            canvas.drawCircle(getWidth() / 2.0f, getHeight() / 2.0f, _DrawableRadius, _BitmapPaint);
        }
        if (_BorderWidth != 0) 
        {
            canvas.drawCircle(getWidth() / 2.0f, getHeight() / 2.0f, _BorderRadius, _BorderPaint);
        }
    }
    
    @SuppressLint("ClickableViewAccessibility")
	@Override
    public boolean onTouchEvent(MotionEvent event)
    {
        switch (event.getAction()) 
        {
            case MotionEvent.ACTION_DOWN: 
            	if (super.isClickable())
            	{
                	_Pressed = true;
                	invalidate();
            	}
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: 
            	_Pressed = false;
            	invalidate();
                break;
        }
    	return super.onTouchEvent(event);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) 
    {
        super.onSizeChanged(w, h, oldw, oldh);
        setup();
    }

    public int getBorderColor() 
    {
        return _BorderColor;
    }

    public void setBorderColor(@ColorInt int borderColor) 
    {
        if (borderColor == _BorderColor) 
        {
            return;
        }

        _BorderColor = borderColor;
        _BorderPaint.setColor(_BorderColor);
        invalidate();
    }

    public void setBorderColorResource(@ColorRes int borderColorRes) 
    {
        setBorderColor(getContext().getResources().getColor(borderColorRes));
    }

    public int getBorderWidth() 
    {
        return _BorderWidth;
    }

    public void setBorderWidth(int borderWidth) 
    {
        if (borderWidth == _BorderWidth) 
        {
            return;
        }

        _BorderWidth = borderWidth;
        setup();
    }

    public boolean isBorderOverlay() 
    {
        return _BorderOverlay;
    }

    public void setBorderOverlay(boolean borderOverlay) 
    {
        if (borderOverlay == _BorderOverlay) 
        {
            return;
        }

        _BorderOverlay = borderOverlay;
        setup();
    }

    @Override
    public void setImageBitmap(Bitmap bm) 
    {
        super.setImageBitmap(bm);
        _Bitmap = bm;
        setup();
    }

    @Override
    public void setImageDrawable(Drawable drawable) 
    {
        super.setImageDrawable(drawable);
        _Bitmap = getBitmapFromDrawable(drawable);
        setup();
    }

    @Override
    public void setImageResource(@DrawableRes int resId) 
    {
        super.setImageResource(resId);
        _Bitmap = getBitmapFromDrawable(getDrawable());
        setup();
    }

    @Override
    public void setImageURI(Uri uri) 
    {
        super.setImageURI(uri);
        _Bitmap = uri != null ? getBitmapFromDrawable(getDrawable()) : null;
        setup();
    }

    @Override
    public void setColorFilter(ColorFilter cf) 
    {
        if (cf == _ColorFilter) 
        {
            return;
        }

        _ColorFilter = cf;
        _BitmapPaint.setColorFilter(_ColorFilter);
        invalidate();
    }

    private Bitmap getBitmapFromDrawable(Drawable drawable)
    {
        if (drawable == null) 
        {
            return null;
        }

        if (drawable instanceof BitmapDrawable) 
        {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        try 
        {
            Bitmap bitmap;

            if (drawable instanceof ColorDrawable) 
            {
                bitmap = Bitmap.createBitmap(COLORDRAWABLE_DIMENSION, COLORDRAWABLE_DIMENSION, BITMAP_CONFIG);
            } 
            else 
            {
                bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), BITMAP_CONFIG);
            }

            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
            return null;
        }
    }

    private void setup() 
    {
        if (!_Ready) 
        {
            _SetupPending = true;
            return;
        }

        if (getWidth() == 0 && getHeight() == 0) 
        {
            return;
        }

        if (_Bitmap == null) 
        {
            invalidate();
            return;
        }

        _BitmapShader = new BitmapShader(_Bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

        _BitmapPaint.setAntiAlias(true);
        _BitmapPaint.setShader(_BitmapShader);

        _BorderPaint.setStyle(Paint.Style.STROKE);
        _BorderPaint.setAntiAlias(true);
        _BorderPaint.setColor(_BorderColor);
        _BorderPaint.setStrokeWidth(_BorderWidth);

        _BitmapHeight = _Bitmap.getHeight();
        _BitmapWidth = _Bitmap.getWidth();

        _BorderRect.set(0, 0, getWidth(), getHeight());
        _BorderRadius = Math.min((_BorderRect.height() - _BorderWidth) / 2.0f, (_BorderRect.width() - _BorderWidth) / 2.0f);

        _DrawableRect.set(_BorderRect);
        if (!_BorderOverlay) 
        {
            _DrawableRect.inset(_BorderWidth, _BorderWidth);
        }
        _DrawableRadius = Math.min(_DrawableRect.height() / 2.0f, _DrawableRect.width() / 2.0f);
        
        LightingColorFilter filter = new LightingColorFilter(0x9999FF, 0x000099);
        _BitmapShaderPressed = new BitmapShader(_Bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        _BitmapPaintPressed.setAntiAlias(true);
        _BitmapPaintPressed.setColorFilter(filter);
        _BitmapPaintPressed.setShader(_BitmapShaderPressed);
        
        updateShaderMatrix();
        invalidate();
    }

    private void updateShaderMatrix() 
    {
        float scale;
        float dx = 0;
        float dy = 0;

        _ShaderMatrix.set(null);

        if (_BitmapWidth * _DrawableRect.height() > _DrawableRect.width() * _BitmapHeight) 
        {
            scale = _DrawableRect.height() / (float) _BitmapHeight;
            dx = (_DrawableRect.width() - _BitmapWidth * scale) * 0.5f;
        } 
        else 
        {
            scale = _DrawableRect.width() / (float) _BitmapWidth;
            dy = (_DrawableRect.height() - _BitmapHeight * scale) * 0.5f;
        }

        _ShaderMatrix.setScale(scale, scale);
        _ShaderMatrix.postTranslate((int) (dx + 0.5f) + _DrawableRect.left, (int) (dy + 0.5f) + _DrawableRect.top);

        _BitmapShader.setLocalMatrix(_ShaderMatrix);
        _BitmapShaderPressed.setLocalMatrix(_ShaderMatrix);
    }
}