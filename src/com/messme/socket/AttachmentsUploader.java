package com.messme.socket;

import java.io.File;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;

import org.apache.http.NoHttpResponseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.messme.ActivityMain;
import com.messme.chats.messages.AbstractMessage;
import com.messme.chats.messages.Attachment;
import com.messme.data.DB;
import com.messme.profile.Profile;

import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;


public class AttachmentsUploader extends AsyncTask<Void, Integer, Integer>
{
	class CountingOutputStream extends FilterOutputStream 
	{
	    CountingOutputStream(OutputStream pOutputStream) 
	    {
	        super(pOutputStream);
	    }

	    @Override
	    public void write(int pByte) throws IOException 
	    {
	        out.write(pByte);
	        _AddWritedBytes(1);
	    }
	    @Override
	    public void write(byte[] pBytes) throws IOException 
	    {
	        out.write(pBytes);
	        _AddWritedBytes(pBytes.length);
	    }
	    @Override
	    public void write(byte[] pBytes, int pOffset, int pLength) throws IOException 
	    {
	        out.write(pBytes, pOffset, pLength);
	        _AddWritedBytes(pLength);
	    }
	}
	
	
	private final ActivityMain _hActivity;
	private final AbstractMessage _hMessage;
	private long _FileSize = 0;
	private volatile long _BytesWritten = 0;
	private int _OldProcent = -10;
	
	
	public AttachmentsUploader(ActivityMain pActivity, AbstractMessage pMessage)
	{
		_hActivity = pActivity;
		_hMessage = pMessage;
	}
	
	
	@Override
	protected void onPreExecute()
	{
		super.onPreExecute();
		publishProgress(0);
	}
	@Override
	protected void onProgressUpdate(Integer... pValues)
	{
		super.onProgressUpdate(pValues);
		
		_hMessage.SetProgress(pValues[0]);
	}
	@SuppressWarnings("deprecation")
	@Override
	protected Integer doInBackground(Void... params)
	{
		try 
		{
			HttpClient httpClient = new DefaultHttpClient();
			
	        HttpProtocolParams.setUseExpectContinue(httpClient.getParams(), false);
	        HttpRequestRetryHandler retryHandler = new HttpRequestRetryHandler() 
	        {
	            public boolean retryRequest(IOException pException, int pExecutionCount, HttpContext pContext) 
	            {
	                if(pExecutionCount >= 5)
	                {
	                    return false;
	                }
	                if(pException instanceof NoHttpResponseException)
	                {
	                	_BytesWritten = 0;
	                    return true;
	                } 
	                else if (pException instanceof ClientProtocolException)
	                {
	                	_BytesWritten = 0;
	                    return true;
	                } 
	                return false;
	            }
	        };
	        ((AbstractHttpClient) httpClient).setHttpRequestRetryHandler(retryHandler);
			
            HttpPost httpPost = new HttpPost("https://files.messme.me:8102/message/");
            
            JSONArray headerJSON = new JSONArray();
            for (int i = 0; i < _hMessage.GetAttachmentsCount(); i++)
            {
            	Attachment attachment = _hMessage.GetAttachment(i);
            	JSONObject item = new JSONObject();
            	item.put("id", attachment.GetID());
            	item.put("type", attachment.GetType());
            	item.put("title", URLEncoder.encode(attachment.GetTitle()));
            	item.put("description", URLEncoder.encode(attachment.GetDescription()));
            	
            	headerJSON.put(item);
            }
            httpPost.addHeader("User-Data", headerJSON.toString());
            Profile profile = new Profile(_hActivity);
            httpPost.addHeader("id", Long.toString(profile.GetID()));
            httpPost.addHeader("uuid", profile.GetUUID());
            
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();        
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            for (int i = 0; i < _hMessage.GetAttachmentsCount(); i++)
            {
            	Attachment attachment = _hMessage.GetAttachment(i);
            	_FileSize += attachment.GetFileSize();
            	if (attachment.GetRealFilePath().length() > 0)
            	{
            		File file = new File(attachment.GetRealFilePath());
            		if (file.exists())
            		{
                        FileBody fileBody = new FileBody(file); 
                        builder.addPart(attachment.GetFileName(), fileBody); 
            		}
            		else
            		{
            			throw new Exception();
            		}
            	}
            	else
            	{
            		File file = new File(attachment.GetFilePath(_hActivity));
            		if (file.exists())
            		{
                        FileBody fileBody = new FileBody(file); 
                        builder.addPart(attachment.GetFileName(), fileBody); 
            		}
            		else
            		{
            			throw new Exception();
            		}
            	}
            }
            
            HttpEntityWrapper httpEntityWrapper = new HttpEntityWrapper(builder.build())
            {
            	@Override
                public void writeTo(final OutputStream outstream) throws IOException 
                {
            		super.writeTo(new CountingOutputStream(outstream));
                }
            };
            
            httpPost.setEntity(httpEntityWrapper);
            httpClient.execute(httpPost);
            
            return 0;
        } 
		catch (Exception e) 
		{
            return 1;
        }
	}
	@Override
	protected void onPostExecute(Integer pResult)
	{
		super.onPostExecute(pResult);
		
		if (pResult == 0)
		{
			_hMessage.NotSended();
			
			try
			{
				_hMessage.Sending(_hActivity);
			}
			catch (JSONException e)
			{
			}
		}
		else
		{
			DB db = new DB(_hActivity);
			SQLiteDatabase writer = db.getWritableDatabase();
			_hMessage.NotUploaded(writer);
			writer.close();
			db.close();
		}
	}
	
	private void _AddWritedBytes(int pCount)
	{
		_BytesWritten += pCount;

		float procent = 100f * (float)_BytesWritten / (float)_FileSize;
		if (procent <= 0f)
		{
			procent = 0f;
		}
		else if (procent >= 100f)
		{
			procent = 100f;
		}
		
		if (procent - _OldProcent >= 10)
		{
			_OldProcent = (int) procent;
			this.publishProgress(_OldProcent);
		}
	}
}