package com.messme.util;

import java.io.ByteArrayOutputStream;

import com.messme.data.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Base64;


public class ImageUtil
{
	public static final int MAX_SIZE = 300;

	public static Bitmap LoadImageFromDB(Context pContext, String pID)
	{
		DB db = new DB(pContext);
		SQLiteDatabase reader = db.getReadableDatabase();
		Cursor cursor = reader.query("images", new String[]{"image"}, "url = ?", new String[]{pID}, null, null, null);
		Bitmap bitmap = null;
		if (cursor.moveToFirst()) 
		{
			byte[] blob = cursor.getBlob(0);
//	    	String image = cursor.getString(0) + cursor.getString(1) + cursor.getString(2);
//			byte[] encodeByte = Base64.decode(image, Base64.DEFAULT);
			bitmap = BitmapFactory.decodeByteArray(blob, 0, blob.length);
		}
		cursor.close();
		reader.close();
		db.close();
		return bitmap;
	}
	public static void SaveImageToDB(Context pContext, Bitmap pBitmap, String pAttachID)
	{
		DB db = new DB(pContext);
		SQLiteDatabase writer = db.getReadableDatabase();
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	pBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		ContentValues cv = new ContentValues();
		cv.put("url", pAttachID);
		cv.put("image", baos.toByteArray());
		
//		String image = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
//		String image1 = image.substring(0 , image.length() / 3);
//		String image2 = image.substring(image.length() / 3 , 2 * image.length() / 3);
//		String image3 = image.substring(2 * image.length() / 3 , image.length());
//		cv.put("image1", image1);
//		cv.put("image2", image2);
//		cv.put("image3", image3);
		long id = writer.insert("images", null, cv);
		if (id == -1)
		{
			//Util.CreateBreakPoint("Не влезло");
		}
		writer.close();
		db.close();
	}
	
	public static Bitmap RotateImage(Bitmap pSource, float pAngle) 
	{
	    Bitmap result;

	    Matrix matrix = new Matrix();
	    matrix.postRotate(pAngle);
	    result = Bitmap.createBitmap(pSource, 0, 0, pSource.getWidth(), pSource.getHeight(), matrix, true);

	    return result;
	}
	
	public static Bitmap ResizeImageWidth(Bitmap pSource, int pWidth)
	{
		if (pSource.getWidth() > pWidth)
		{
        	int width = pSource.getWidth();
        	int height = pSource.getHeight();
        	if (width > height)
        	{
            	height = (int) (pWidth * (float) height / (float) width);
            	width = pWidth;
        	}
        	else
        	{
            	width = (int) (pWidth * (float) width / (float) height);
            	height = pWidth;
        	}
        	pSource = Bitmap.createScaledBitmap(pSource, width, height, true);
		}
		return pSource;
	}
	public static Bitmap ResizeImageHeight(Bitmap pSource, int pHeight)
	{
		if (pSource.getHeight() > pHeight)
		{
        	int width = pSource.getWidth();
        	int height = pSource.getHeight();
        	if (width > height)
        	{
            	height = (int) (pHeight * (float) height / (float) width);
            	width = pHeight;
        	}
        	else
        	{
            	width = (int) (pHeight * (float) width / (float) height);
            	height = pHeight;
        	}
        	pSource = Bitmap.createScaledBitmap(pSource, width, height, true);
		}
		return pSource;
	}
	
	public static Bitmap GetCircle(Bitmap pSource, int pWidth) 
	{
		Bitmap temp = null;
		if (pWidth == pSource.getWidth())
		{
			temp = pSource;
		}
		else
		{
		    int width = pSource.getWidth();
		    int height = pSource.getHeight();
		    float scaleWidth = ((float) pWidth) / width;
		    float scaleHeight = ((float) pWidth) / height;
		    Matrix matrix = new Matrix();
		    matrix.postScale(scaleWidth, scaleHeight);
		    temp = Bitmap.createBitmap(pSource, 0, 0, width, height, matrix, false);
		}
		Bitmap circleImage = Bitmap.createBitmap(temp.getWidth(), temp.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(circleImage);
		int color = Color.RED;
		Paint paint = new Paint();
		Rect rect = new Rect(0, 0, temp.getWidth(), temp.getHeight());
		RectF rectF = new RectF(rect);
		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawOval(rectF, paint);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		canvas.drawBitmap(temp, rect, rect, paint);
		return circleImage;
	}

	public static String GetBase64(Bitmap pImage)
	{
		try
		{
	    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    	pImage.compress(Bitmap.CompressFormat.PNG, 100, baos);
			return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
		}
		catch (Exception e)
		{
			return "";
		}
	}
	
	public static Bitmap ResizeImage(Bitmap pSource)
	{
        if (pSource.getWidth() > ImageUtil.MAX_SIZE || pSource.getHeight() > ImageUtil.MAX_SIZE)
        {
        	int width = pSource.getWidth();
        	int height = pSource.getHeight();
        	if (width > height)
        	{
            	height = (int) (ImageUtil.MAX_SIZE * (float) height / (float) width);
            	width = ImageUtil.MAX_SIZE;
        	}
        	else
        	{
            	width = (int) (ImageUtil.MAX_SIZE * (float) width / (float) height);
            	height = ImageUtil.MAX_SIZE;
        	}
        	pSource = Bitmap.createScaledBitmap(pSource, width, height, true);
        }
		return pSource;
	}
}