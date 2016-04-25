package com.messme.chats.messages;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import com.messme.ActivityMain;
import com.messme.R;
import com.messme.profile.Profile;
import com.messme.socket.iImageContainer;
import com.messme.util.FileUtil;
import com.messme.util.ImageUtil;
import com.messme.util.Util;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.MediaStore.Video.Thumbnails;
import android.webkit.MimeTypeMap;
import ezvcard.Ezvcard;
import ezvcard.VCard;


public class Attachment implements iImageContainer
{
	public final static int TYPE_FILE		= 0;
	public final static int TYPE_IMAGE		= 1;
	public final static int TYPE_VIDEO		= 2;
	public final static int TYPE_AUDIO		= 3;
	public final static int TYPE_VOICE		= 4;
	public final static int TYPE_CONTACT	= 10;
	public final static int TYPE_PLACE		= 11;
	
	
	public final static int STATUS_INITIAL			= 10;
	public final static int STATUS_DOWNLOADING		= 11;
	public final static int STATUS_DOWNLOADED		= 12;
	public final static int STATUS_ERROR			= 13;
	public final static int STATUS_STOPTED			= 14;
	
	
	private final String	_ID;
	private int				_Type;
	private String 			_Description;
	private String 			_FileName;
	private String			_Title;
	private String 			_ContentType;
	private long 			_Filesize;
	private String 			_RealFilePath;

	
    private int _LoadStatus;
    private int _Progress = 0;

    
    private Bitmap _Image = null;
    private boolean _ImageLoaded = false;
    
    private int _AudioLength = 0;
    
    private double _PlaceLat = 0.0d;
    private double _PlaceLng = 0.0d;
    
    private String _ContactName = "";
    private byte[] _ContactImage = null;
    
    private AbstractMessage _hMessage = null;
    @SuppressWarnings("rawtypes")
	private AsyncTask _Downloader = null;
    
	public Attachment(JSONObject pAttach, AbstractMessage pMessage) throws JSONException
	{
		_LoadStatus = STATUS_INITIAL;
		_hMessage = pMessage;
		
		_ID				= pAttach.getString("id");
		_Description	= pAttach.getString("description");
		_Title			= pAttach.getString("title").equals("") ? pAttach.getString("filename") : pAttach.getString("title");
		_Filesize		= pAttach.getLong("filesize");
		_Type			= pAttach.getInt("type");
		try
		{
			String filename = pAttach.getString("filename");
			_ContentType	= filename.substring(filename.lastIndexOf(".") + 1);
			_FileName		= _ID + "." +_ContentType;
		}
		catch (IndexOutOfBoundsException e) 
		{
			_ContentType	= "";
			_FileName		= _ID;
		}
		_RealFilePath = "";
	}
	public Attachment(Cursor pCursor)
	{
		_LoadStatus = STATUS_INITIAL;
		
		_ID				= pCursor.getString(0);
		_Type			= pCursor.getInt(2);
		_Title			= pCursor.getString(3);
		_Description	= pCursor.getString(4);
		_FileName		= pCursor.getString(5);
		_ContentType	= pCursor.getString(6);
		_Filesize		= pCursor.getLong(7);
		_RealFilePath	= pCursor.getString(8);
	}
	public Attachment(Context pContext, String pDescription, Uri pUri, int pType) throws IOException
	{
		_LoadStatus = STATUS_INITIAL;

		_Type = pType;
		
		switch (_Type)
		{
			case TYPE_IMAGE:
			case TYPE_AUDIO:
			case TYPE_VIDEO:
				Cursor cursor = null;
				int column_index = 0;
				switch (_Type)
				{
					case TYPE_IMAGE:
						cursor = pContext.getContentResolver().query(pUri, new String[]{MediaStore.Images.Media.DATA}, null, null, null);
					    column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
						break;
					case TYPE_AUDIO:
						cursor = pContext.getContentResolver().query(pUri, new String[]{MediaStore.Audio.Media.DATA}, null, null, null);
					    column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
						break;
					case TYPE_VIDEO:
						cursor = pContext.getContentResolver().query(pUri, new String[]{MediaStore.Video.Media.DATA}, null, null, null);
					    column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
						break;
				}
			    cursor.moveToFirst();
			    _RealFilePath = cursor.getString(column_index);
				cursor.close();
				break;
			case TYPE_FILE:
				_RealFilePath = pUri.getPath();
				break;
			default:
				throw new IOException();
		}
	    
		_Description	= pDescription;
		_ID = Util.GenerateMID();
		File file = new File(_RealFilePath);
		_Filesize = file.length();
		_Title = _RealFilePath.substring(_RealFilePath.lastIndexOf("/") + 1);
		try
		{
			_ContentType	= _RealFilePath.substring(_RealFilePath.lastIndexOf(".") + 1);
			_FileName		= _ID + "." + _ContentType;
		}
		catch(IndexOutOfBoundsException e)
		{
			_ContentType	= "";
			_FileName		= _ID;
		}
		
		//Util.CopyFileToAttachment(file, GetFullFileName(pContext));
	}
	public Attachment(ActivityMain pActivity, String pDescription, Uri pContactUri) throws IOException
	{
		_LoadStatus = STATUS_INITIAL;
		
		_Type 			= TYPE_CONTACT;
		_Description	= pDescription;
		_ID 			= Util.GenerateMID();
		_ContentType	= "vcf";
		_Title 			= "Contact.vcf";
		_FileName		= _ID + "." + _ContentType;
		_RealFilePath	= "";
		
		@SuppressWarnings("deprecation")
		Cursor cursor = pActivity.managedQuery(pContactUri, null, null, null, null);
        cursor.moveToFirst();
        String lookupKey = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_VCARD_URI, lookupKey);
         AssetFileDescriptor fd = pActivity.getContentResolver().openAssetFileDescriptor(uri, "r");
        FileInputStream fis = fd.createInputStream();
        byte[] buf = new byte[(int) fd.getDeclaredLength()];
        fis.read(buf);
        String vCardString = new String(buf);
        
        File file = FileUtil.CreateFile(GetFilePath(pActivity), vCardString);
		_Filesize = file.length();
	}
	public Attachment(ActivityMain pActivity, String pDescription, String pAttachID, int pType) throws IOException
	{
		_LoadStatus = STATUS_INITIAL;
		
		_Type = pType;
		_Description = pDescription;
		_ID = pAttachID;
		
		switch (_Type)
		{
			case TYPE_IMAGE:
				_Title = "FromAndroidCamera.jpg";
				_ContentType = "jpg";
				break;
//			case TYPE_VIDEO:
//				_Title = "FromAndroidCamera.mp4";
//				_ContentType = "mp4";
//				break;
			case TYPE_VOICE:
				_Title = "Voice.mp4";
				_ContentType = "mp4";
				break;
			default:
				throw new IOException();
		}

		_FileName = _ID + "." + _ContentType;
		_Filesize = new File(GetFilePath(pActivity)).length();
		_RealFilePath = "";
		
		if (_Type == TYPE_IMAGE)
		{
			ExifInterface ei = new ExifInterface(GetFilePath(pActivity));
			Bitmap image = BitmapFactory.decodeFile(GetFilePath(pActivity));
			switch(ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)) 
			{
			    case ExifInterface.ORIENTATION_ROTATE_90:
			    	image = ImageUtil.RotateImage(image, 90);
			        break;
			    case ExifInterface.ORIENTATION_ROTATE_180:
			    	image = ImageUtil.RotateImage(image, 180);
			        break;
			    case ExifInterface.ORIENTATION_ROTATE_270:
			    	image = ImageUtil.RotateImage(image, 270);
			        break;
			}
			FileOutputStream fos = new FileOutputStream(new File(GetFilePath(pActivity)));
	        image.compress(Bitmap.CompressFormat.JPEG, 100, fos);
	        fos.close();
		}
	}	
	public Attachment(ActivityMain pActivity, String pDescription, double pLat, double pLng) throws JSONException, IOException
	{
		_LoadStatus = STATUS_INITIAL;
		
		_Type = TYPE_PLACE;
		_Description = pDescription;
		_ID = Util.GenerateMID();
		_Title = "Place.json";
		_ContentType = "json";
		_FileName = _ID + "." + _ContentType;
		_RealFilePath = "";
		
		JSONObject place = new JSONObject();
		place.put("lat", pLat);
		place.put("lng", pLng);
		place.put("description", "");
		
		File file = FileUtil.CreateFile(GetFilePath(pActivity), place.toString());
		
		_Filesize = file.length();
	}
	
	
	public void SetMessage(AbstractMessage pMessage)
	{
		_hMessage = pMessage;
	}
	
	
	public int GetType()
	{
		return _Type;
	}
	public String GetID()
	{
		return _ID;
	}
	public String GetFileName()
	{
		return _FileName;
	}
	public String GetTitle()
	{
		return _Title;
	}
	public String GetDescription()
	{
		return _Description;
	}
	public long GetFileSize()
	{
		return _Filesize;
	}
	public String GetFileSizeText()
	{
		long bytes = _Filesize;
		
		if (bytes < 1024)
		{
			return Long.toString(bytes) + "b";
		}
		bytes = bytes / 1024;
		if (bytes < 1024)
		{
			return Long.toString(bytes) + "Kb";
		}
		bytes = bytes / 1024;
		
		return Long.toString(bytes) + "Mb";
	}
	public String GetFilePath(ActivityMain pActivity)
	{
		return GetFilePath(pActivity.GetProfile());
	}
	public String GetFilePath(Profile pProfile)
	{
		return pProfile.GetFolderAttachment().getPath() + "/" + _FileName;
	}
	public int GetLoadStatus()
	{
		return _LoadStatus;
	}
	public String GetContactName()
	{
		return _ContactName;
	}
	public String GetProgress()
	{
		return Integer.toString(_Progress) + "%";
	}
	public int GetAudioLength()
	{
		return _AudioLength;
	}
	public String GetRealFilePath()
	{
		return _RealFilePath;
	}


	public void StopDownloading()
	{
		if (_Downloader != null)
		{
			_Downloader.cancel(false);
		}
	}
	public void Downloading(ActivityMain pActivity, final boolean pUserCommand)
	{
		_LoadStatus = STATUS_DOWNLOADING;	
		_Progress = 0;
		_Downloader = new AsyncTask<ActivityMain, Integer, Integer>()
		{
			@Override
			protected void onPreExecute()
			{
				super.onPreExecute();
				publishProgress(0);
			}
			@Override
			protected Integer doInBackground(ActivityMain... pParams)
			{
				File file = null;
    			if (_RealFilePath.length() > 0 && new File(_RealFilePath).exists())
    			{
					file = new File(_RealFilePath);
    			}
				else
				{
					file = new File(GetFilePath(pParams[0]));
				} 
			    if (!file.exists()) 
			    {
			    	if (!pUserCommand)
			    	{
			    		int conType = Util.GetConnectionType(pParams[0]);
			    		if (conType == 0)
			    		{
			    			return 0;
			    		}
			    		switch (_Type)
						{
							case TYPE_IMAGE:
								switch (conType)
								{
									case 1:
										if (!pParams[0].GetProfile().IsWPhoto())
										{
											return 2;
										}
										break;
									case 2:
										if (!pParams[0].GetProfile().IsMPhoto())
										{
											return 2;
										}
										break;
									case 3:
										if (!pParams[0].GetProfile().IsRPhoto())
										{
											return 2;
										}
										break;
								}
								break;
								
							case TYPE_AUDIO:
							case TYPE_VOICE:
								switch (conType)
								{
									case 1:
										if (!pParams[0].GetProfile().IsWAudio())
										{
											return 2;
										}
										break;
									case 2:
										if (!pParams[0].GetProfile().IsMAudio())
										{
											return 2;
										}
										break;
									case 3:
										if (!pParams[0].GetProfile().IsRAudio())
										{
											return 2;
										}
										break;
								}
								break;
								
							case TYPE_VIDEO:
								switch (conType)
								{
									case 1:
										if (!pParams[0].GetProfile().IsWVideo())
										{
											return 2;
										}
										break;
									case 2:
										if (!pParams[0].GetProfile().IsMVideo())
										{
											return 2;
										}
										break;
									case 3:
										if (!pParams[0].GetProfile().IsRVideo())
										{
											return 2;
										}
										break;
								}
								break;
								
							case TYPE_FILE:
								switch (conType)
								{
									case 1:
										if (!pParams[0].GetProfile().IsWFiles())
										{
											return 2;
										}
										break;
									case 2:
										if (!pParams[0].GetProfile().IsMFiles())
										{
											return 2;
										}
										break;
									case 3:
										if (!pParams[0].GetProfile().IsRFiles())
										{
											return 2;
										}
										break;
								}
								break;
						}
			    	}
			    	
			    	InputStream input = null;
			        OutputStream output = null;
			        HttpURLConnection connection = null;
			        try 
			        {
			            URL url = new URL("https://files.messme.me:8102/message/" + _ID);
			            connection = (HttpURLConnection) url.openConnection();
			            connection.connect();
			
			            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) 
			            {
							return 0;
			            }
			
			            int fileLength = connection.getContentLength();
			
			            input = connection.getInputStream();
			            output = new FileOutputStream(GetFilePath(pParams[0]));
			
			            byte data[] = new byte[4096];
			            long total = 0;
			            int count;
			            int oldProcent = -10;
			            boolean cancelled = false;
			            while ((count = input.read(data)) != -1) 
			            {
			                // allow canceling with back button
			                if (isCancelled()) 
			                {
			                	cancelled = true;
					        	break;
			                }
			                total += count;
			        		float procent = 100f * (float)total / (float)fileLength;
			        		if (procent <= 0f)
			        		{
			        			procent = 0f;
			        		}
			        		else if (procent >= 100f)
			        		{
			        			procent = 100f;
			        		}
			        		if (procent - oldProcent >= 10)
			        		{
			        			oldProcent = (int) procent;
			        			publishProgress(oldProcent);
			        		}
			                output.write(data, 0, count);
			            }
			            
			            try 
			            {
			                if (output != null)
			                {
			                    output.close();
			                }
			                if (input != null)
			                {
			                    input.close();
			                }
			            } 
			            catch (IOException e) 
			            {
			            }
			            
			            if (connection != null)
			            {
			                connection.disconnect();
			            }

			            if (cancelled)
			            {
				        	return 0;
			            }
			        }
			        catch (Exception e) 
			    	{
			        	return 0;
			        }
			    } // if (!file.exists()) 
	            
	            if (_Type == TYPE_PLACE)
	            {
	            	// только из папки с аттачментами
	            	if (_PlaceLat < 0.0001 && _PlaceLng < 0.0001)
	            	{
			            try 
			            {
							String result = FileUtil.ReadFile(GetFilePath(pParams[0]));
							JSONObject json = new JSONObject(result);
							_PlaceLat = json.getDouble("lat");
							_PlaceLng = json.getDouble("lng");
			            }
						catch (Exception e)
						{
							_PlaceLat = 0;
							_PlaceLng = 0;
						}
	            	}
	            }
	            else if (_Type == TYPE_VOICE)
	            {
	            	// только из папки с аттачментами
		    		try
		    		{
		    			_AudioLength = MediaPlayer.create(pParams[0], Uri.parse(GetFilePath(pParams[0]))).getDuration();
		    	    }
		    		catch(Exception e)
		    		{
		    			_AudioLength = 0;
		    		}
	            }
	            else if (_Type == TYPE_CONTACT)
	            {
	            	// только из папки с аттачментами
					if (_ContactName.length() == 0)
					{
			    		try
			    		{
			    	        VCard vcard = Ezvcard.parse(new File(GetFilePath(pParams[0]))).first();
		    	        	_ContactName = vcard.getFormattedName().getValue();
		    	    		if (vcard.getPhotos() == null || vcard.getPhotos().size() == 0)
		    	    		{
		    	    			_ContactImage = null;
		    	    		}
		    	    		else
		    	    		{
		    	    			_ContactImage = vcard.getPhotos().get(0).getData();
		    	    		}
			    	    }
			    		catch(Exception e)
			    		{
			    			_ContactName = "";
			    			_ContactImage = null;
			    		}
					}
	            }
	            else if (_Type == TYPE_AUDIO)
	            {
		    		try
		    		{
		    			if (_RealFilePath.length() > 0 && new File(_RealFilePath).exists())
		    			{
			    			_AudioLength = MediaPlayer.create(pParams[0], Uri.parse(_RealFilePath)).getDuration();
		    			}
		    			else
		    			{
			    			_AudioLength = MediaPlayer.create(pParams[0], Uri.parse(GetFilePath(pParams[0]))).getDuration();
		    			}
		    	    }
		    		catch(Exception e)
		    		{
		    			_AudioLength = 0;
		    		}
	            }
	            
	        	return 1;
			}
			@Override
			protected void onPostExecute(Integer pResult)
			{
				_Image = null;
				super.onPostExecute(pResult);
				if (pResult == 1)
				{
					_LoadStatus = STATUS_DOWNLOADED;
				}
				else if (pResult == 0)
				{
					_LoadStatus = STATUS_ERROR;	
				}
				else 
				{
					_LoadStatus = STATUS_STOPTED;	
				}
				_hMessage.RefreshView();
			}
			@Override
			protected void onProgressUpdate(Integer... values)
			{
				super.onProgressUpdate(values);
				_Progress = values[0];
				_hMessage.RefreshView();
			}
		}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, pActivity);
	}
	
	public ContentValues GetCV(String pMID)
	{	   
		ContentValues cv = new ContentValues();
		cv.put("id", _ID);
	    cv.put("mid", pMID);
	    cv.put("type", _Type);
	    cv.put("title", _Title);
	    cv.put("description", _Description);
	    cv.put("filename", _FileName);
	    cv.put("contenttype", _ContentType);
	    cv.put("filesize", _Filesize);
	    cv.put("realpath", _RealFilePath);
	    return cv;
	}
	
	public boolean Open(ActivityMain pActivity, boolean pCheck)
	{
		if (pCheck && _LoadStatus != STATUS_DOWNLOADED)
		{
			return false;
		}
		switch (_Type)
		{
			case TYPE_IMAGE:
				Intent intentImage = new Intent();
				intentImage.setAction(Intent.ACTION_VIEW);
    			if (_RealFilePath.length() > 0 && new File(_RealFilePath).exists())
    			{
    				intentImage.setDataAndType(Uri.parse("file://sdcard/" + _RealFilePath), "image/*");
    			}
    			else
    			{
    				intentImage.setDataAndType(Uri.parse("file://sdcard/" + GetFilePath(pActivity)), "image/*");
    			}
				pActivity.startActivity(intentImage);
				return true;
			case TYPE_VIDEO:
				Intent intentVideo = new Intent();
				intentVideo.setAction(Intent.ACTION_VIEW);
    			if (_RealFilePath.length() > 0 && new File(_RealFilePath).exists())
    			{
    				intentVideo.setDataAndType(Uri.parse("file://sdcard/" + _RealFilePath), "video/*");
    			}
    			else
    			{
    				intentVideo.setDataAndType(Uri.parse("file://sdcard/" + GetFilePath(pActivity)), "video/*");
    			}
				pActivity.startActivity(intentVideo);
				return true;
			case TYPE_PLACE:
				Util.GoToMap(pActivity, _PlaceLat, _PlaceLng);
				return true;
			case TYPE_FILE:
				File file = null;
    			if (_RealFilePath.length() > 0 && new File(_RealFilePath).exists())
    			{
    				file = new File(_RealFilePath);
    			}
    			else
    			{
    				file = new File(GetFilePath(pActivity));
    			}
			    MimeTypeMap map = MimeTypeMap.getSingleton();
			    String ext = MimeTypeMap.getFileExtensionFromUrl(file.getName());
			    String type = map.getMimeTypeFromExtension(ext);

			    if (type == null)
			    {
			        type = "*/*";
			    }

			    Intent intentFile = new Intent(Intent.ACTION_VIEW);
			    Uri data = Uri.fromFile(file);
			    intentFile.setDataAndType(data, type);
			    pActivity.startActivity(intentFile);
				return true;
			case TYPE_CONTACT:
				pActivity.GetManager().GoToContact(GetFilePath(pActivity));
				return true;
			case TYPE_AUDIO:
			case TYPE_VOICE:
				Intent intentAudio = new Intent();
				intentAudio.setAction(Intent.ACTION_VIEW);
				File fileAudio = null;  
    			if (_RealFilePath.length() > 0 && new File(_RealFilePath).exists())
    			{
    				fileAudio = new File(_RealFilePath);
    			}
    			else
    			{
    				fileAudio = new File(GetFilePath(pActivity));
    			}
    			intentAudio.setDataAndType(Uri.fromFile(fileAudio), "audio/*"); 
				pActivity.startActivity(intentAudio);
				return true;
			default:
				return false;
		}
	}

	@Override
	public void LoadImage(Context pContext, Profile pProfile)
	{	
		switch (_Type)
		{
			case TYPE_FILE:
				_Image = BitmapFactory.decodeResource(pContext.getResources(), R.drawable.ic_attach_file_preview);
				break;
			case TYPE_IMAGE:
				_Image = ImageUtil.LoadImageFromDB(pContext, _ID + "_small");
				if (_Image == null)
				{
					try 
					{			
						_Image = BitmapFactory.decodeStream((InputStream)new URL("https://files.messme.me:8102/message/" + _ID + "_small").getContent());
						//_Image = ImageUtil.ResizeImageWidth(_Image, 300);
						ImageUtil.SaveImageToDB(pContext, _Image, _ID + "_small");
					} 
					catch(Exception e) 
					{
						_Image = null;
						if (_RealFilePath.length() > 0)
						{
							_Image = BitmapFactory.decodeFile(_RealFilePath);
						}
						else
						{
							_Image = BitmapFactory.decodeFile(GetFilePath(pProfile));
						}
						if (_Image != null)
						{
							_Image = ImageUtil.ResizeImageWidth(_Image, 500);
							ImageUtil.SaveImageToDB(pContext, _Image, _ID + "_small");
						}
					}
				}
				break;
			case TYPE_VIDEO:
				_Image = ImageUtil.LoadImageFromDB(pContext, _ID + "_small");
				if (_Image == null)
				{
					try 
					{			
						_Image = BitmapFactory.decodeStream((InputStream)new URL("https://files.messme.me:8102/message/" + _ID + "_small").getContent());
						//_Image = ImageUtil.ResizeImageWidth(_Image, 300);
						ImageUtil.SaveImageToDB(pContext, _Image, _ID + "_small");
					} 
					catch(Exception e) 
					{
						_Image = null;
						if (_RealFilePath.length() > 0)
						{
							_Image = ThumbnailUtils.createVideoThumbnail(_RealFilePath, Thumbnails.MINI_KIND);
						}
						else
						{
							_Image = ThumbnailUtils.createVideoThumbnail(GetFilePath(pProfile), Thumbnails.MINI_KIND);
						}
//						if (_Image != null)
//						{
//////							_Image = ImageUtil.ResizeImageWidth(_Image, 300);
//////							_Image = ImageUtil.ResizeImageHeight(_Image, 300);
////							//Util.SaveImageToDB(pParams[0], bitmap, id);
//						}
					}
				}
				break;
			case TYPE_AUDIO:
				_Image = BitmapFactory.decodeResource(pContext.getResources(), R.drawable.ic_attach_audio_preview);
				break;
			case TYPE_VOICE:
				_Image = BitmapFactory.decodeResource(pContext.getResources(), R.drawable.ic_attach_voice_preview);
				break;
			case TYPE_CONTACT:
				_Image = ImageUtil.LoadImageFromDB(pContext, _ID);
				if (_Image == null)
				{			
					if (_ContactName.length() == 0)
					{
			    		try
			    		{
			    	        VCard vcard = Ezvcard.parse(new File(GetFilePath(pProfile))).first();
		    	        	_ContactName = vcard.getFormattedName().getValue();
		    	    		if (vcard.getPhotos() == null || vcard.getPhotos().size() == 0)
		    	    		{
		    	    			_ContactImage = null;
		    	    		}
		    	    		else
		    	    		{
		    	    			_ContactImage = vcard.getPhotos().get(0).getData();
		    	    		}
			    	    }
			    		catch(Exception e)
			    		{
			    			_ContactName = "";
			    			_ContactImage = null;
			    		}
					}
					if (_ContactImage == null)
					{
						_Image = BitmapFactory.decodeResource(pContext.getResources(), R.drawable.ic_attach_contact_preview);
					}
					else
					{
						_Image = BitmapFactory.decodeByteArray(_ContactImage, 0, _ContactImage.length);
						ImageUtil.SaveImageToDB(pContext, _Image, _ID);
					}
				}
				break;
			case TYPE_PLACE:				
				_Image = ImageUtil.LoadImageFromDB(pContext, _ID);
				if (_Image == null)
				{
	            	if (_PlaceLat < 0.0001 && _PlaceLng < 0.0001)
	            	{
			            try 
			            {
							String result = FileUtil.ReadFile(GetFilePath(pProfile));
							JSONObject json = new JSONObject(result);
							_PlaceLat = json.getDouble("lat");
							_PlaceLng = json.getDouble("lng");
			            }
						catch (Exception e)
						{
							_PlaceLat = 0;
							_PlaceLng = 0;
						}
	            	}
					try 
					{									
						_Image = BitmapFactory.decodeStream((InputStream)new URL("http://maps.google.com/maps/api/staticmap?center=" + _PlaceLat + "," + _PlaceLng + "&zoom=17&size=600x400&language=" + pProfile.GetLocale() + "&sensor=false&markers=color:red%7C" + _PlaceLat + "," + _PlaceLng).getContent());
						ImageUtil.SaveImageToDB(pContext, _Image, _ID);
					} 
					catch(Exception e) 
					{
						_Image = null;
					}
				}
				break;
		}
		
		if (_Image == null)
		{
			_ImageLoaded = false;
			_Image = BitmapFactory.decodeResource(pContext.getResources(), R.drawable.ic_error);
		}
		else
		{
			_ImageLoaded = true;
		}
//        if (_Image.getWidth() > 300)
//        {
//        	int width = 300;
//        	int height = (int) (width * (float) _Image.getHeight() / (float) _Image.getWidth());
//        	_Image = Bitmap.createScaledBitmap(_Image, width, height, true);
//        }
        
        try
        {
        	((ActivityMain) pContext).runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					if (_hMessage != null)
					{
				        _hMessage.RefreshView();
					}
				}
			});
        }
        catch (Exception e)
        {
        }
	}
	@Override
	public Bitmap GetImage()
	{
		return _Image;
	}
	@Override
	public boolean HasLoadedImage()
	{
		return _ImageLoaded && _Image != null;
	}
}