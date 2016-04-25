package com.messme.chats.messages;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.github.ksoichiro.android.observablescrollview.ObservableRecyclerView;
import com.messme.ActivityMain;
import com.messme.R;
import com.messme.chats.messages.chat.Chat;
import com.messme.chats.messages.delivery.Delivery;
import com.messme.chats.messages.dialog.Dialog;
import com.messme.data.DB;
import com.messme.socket.ImageLoader;
import com.messme.util.DateUtil;
import com.messme.util.Util;
import com.messme.view.MessmeFragment;
import com.messme.view.TextWatcher;
import com.messme.view.TextWatcher.iOnTextChanged;

import android.R.color;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.ContactsContract.Contacts;
import android.provider.MediaStore;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.PasswordTransformationMethod;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;


public abstract class AbstractMessageFragment<T extends AbstractMessage> extends MessmeFragment implements OnClickListener, iOnTextChanged
{
	protected final static int __LOAD_COUNT = 20;
	
	protected static final int _RESULT_CAMERA = 3000;
	protected static final int _RESULT_GALLERY = 3001;
	protected static final int _RESULT_FILE = 3002;
	protected static final int _RESULT_AUDIO = 3003;
	protected static final int _RESULT_VIDEO_CAMERA = 3005;
	protected static final int _RESULT_VIDEO = 3006;
	protected static final int _RESULT_CONTACT = 3007;
	
	private static final int _PRESSED_PLACE = 201;
	private static final int _PRESSED_TRANSLATE = 202;

	
	class RecordTimerTask extends TimerTask 
	{
		@Override
		public void run() 
		{
			_RecordSecond += 1000;
			_tvAttachVoice.post(new Runnable()
			{
				@Override
				public void run()
				{
					_tvAttachVoice.setText(DateUtil.GetMinSec(new Date(_RecordSecond)));
				}
			});
		}
	}
	class PlayTimerTask extends TimerTask 
	{
		@Override
		public void run() 
		{
			_PlaySecond += 1000;
			_tvAttachVoice.post(new Runnable()
			{
				@Override
				public void run()
				{
					_tvAttachVoice.setText(DateUtil.GetMinSec(new Date(_PlaySecond)) + _RecordTotal);
				}
			});
		}
	}
	
	
	//private LinearLayout _llTitle = null;
	
	private LinearLayout _llMenu = null;
	protected TextView __tvTitle = null;
	private ImageView _ivLock = null;;
	private ImageView _ivSearch = null;
	
	private LinearLayout _llSearch = null;
	private EditText _etSearch = null;
	private ImageView _ivUp = null;
	private ImageView _ivDown = null;
	
	private ProgressBar _pb = null;
	private LinearLayoutManager _llManager = null;
	private ObservableRecyclerView _rv = null;

	private LinearLayout _llCode = null;
	private EditText _etCode = null;

	private HorizontalScrollView _hsvAttachment = null;
	private LinearLayout _llAttachment = null;
	
	protected LinearLayout __llSend = null;
	
	private ImageView _ivAttach = null;
	private EditText _etSend = null;
	private ImageView _ivSend = null;
	
	//private RelativeLayout _rlMessages = null;
	
	private LinearLayout _llAttach = null;
	
	private LinearLayout _llAttachSettings = null;

	private LinearLayout _llAttachMain = null;
	private ImageView _ivPlace = null;
	private ImageView _ivTranslate = null;
	
	private LinearLayout _llAttachVoice = null;
	private LinearLayout _llAttachVoiceRecord = null;
	private ImageView _ivAttachVoice = null;
	private TextView _tvAttachVoice = null;
	private ImageView _ivAttachVoiceOk = null;
	
	private RelativeLayout _rlDialog = null;
	private LinearLayout _llDialogCopy = null;
	
	private RelativeLayout _rlSecret = null;
	private ImageView _ivSecret;
	private EditText _etSecret = null;
	
	
	private TextWatcher _SearchTextWatcher = null;
	private TextWatcher _SendTextWatcher = null;
	private TextWatcher _CodeTextWatcher = null;

	protected AbstractMessageAdapter<T> __Adapter = null;
	private AbstractMessageContainer<T> _hContainer = null;
	
	private boolean _ScrollLock = false;
	protected boolean __MessageOver = false;
	private boolean _ScrollOnBottom = true;
	
	private String _TempID = "";
	
	private MediaRecorder _Recorder = null;
	private Timer _RecordTimer;
	private long _RecordSecond = 0;
	private String _RecordTotal = "";
	private RecordTimerTask _RecordTimerTask;
	
	private final MediaPlayer _MediaPlayer;
	private Timer _PlayTimer;
	private long _PlaySecond = 0;
	private PlayTimerTask _PlayTimerTask;
	
	protected boolean __PlacePressed;
	protected boolean __TranslatePressed;
	
	// выделение сообщения
	private T _hSelectedMessage = null;
	private LinearLayout _hSelectedBackground = null;
	
	// пересылка сообщения
	private int _ResendMessageType = -1;
	private String _ResendMessageID;
	private String _ResendMessageContainerID;
	
	private boolean _Inited = false;
	protected boolean __Syncronizing = false;
	
	protected String __FirstMID;

	private boolean _KeyboardShowing = false;
	
	protected ArrayList<Attachment> _Attachments = new ArrayList<Attachment>();
	private final LayoutInflater _Inflater;
	protected final ImageLoader __Loader;
	
	
	public AbstractMessageFragment(ActivityMain pActivity, SparseArray<String> pStore)
	{
		super(pActivity, pStore);
		_SearchTextWatcher = new TextWatcher(this, 1);
		_SendTextWatcher = new TextWatcher(this, 2);
		_CodeTextWatcher = new TextWatcher(this, 3);
		_Inflater = (LayoutInflater) __GetMainActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		__Loader = new ImageLoader(__GetMainActivity());
		
		_MediaPlayer = new MediaPlayer();
		_MediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
		{
			@Override
			public void onCompletion(MediaPlayer pMediaPlayer)
			{
				_StopPlay();
				_tvAttachVoice.setText("00:00" + _RecordTotal);
				_ivAttachVoice.setImageResource(R.drawable.ic_voice_play);
			}
		});
	}


	public final void OnBottomAdded(boolean pUserMessage)
	{
		if (_ScrollOnBottom || pUserMessage)
		{
			_rv.postDelayed(new Runnable()
			{						
				@Override
				public void run()
				{
					if (__Adapter.getItemCount() != 0)
					{
						_rv.smoothScrollToPosition(__Adapter.getItemCount() - 1);
					}
				}
			}, 200);
		}
	}
	
	public final void CallDialog(T pMessage, LinearLayout pBackGround)
	{
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(_etSend.getWindowToken(), 0);
		imm.hideSoftInputFromWindow(_etSearch.getWindowToken(), 0);
		_rlDialog.setVisibility(View.VISIBLE);
		if (pMessage.GetMessage().length() > 0)
		{
			_llDialogCopy.setVisibility(View.VISIBLE);
		}
		else
		{
			_llDialogCopy.setVisibility(View.GONE);
		}
		_hSelectedMessage = pMessage;
		_hSelectedBackground = pBackGround;
		_hSelectedBackground.setBackgroundColor(__GetMainActivity().getResources().getColor(R.color.BackgroundBlue));
	}
	
	public final boolean IsInited()
	{
		return _Inited;
	}

	@Override
	protected void __OnSave(SparseArray<String> pStore)
	{
		pStore.put(_PRESSED_PLACE, Boolean.toString(__PlacePressed));
		pStore.put(_PRESSED_TRANSLATE, Boolean.toString(__TranslatePressed));
	}
	
	@Override
	protected void __OnDestroy()
	{
		if (_MediaPlayer.isPlaying())
		{
			_MediaPlayer.stop();
		}
		_etSearch.removeTextChangedListener(_SearchTextWatcher);
		_etSend.removeTextChangedListener(_SendTextWatcher);
		_etCode.removeTextChangedListener(_CodeTextWatcher);
		if (_etSend != null)
		{
			_hContainer.SetTypedText(_etSend.getText().toString());
		}
	}
	
	@Override
	public void onClick(View pView)
	{
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		if (pView.getId() == R.id.etMessages)
		{
			if (!_KeyboardShowing)
			{
				_llAttachSettings.setVisibility(View.INVISIBLE);
				_llAttachMain.setVisibility(View.INVISIBLE);
		        _llAttach.setVisibility(View.VISIBLE);
		        if (_etSend.requestFocus())
		        {
					imm.showSoftInput(_etSend, InputMethodManager.SHOW_IMPLICIT);
		        }
			}
		}
		else
		{
		    imm.hideSoftInputFromWindow(_etSend.getWindowToken(), 0);
		    imm.hideSoftInputFromWindow(_etSearch.getWindowToken(), 0);
			switch (pView.getId())
			{
				case R.id.ivMessagesBack:
					__GetMainActivity().GetManager().GoToBack();
					break;
				case R.id.ivMessagesSearch:
					_llMenu.setVisibility(View.GONE);
					_llSearch.setVisibility(View.VISIBLE);
					_etSearch.setVisibility(View.VISIBLE);
					_etSearch.setEnabled(true);
					if (_etSearch.requestFocus())
					{
						imm.showSoftInput(_etSearch, InputMethodManager.SHOW_IMPLICIT);
					}
					break;
					
				case R.id.ivMessagesUp:
					_rv.smoothScrollToPosition(__Adapter.GetNextFilteredMessage(true, _llManager.findFirstVisibleItemPosition()));
					break;
				case R.id.ivMessagesDown:
					_rv.smoothScrollToPosition(__Adapter.GetNextFilteredMessage(false, _llManager.findLastVisibleItemPosition()));
					break;
				case R.id.ivMessagesClose:
					_llMenu.setVisibility(View.VISIBLE);
					_llSearch.setVisibility(View.GONE);
					__Adapter.SetFilter("");
					break;
					
				case R.id.ivMessagesAttach:
					if (_KeyboardShowing)
					{
					    imm.hideSoftInputFromWindow(_etSend.getWindowToken(), 0);
					    imm.hideSoftInputFromWindow(_etSearch.getWindowToken(), 0);
					}
					else
					{
			    		if (_llAttach.getVisibility() == View.GONE)
			    		{
							_llAttachSettings.setVisibility(View.VISIBLE);
							_llAttachMain.setVisibility(View.VISIBLE);
					        _llAttach.setVisibility(View.VISIBLE);
			    		}
			    		else
			    		{
					        _llAttach.setVisibility(View.GONE);
			    		}
						_InitAttachMenu();
					}
					break;
					
				case R.id.ivMessagesSend:
					String message = _etSend.getText().toString().trim();
					if (_Attachments.size() > 0 || message.length() > 0)
					{
						try
						{			
							__SendMessage(message, false, _Attachments);
							
							_ClearAttachments();
						}
						catch (Exception e)
						{
						}
					}
					else
					{
						Intent intentCamera = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
						_TempID = Util.GenerateMID();
						intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(__GetMainActivity().GetProfile().GetFolderAttachment() + "/" + _TempID + ".jpg")));
						startActivityForResult(intentCamera, _RESULT_CAMERA);
					}
					
			        _llAttach.setVisibility(View.GONE);
			        _InitAttachMenu();
					break;
					
				case R.id.llMessagesAttachMainPhoto:
					Intent intentGallery = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
					intentGallery.setType("image/*");
					startActivityForResult(intentGallery, _RESULT_GALLERY);
					break;
				case R.id.llMessagesAttachMainVideo:
		            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_VIDEO_CAPTURE);
//					_TempID = Util.GenerateMID();
//					cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(__GetMainActivity().GetFolderAttachment() + "/" + _TempID + ".mp4")));
//		            cameraIntent.putExtra(MediaStore.Video.Media.MIME_TYPE, "video/mp4");

					Intent intentVideo = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
					intentVideo.setType("video/*");
		            
		            Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
		            chooserIntent.putExtra(Intent.EXTRA_INTENT, intentVideo);      
		            chooserIntent.putExtra(Intent.EXTRA_TITLE, getActivity().getText(R.string.ProfileChoose));
			        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Parcelable[] {cameraIntent});
		            startActivityForResult(chooserIntent, _RESULT_VIDEO);
					break;
				case R.id.llMessagesAttachMainAudio:
					Intent intentAudio = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
		            Intent chooserIntent2 = new Intent(Intent.ACTION_CHOOSER);
		            chooserIntent2.putExtra(Intent.EXTRA_TITLE, getActivity().getText(R.string.ProfileChoose));
		            chooserIntent2.putExtra(Intent.EXTRA_INTENT, intentAudio);  
		            startActivityForResult(chooserIntent2, _RESULT_AUDIO);
					break;
				case R.id.llMessagesAttachMainVoice:
					_llAttachMain.setVisibility(View.GONE);
					_llAttachVoice.setVisibility(View.VISIBLE);
					_InitVoiceMenu();
					break;
				case R.id.llMessagesAttachMainFile:
					Intent intentFile = new Intent(Intent.ACTION_GET_CONTENT);
					intentFile.setType("file/*");
				    startActivityForResult(intentFile, _RESULT_FILE);
					break;
				case R.id.llMessagesAttachMainPlace:
					__GetMainActivity().GetManager().GoToPlace();
					break;
				case R.id.llMessagesAttachMainContact:
					Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI);  
				    startActivityForResult(contactPickerIntent, _RESULT_CONTACT); 
					break;
				case R.id.llMessagesAttachMainHide:
			        _llAttach.setVisibility(View.GONE);
					break;
					
				case R.id.ivMessagesAttachVoiceBack:
					_StopRecordTimer();
					_StopPlay();
					_llAttachMain.setVisibility(View.VISIBLE);
					_llAttachVoice.setVisibility(View.GONE);
					break;
				case R.id.llMessagesAttachVoiceRecord:
					if (_TempID.length() == 0)
					{
		            	_TempID = Util.GenerateMID();
		                try 
		                {
			            	_Recorder = new MediaRecorder();
			            	_Recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			            	_Recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
			            	_Recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			            	_Recorder.setOutputFile(__GetMainActivity().GetProfile().GetFolderAttachment() + "/" + _TempID + ".mp4");
		                	_Recorder.prepare();
		                	_Recorder.start();
		                	
		                	_RecordSecond = 0;
		                	_RecordTotal = "";
		                	
		                	_RecordTimerTask = new RecordTimerTask();
		                	_RecordTimer = new Timer();
		                	_RecordTimer.schedule(_RecordTimerTask, 1000, 1000);

							_ivAttachVoice.setImageResource(R.drawable.ic_voice_record);
		                } 
		                catch (Exception e) 
		                {
		                	_InitVoiceMenu();
		                	
		            		__GetMainActivity().OpenDialog(1, __GetMainActivity().getString(R.string.Dialog35Title), __GetMainActivity().getString(R.string.Dialog35Description));
		                }
					}
					else if (_RecordTimer != null)
					{
	            		_StopRecordTimer();
						
						File file = new File(__GetMainActivity().GetProfile().GetFolderAttachment() + "/" + _TempID + ".mp4");
						if (file.exists())
						{
		            		_StopPlay();

		            		_ivAttachVoiceOk.setVisibility(View.VISIBLE);
		            		_RecordTotal = "/" + _tvAttachVoice.getText().toString();
		            		_tvAttachVoice.setText("00:00" + _RecordTotal);
							_ivAttachVoice.setImageResource(R.drawable.ic_voice_play);
						}
						else
						{
		                	_InitVoiceMenu();
		                	
		            		__GetMainActivity().OpenDialog(1, __GetMainActivity().getString(R.string.Dialog35Title), __GetMainActivity().getString(R.string.Dialog35Description));
						}
					}
					else if (_PlayTimer == null)
					{
						try
						{					
			            	_MediaPlayer.reset();
							_MediaPlayer.setDataSource(__GetMainActivity().GetProfile().GetFolderAttachment().getPath() + "/" + _TempID + ".mp4");
							_MediaPlayer.prepare();
							_MediaPlayer.start();
							
							_PlaySecond = 0;
							
		                	_PlayTimerTask = new PlayTimerTask();
		                	_PlayTimer = new Timer();
		                	_PlayTimer.schedule(_PlayTimerTask, 1000, 1000);

							_ivAttachVoice.setImageResource(R.drawable.ic_voice_pause);
						}
						catch (Exception e)
						{
		                	_InitVoiceMenu();
		                	
		            		__GetMainActivity().OpenDialog(1, __GetMainActivity().getString(R.string.Dialog35Title), __GetMainActivity().getString(R.string.Dialog35Description));
						}
					}
					else
					{
	            		_tvAttachVoice.setText("00:00" + _RecordTotal);
						_StopPlay();
						_ivAttachVoice.setImageResource(R.drawable.ic_voice_play);
					}
					break;
				case R.id.ivMessagesAttachVoiceOK:
					_llAttachMain.setVisibility(View.VISIBLE);
					_llAttachVoice.setVisibility(View.GONE);
					_StopPlay();
	            	try
	            	{
					    Attachment attachment = new Attachment(__GetMainActivity(), "", _TempID, Attachment.TYPE_VOICE);
					    _AddAttachment(attachment);
	            	}
	            	catch (Exception e)
	            	{
	            		__GetMainActivity().OpenDialog(1, __GetMainActivity().getString(R.string.Dialog35Title), __GetMainActivity().getString(R.string.Dialog35Description));
	            	}
					break;

				case R.id.ivMessagesPlace:
					if (_ivPlace.isSelected())
					{
						_ivPlace.setSelected(false);
						__PlacePressed = false;
					}
					else
					{
						if (__GetMainActivity().GetLocation() == null)
						{
							__GetMainActivity().OpenDialog(10, __GetMainActivity().getString(R.string.Dialog45Title), __GetMainActivity().getString(R.string.Dialog45Description));
						}
						else
						{
							_ivPlace.setSelected(true);
							__PlacePressed = true;
						}
					}
					break;
				case R.id.ivMessagesTranslate:
					if (_ivTranslate.isSelected())
					{
						_ivTranslate.setSelected(false);
						__TranslatePressed = false;
					}
					else
					{
						_ivTranslate.setSelected(true);
						__TranslatePressed = true;
					}
					break;

				case R.id.rlMessageDialog:
				case R.id.llMessageDialogResend:
				case R.id.llMessageDialogCopy:
				case R.id.llMessageDialogDelete:
					_rlDialog.setVisibility(View.GONE);
					_hSelectedBackground.setBackgroundColor(color.transparent);
					_hSelectedBackground = null;
					switch (pView.getId())
					{
						case R.id.llMessageDialogResend:
							__ResendMessage(_hSelectedMessage);
							break;
						case R.id.llMessageDialogCopy:
							android.content.ClipboardManager clipboard = (android.content.ClipboardManager) __GetMainActivity().getSystemService(Context.CLIPBOARD_SERVICE); 
						    android.content.ClipData clip = android.content.ClipData.newPlainText("Message Text", _hSelectedMessage.GetMessage());
						    clipboard.setPrimaryClip(clip);
							break;
						case R.id.llMessageDialogDelete:
							DB db = new DB(__GetMainActivity());
							SQLiteDatabase writer = db.getWritableDatabase();
							try
							{
								if (_hSelectedMessage.GetStatus() == AbstractMessage.STATUS_NOTUPLOADED 
										|| _hSelectedMessage.GetStatus() == AbstractMessage.STATUS_UPLOADING)
								{
									_hContainer.RemoveMessage(__GetMainActivity(), _hSelectedMessage.GetID(), writer);
								}
								else if (_hSelectedMessage.GetStatus() == AbstractMessage.STATUS_NOTSENDED)
								{
									__GetMainActivity().GetServerConnection().RemoveNotSended(_hSelectedMessage.GetID());
									_hContainer.RemoveMessage(__GetMainActivity(), _hSelectedMessage.GetID(), writer);
								}
								else //if (pMessage.GetStatus() >= AbstractMessage.STATUS_SENDED)
								{
									JSONObject options = new JSONObject();
									if (_hContainer instanceof Dialog)
									{
										options.put("userid", ((Dialog) _hContainer).GetUserID());
									} 
									else if (_hContainer instanceof Chat)
									{
										options.put("id", _hContainer.GetID());
										options.put("chat", "1");
									}
									else if (_hContainer instanceof Delivery)
									{
										options.put("id", _hContainer.GetID());
									}
									JSONArray list = new JSONArray();
									list.put(_hSelectedMessage.GetID());
									options.put("list", list);
									__SendToServer("message.delete", options);
								}
							}
							catch (JSONException e)
							{
							}
							writer.close();
							db.close();
							break;
					}
					_hSelectedMessage = null;
					break;

				case R.id.ivMessagesLock:
					if (_hContainer.GetType() == AbstractMessageContainer.SECRET)
					{
						_ivLock.setImageResource(R.drawable.ic_unlock);
						//TODO посылаем на сервак
//						try
//						{
//						}
//						catch (JSONException e)
//						{
//						}
//						_hContainer.SetType(_hContainer.GetType() == AbstractMessageContainer.SECRET ? AbstractMessageContainer.MAIN : AbstractMessageContainer.SECRET);
					}
					else
					{
						if (__GetMainActivity().GetProfile().IsSecurityPinInitiated())
						{
							_ivLock.setImageResource(R.drawable.ic_lock);
							//TODO посылаем на сервак
//							try
//							{
//							}
//							catch (JSONException e)
//							{
//							}
//							_hContainer.SetType(_hContainer.GetType() == AbstractMessageContainer.SECRET ? AbstractMessageContainer.MAIN : AbstractMessageContainer.SECRET);
						}
						else
						{
							_rlSecret.setVisibility(View.VISIBLE);
						}						
					}
					break;
				case R.id.rlMessagesSecretEmpty:
				case R.id.btnMessagesSecretEmpty:
					_rlSecret.setVisibility(View.GONE);
					if (pView.getId() == R.id.btnMessagesSecretEmpty)
					{					
						//TODO новый пин
//						try
//						{
//						}
//						catch (JSONException e)
//						{
//						}
					}
					break;
				case R.id.ivMessagesSecretEmpty:
					if (_etSecret.getTransformationMethod() instanceof PasswordTransformationMethod)
					{
						_ivSecret.setImageResource(R.drawable.ic_eye_close);
						_etSecret.setTransformationMethod(null);
					}
					else
					{
						_ivSecret.setImageResource(R.drawable.ic_eye_open);
						_etSecret.setTransformationMethod(new PasswordTransformationMethod());
					}
					break;
			}
		}
	}

	@Override
	public final void OnTextChanged(int pID, String pText)
	{
		switch (pID)
		{
			case 1:
				if (pText.length() == 0)
				{
					_ivUp.setVisibility(View.GONE);
					_ivDown.setVisibility(View.GONE);
				}
				else
				{
					_ivUp.setVisibility(View.VISIBLE);
					_ivDown.setVisibility(View.VISIBLE);
				}
				__Adapter.SetFilter(pText);
				break;
			case 2:
				if (_Attachments.size() == 0 && pText.trim().length() == 0)
				{
					_ivSend.setImageResource(R.drawable.ic_main_photo);
				}
				else
				{
					_ivSend.setImageResource(R.drawable.ic_main_send);
				}
				break;
			case 3:
				if (__GetMainActivity().GetProfile().CheckSecurityPin(pText))
				{
					InputMethodManager imm = (InputMethodManager) __GetMainActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				    imm.hideSoftInputFromWindow(_etCode.getWindowToken(), 0);
				    __Init();
				}
				break;
		}
	}
	
	@Override
	protected void __OnActivityResult(int pRequestCode, int pResultCode, Intent pData)
	{
		switch (pRequestCode)
		{
			case _RESULT_CAMERA:
				if (pResultCode == ActivityMain.RESULT_OK)
				{
					File file = new File(__GetMainActivity().GetProfile().GetFolderAttachment() + "/" + _TempID + ".jpg");
					if (file.exists())
					{
						try
						{
						    Attachment attachment = new Attachment(__GetMainActivity(), "", _TempID, Attachment.TYPE_IMAGE);
						    ArrayList<Attachment> attachments = new ArrayList<Attachment>();
						    attachments.add(attachment);
						    __SendMessage("", false, attachments);
						}
						catch (Exception e)
						{
							__GetMainActivity().OpenDialog(1, __GetMainActivity().getString(R.string.Dialog36Title), __GetMainActivity().getString(R.string.Dialog36Description));
						}
					}
					else
					{
						__GetMainActivity().OpenDialog(1, __GetMainActivity().getString(R.string.Dialog38Title), __GetMainActivity().getString(R.string.Dialog38Description));
					}
				}
				break;
			case _RESULT_VIDEO_CAMERA:
				if (pResultCode == ActivityMain.RESULT_OK)
				{
					File file = new File(__GetMainActivity().GetProfile().GetFolderAttachment() + "/" + _TempID + ".mp4");
					if (file.exists())
					{
						try
						{
						    Attachment attachment = new Attachment(__GetMainActivity(), "", _TempID, Attachment.TYPE_VIDEO);
						    _AddAttachment(attachment);
						}
						catch (Exception e)
						{
							__GetMainActivity().OpenDialog(1, __GetMainActivity().getString(R.string.Dialog36Title), __GetMainActivity().getString(R.string.Dialog36Description));
						}
					}
					else
					{
						__GetMainActivity().OpenDialog(1, __GetMainActivity().getString(R.string.Dialog37Title), __GetMainActivity().getString(R.string.Dialog37Description));
					}
				}
				break;
			case _RESULT_GALLERY:
				if (pResultCode == ActivityMain.RESULT_OK && pData != null)
				{
					try
					{
					    Attachment attachment = new Attachment(__GetMainActivity(), "", pData.getData(), Attachment.TYPE_IMAGE);
					    _AddAttachment(attachment);
					}
					catch (Exception e)
					{
						__GetMainActivity().OpenDialog(1, __GetMainActivity().getString(R.string.Dialog36Title), __GetMainActivity().getString(R.string.Dialog36Description));
					}
				}
				break;
			case _RESULT_AUDIO:
				if (pResultCode == ActivityMain.RESULT_OK && pData != null)
				{
					try
					{
					    Attachment attachment = new Attachment(__GetMainActivity(), "", pData.getData(), Attachment.TYPE_AUDIO);
					    _AddAttachment(attachment);
					}
					catch (Exception e)
					{
						__GetMainActivity().OpenDialog(1, __GetMainActivity().getString(R.string.Dialog36Title), __GetMainActivity().getString(R.string.Dialog36Description));
					}
				}
				break;
			case _RESULT_VIDEO:
				if (pResultCode == ActivityMain.RESULT_OK && pData != null)
				{
					try
					{
					    Attachment attachment = new Attachment(__GetMainActivity(), "", pData.getData(), Attachment.TYPE_VIDEO);
					    _AddAttachment(attachment);
					}
					catch (Exception e)
					{
						__GetMainActivity().OpenDialog(1, __GetMainActivity().getString(R.string.Dialog36Title), __GetMainActivity().getString(R.string.Dialog36Description));
					}
				}
				break;
			case _RESULT_FILE:
				if (pResultCode == ActivityMain.RESULT_OK && pData != null)
				{
					try
					{
					    Attachment attachment = new Attachment(__GetMainActivity(), "", pData.getData(), Attachment.TYPE_FILE);
					    _AddAttachment(attachment);
					}
					catch (Exception e)
					{
						__GetMainActivity().OpenDialog(1, __GetMainActivity().getString(R.string.Dialog36Title), __GetMainActivity().getString(R.string.Dialog36Description));
					}
				}
				break;
			case _RESULT_CONTACT:
				if (pResultCode == ActivityMain.RESULT_OK && pData != null)
				{
                    try 
                    {
					    Attachment attachment = new Attachment(__GetMainActivity(), "", pData.getData());
					    _AddAttachment(attachment);
                    } 
                    catch (Exception e) 
                    {
						__GetMainActivity().OpenDialog(1, __GetMainActivity().getString(R.string.Dialog36Title), __GetMainActivity().getString(R.string.Dialog36Description));
                    }
				}
				break;
		}
	}
	
	@Override
	protected final boolean __OnBackPressed()
	{
		if (_rlDialog.getVisibility() == View.VISIBLE)
		{
			_rlDialog.setVisibility(View.GONE);
			_hSelectedBackground.setBackgroundColor(color.transparent);
			_hSelectedBackground = null;
			_hSelectedMessage = null;
			return false;
		}
		if (_rlSecret.getVisibility() == View.VISIBLE)
		{
			_rlSecret.setVisibility(View.GONE);
			return false;
		}
		if (_llAttach.getVisibility() == View.VISIBLE)
		{
			_llAttach.setVisibility(View.GONE);
			return false;
		}
		return true;
	}
	
	@Override
	protected final void __OnConnected()
	{
		try
		{
			__Syncronizing = false;
			__SynchronizeMessages();
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}
	
	
	private void _InitAttachMenu()
	{
        _llAttachMain.setVisibility(View.VISIBLE);
        _llAttachVoice.setVisibility(View.GONE);
        _StopRecordTimer();
        _StopPlay();
	}
	private void _InitVoiceMenu()
	{
		_ivAttachVoiceOk.setVisibility(View.INVISIBLE);
		_tvAttachVoice.setText("00:00");
		_ivAttachVoice.setImageResource(R.drawable.ic_voice_init);
		_TempID = "";
        _StopRecordTimer();
        _StopPlay();
	}
	private void _StopRecordTimer()
	{
        try 
        {
    		if (_RecordTimer != null) 
    		{
    			_RecordTimer.cancel();
    			_RecordTimer = null;
    		}
        } 
        catch (Exception e) 
        {
			_RecordTimer = null;
        }
        try 
        {
        	if(_Recorder != null)
        	{
        		_Recorder.stop();
        		_Recorder.reset();
        		_Recorder.release();
        		_Recorder = null;
        	}
        } 
        catch (Exception e) 
        {
    		_Recorder = null;
        }
	}
	private void _StopPlay()
	{
        try 
        {
    		if (_PlayTimer != null) 
    		{
    			_PlayTimer.cancel();
    			_PlayTimer = null;
    		}
        } 
        catch (Exception e) 
        {
        	_PlayTimer = null;
        }
		if (_MediaPlayer.isPlaying())
		{
			_MediaPlayer.stop();
		}
	}
	
	private void _AddAttachment(Attachment pAttachment)
	{
		if (_Attachments.size() == 0)
		{
			_hsvAttachment.setVisibility(View.VISIBLE);
		}
		_Attachments.add(pAttachment);
		View attahmentView = _Inflater.inflate(R.layout.attachment, null, false);
		ImageView iv = (ImageView) attahmentView.findViewById(R.id.ivAttachment);
		iv.setTag(pAttachment);
		if (pAttachment.HasLoadedImage())
		{
			iv.setImageBitmap(pAttachment.GetImage());
		}
		else
		{
			iv.setImageResource(R.drawable.ic_load);
			__Loader.Load(iv, pAttachment);
		}
		iv.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View pView)
			{
				((Attachment) pView.getTag()).Open(__GetMainActivity(), false);
			}
		});		
		ImageView ivRemove = (ImageView) attahmentView.findViewById(R.id.ivAttachmentRemove);
		ivRemove.setTag(pAttachment);
		ivRemove.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View pView)
			{
				_RemoveAttachment((Attachment) pView.getTag());
			}
		});
		_llAttachment.addView(attahmentView);
		_ivSend.setImageResource(R.drawable.ic_main_send);
	}
	private void _RemoveAttachment(Attachment pAttachment)
	{
		int index = 0;
		for (int i = 0; i < _Attachments.size(); i++)
		{
			if (pAttachment.GetID().equals(_Attachments.get(i).GetID()))
			{
				index = i;
				break;
			}
		}
		_Attachments.remove(index);
		_llAttachment.removeViewAt(index);
		if (_Attachments.size() == 0)
		{
			_hsvAttachment.setVisibility(View.GONE);
			
			if (_etSend.getText().toString().length() == 0)
			{
				_ivSend.setImageResource(R.drawable.ic_main_photo);
			}
			else
			{
				_ivSend.setImageResource(R.drawable.ic_main_send);
			}
		}
	}
	private void _ClearAttachments()
	{
		_hsvAttachment.setVisibility(View.GONE);
		_llAttachment.removeAllViews();
		_Attachments.clear();
		_etSend.getText().clear();
		_ivSend.setImageResource(R.drawable.ic_main_photo);
	}


	@SuppressWarnings("deprecation")
	protected final void __CreateView(View pView, SparseArray<String> pStore, AbstractMessageContainer<T> pContainer, boolean pShowPassword)
	{
		_hContainer = pContainer;
		_hContainer.SetAdapter(__Adapter);
		__PlacePressed = pStore.get(_PRESSED_PLACE) == null ? false : Boolean.parseBoolean(pStore.get(_PRESSED_PLACE));
		__TranslatePressed = pStore.get(_PRESSED_TRANSLATE) == null ? false : Boolean.parseBoolean(pStore.get(_PRESSED_TRANSLATE));
		
		
		//_llTitle = (LinearLayout) pView.findViewById(R.id.llMessagesTitle);
		
		_llMenu = (LinearLayout) pView.findViewById(R.id.llMessagesMenu);
		pView.findViewById(R.id.ivMessagesBack).setOnClickListener(this);
		__tvTitle = (TextView) pView.findViewById(R.id.tvMessagesTitle);
		_ivLock = (ImageView) pView.findViewById(R.id.ivMessagesLock);
		if (_hContainer.GetType() == AbstractMessageContainer.SECRET)
		{
			_ivLock.setImageResource(R.drawable.ic_lock);
		}
		else
		{
			_ivLock.setImageResource(R.drawable.ic_unlock);
		}
		_ivSearch = (ImageView) pView.findViewById(R.id.ivMessagesSearch);
		
		_llSearch = (LinearLayout) pView.findViewById(R.id.llMessagesSearch);
		_etSearch = (EditText) pView.findViewById(R.id.etMessagesSearch);
		_etSearch.addTextChangedListener(_SearchTextWatcher);
		_ivUp = (ImageView) pView.findViewById(R.id.ivMessagesUp);
		_ivUp.setOnClickListener(this);
		_ivDown = (ImageView) pView.findViewById(R.id.ivMessagesDown);
		_ivDown.setOnClickListener(this);
		if (_etSearch.getText().length() > 0)
		{
			_ivUp.setVisibility(View.VISIBLE);
			_ivDown.setVisibility(View.VISIBLE);
		}
		else
		{
			_ivUp.setVisibility(View.GONE);
			_ivDown.setVisibility(View.GONE);
		}
		pView.findViewById(R.id.ivMessagesClose).setOnClickListener(this);
		
		pView.findViewById(R.id.llMessagesShadow).bringToFront();

		pView.findViewById(R.id.llMessages).setBackgroundDrawable(__GetMainActivity().GetProfile().GetTheme());
		
		_pb = (ProgressBar) pView.findViewById(R.id.pbMessages);
		_pb.bringToFront();
		
		//_rlMessages = (RelativeLayout) pView.findViewById(R.id.rlMessages);
		
		_rv = (ObservableRecyclerView) pView.findViewById(R.id.lvMessages);
		_llManager = new LinearLayoutManager(getActivity());
		_llManager.setStackFromEnd(true);
		_rv.setLayoutManager(_llManager);
		_rv.setHasFixedSize(true);
//		_rv.setScrollViewCallbacks(new ObservableScrollViewCallbacks()
//		{
//			@Override
//			public void onUpOrCancelMotionEvent(ScrollState pScrollState)
//			{
//				if (__Adapter.getItemCount() > 20)
//				{
//			        if (pScrollState == ScrollState.UP) 
//			        {
//			            if (_llTitle.getVisibility() == View.VISIBLE) 
//			            {
//			            	_llTitle.setVisibility(View.GONE);
//			            }
//			        } 
//			        else if (pScrollState == ScrollState.DOWN) 
//			        {
//			            if (_llTitle.getVisibility() == View.GONE) 
//			            {
//			            	_llTitle.setVisibility(View.VISIBLE);
//			            }
//			        }
//				}
//				else
//				{
//					_llTitle.setVisibility(View.VISIBLE);
//				}
//			}
//			@Override
//			public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging)
//			{
//			}
//			@Override
//			public void onDownMotionEvent()
//			{
//			}
//		});
		_rv.addOnScrollListener(new RecyclerView.OnScrollListener() 
		{
	        @Override
	        public void onScrolled(RecyclerView recyclerView, int pDx, int pDy) 
	        {
	            if (!_ScrollLock) 
	            {
		            int firstVisibleItem = _llManager.findFirstVisibleItemPosition();
		            if (firstVisibleItem == 0) 
		            {
		            	if (!__MessageOver)
		            	{
			            	_ScrollLock = true;
		        			_pb.setVisibility(View.VISIBLE);
		        			
			            	try
			        		{
			    				__GetMessagesNext(__Adapter.getItemCount() / __LOAD_COUNT);
			        		}
			        		catch (JSONException e)
			        		{
			        		}
		            	}
		            }
	            }
	            if (_llManager.findLastCompletelyVisibleItemPosition() == _llManager.getItemCount() - 1)
	            {
	            	_ScrollOnBottom = true;
	            }
	            else
	            {
	            	_ScrollOnBottom = false;
	            }
	        }
	    });
		_rv.addOnItemTouchListener(new RecyclerView.OnItemTouchListener()
		{
			@Override
			public void onTouchEvent(RecyclerView arg0, MotionEvent arg1)
			{
			}
			@Override
			public void onRequestDisallowInterceptTouchEvent(boolean arg0)
			{
			}
			@Override
			public boolean onInterceptTouchEvent(RecyclerView arg0, MotionEvent arg1)
			{
				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
			    imm.hideSoftInputFromWindow(_etSend.getWindowToken(), 0);
				_llAttach.post(new Runnable()
				{
					@Override
					public void run()
					{
						_llAttach.setVisibility(View.GONE);
						_InitAttachMenu();
					}
				});
				return false;
			}
		});
		
		_llCode = (LinearLayout) pView.findViewById(R.id.llMessagesLock);
		_etCode = (EditText) pView.findViewById(R.id.etMessagesLock);
		_etCode.addTextChangedListener(_CodeTextWatcher);
		
		_hsvAttachment = (HorizontalScrollView) pView.findViewById(R.id.hsvMessagesAttachment);
		_llAttachment = (LinearLayout) pView.findViewById(R.id.llMessagesAttachment);
		_llAttachment.removeAllViews();
		_hsvAttachment.setVisibility(View.GONE);
		
		__llSend = (LinearLayout) pView.findViewById(R.id.llMessagesSend);
		__llSend.bringToFront();
		
		_ivAttach = (ImageView) pView.findViewById(R.id.ivMessagesAttach);
		_ivAttach.setOnClickListener(this);
		_ivSend = (ImageView) pView.findViewById(R.id.ivMessagesSend);
		_ivSend.setOnClickListener(this);
		_etSend = (EditText) pView.findViewById(R.id.etMessages);
		_etSend.setOnClickListener(this);
		_etSend.setText(pContainer.GetTypedText());
		_etSend.addTextChangedListener(_SendTextWatcher);
		_etSend.setOnTouchListener(new View.OnTouchListener()
		{
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				_etSend.callOnClick();
				return false;
			}
		});
		_etSend.setOnKeyListener(new View.OnKeyListener()
		{
			@Override
			public boolean onKey(View pView, int pKeyCode, KeyEvent pEvent)
			{
				if (__GetMainActivity().GetProfile().IsMessagesEnter() && pKeyCode == KeyEvent.KEYCODE_ENTER && pEvent.getAction() == KeyEvent.ACTION_DOWN)
				{
					String text = _etSend.getText().toString().trim();
					if (text.length() > 0)
					{
						try
						{			
							__SendMessage(text, false, _Attachments);
							
							_ClearAttachments();
						}
						catch (Exception e)
						{
						}
					}
					return true;
				}
				return false;
			}
		});
		if (_Attachments.size() == 0 && _etSend.getText().toString().trim().length() == 0)
		{
			_ivSend.setImageResource(R.drawable.ic_main_photo);
		}
		else
		{
			_ivSend.setImageResource(R.drawable.ic_main_send);
		}
		
		_llAttach = (LinearLayout) pView.findViewById(R.id.llMessagesAttach);
		_llAttach.setVisibility(View.GONE);
        LinearLayout.LayoutParams paramsAttach = (LinearLayout.LayoutParams) _llAttach.getLayoutParams();
        paramsAttach.height = __GetMainActivity().GetProfile().GetKeyboardHeight();
        _llAttach.setLayoutParams(paramsAttach);
        
        _llAttachSettings = (LinearLayout) pView.findViewById(R.id.llMessagesAttachSettings);
        _llAttachSettings.setVisibility(View.INVISIBLE);
		
		_ivPlace = (ImageView) pView.findViewById(R.id.ivMessagesPlace);
		_ivPlace.setOnClickListener(this);
		_ivPlace.setSelected(__PlacePressed);
		_ivTranslate = (ImageView) pView.findViewById(R.id.ivMessagesTranslate);
		_ivTranslate.setOnClickListener(this);
		_ivTranslate.setSelected(__TranslatePressed);
        
        _llAttachMain = (LinearLayout) pView.findViewById(R.id.llMessagesAttachMain);
        _llAttachMain.setVisibility(View.INVISIBLE);
		pView.findViewById(R.id.llMessagesAttachMainPhoto).setOnClickListener(this);
		pView.findViewById(R.id.llMessagesAttachMainVideo).setOnClickListener(this);
		pView.findViewById(R.id.llMessagesAttachMainAudio).setOnClickListener(this);
		pView.findViewById(R.id.llMessagesAttachMainVoice).setOnClickListener(this);
		pView.findViewById(R.id.llMessagesAttachMainFile).setOnClickListener(this);
		pView.findViewById(R.id.llMessagesAttachMainPlace).setOnClickListener(this);
		pView.findViewById(R.id.llMessagesAttachMainContact).setOnClickListener(this);
		pView.findViewById(R.id.llMessagesAttachMainHide).setOnClickListener(this);

		_llAttachVoice = (LinearLayout) pView.findViewById(R.id.llMessagesAttachVoice);
		_llAttachVoice.setVisibility(View.GONE);
		pView.findViewById(R.id.ivMessagesAttachVoiceBack).setOnClickListener(this);
		
		_llAttachVoiceRecord = (LinearLayout) pView.findViewById(R.id.llMessagesAttachVoiceRecord);
		_llAttachVoiceRecord.setOnClickListener(this);
        RelativeLayout.LayoutParams paramsVoice = (RelativeLayout.LayoutParams) _llAttachVoiceRecord.getLayoutParams();
        paramsVoice.height = __GetMainActivity().GetProfile().GetKeyboardHeight()  * 2 / 3;
        paramsVoice.width = paramsVoice.height;
        _llAttachVoiceRecord.setLayoutParams(paramsVoice);
        
        _ivAttachVoice = (ImageView) pView.findViewById(R.id.ivMessagesAttachVoiceRecord);
        _tvAttachVoice = (TextView) pView.findViewById(R.id.tvMessagesAttachVoiceRecord);
        
        _ivAttachVoiceOk = (ImageView) pView.findViewById(R.id.ivMessagesAttachVoiceOK);
        _ivAttachVoiceOk.setOnClickListener(this);
        _ivAttachVoiceOk.setVisibility(View.INVISIBLE);

		_KeyboardShowing = false;
		View rootView = __GetMainActivity().getWindow().getDecorView().findViewById(android.R.id.content);
		rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() 
		{
		    public void onGlobalLayout()
		    {
		    	__Log("onGlobalLayout");
		        Rect r = new Rect();
		        View view = __GetMainActivity().getWindow().getDecorView();
		        view.getWindowVisibleDisplayFrame(r);
		        int visible = r.bottom - r.top;
		    	final int height = view.getHeight() - visible - r.top;
		    	if (height > 150)
		    	{
		    		_KeyboardShowing = true;
		    		
			        _llAttach.postDelayed(new Runnable()
					{
						@Override
						public void run()
						{
							_llAttachSettings.setVisibility(View.VISIBLE);
							_llAttachMain.setVisibility(View.VISIBLE);
						}
					}, 300);
		    		
		    		if (height != __GetMainActivity().GetProfile().GetKeyboardHeight())
		    		{
				    	__Log("onGlobalLayout CHANGED - " + Integer.toString(height));
		    			__GetMainActivity().GetProfile().SetKeyboardHeight(height);
		    			
					    LinearLayout.LayoutParams params = (LayoutParams) _llAttach.getLayoutParams();
				        params.height = height;
				        _llAttach.setLayoutParams(params);
				        
				        RelativeLayout.LayoutParams paramsVoice = (RelativeLayout.LayoutParams) _llAttachVoiceRecord.getLayoutParams();
				        paramsVoice.height = __GetMainActivity().GetProfile().GetKeyboardHeight() * 2 / 3;
				        paramsVoice.width = paramsVoice.height;
				        _llAttachVoiceRecord.setLayoutParams(paramsVoice);
		    		}
		    	}
		    	else
		    	{
		    		_KeyboardShowing = false;
		    	}
		    }
		});
		
		_rlDialog = (RelativeLayout) pView.findViewById(R.id.rlMessageDialog);
		_rlDialog.setVisibility(View.GONE);
		_rlDialog.setOnClickListener(this);
		pView.findViewById(R.id.llMessageDialogResend).setOnClickListener(this);
		_llDialogCopy = (LinearLayout) pView.findViewById(R.id.llMessageDialogCopy);
		_llDialogCopy.setOnClickListener(this);
		pView.findViewById(R.id.llMessageDialogDelete).setOnClickListener(this);
		
		if (_Attachments.size() == 0)
		{
			_hsvAttachment.setVisibility(View.GONE);
		}
		else
		{
			_hsvAttachment.setVisibility(View.VISIBLE);
			for (int i = 0; i < _Attachments.size(); i++)
			{
				View attahmentView = _Inflater.inflate(R.layout.attachment, null, false);
				ImageView iv = (ImageView) attahmentView.findViewById(R.id.ivAttachment);
				if (_Attachments.get(i).HasLoadedImage())
				{
					iv.setImageBitmap(_Attachments.get(i).GetImage());
				}
				else
				{
					iv.setImageResource(R.drawable.ic_load);
					__Loader.Load(iv, _Attachments.get(i));
				}
				ImageView ivRemove = (ImageView) attahmentView.findViewById(R.id.ivAttachmentRemove);
				ivRemove.setTag(_Attachments.get(i));
				ivRemove.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View pView)
					{
						_RemoveAttachment((Attachment) pView.getTag());
					}
				});
				_llAttachment.addView(attahmentView);
			}
			_ivSend.setImageResource(R.drawable.ic_main_send);
		}
		
		_rlSecret = (RelativeLayout) pView.findViewById(R.id.rlMessagesSecretEmpty);
		_rlSecret.setVisibility(View.GONE);
		_rlSecret.setOnClickListener(this);
		_etSecret = (EditText) pView.findViewById(R.id.etMessagesSecretEmpty);
		_ivSecret = (ImageView) pView.findViewById(R.id.ivMessagesSecretEmpty);
		pView.findViewById(R.id.ivMessagesSecretEmpty).setOnClickListener(this);
		pView.findViewById(R.id.btnMessagesSecretEmpty).setOnClickListener(this);
		
		if (pStore.get(PLACE_LAT) != null)
		{
			double lat = Double.parseDouble(pStore.get(PLACE_LAT));
			double lng = Double.parseDouble(pStore.get(PLACE_LNG));
			pStore.remove(PLACE_LAT);
			pStore.remove(PLACE_LNG);
			try
			{
				Attachment attachment = new Attachment(__GetMainActivity(), "", lat, lng);	
				_AddAttachment(attachment);
			}
			catch (Exception e)
			{
			}
		}
		
		if (pStore.get(CONTAINER_TYPE) != null)
		{
			_ResendMessageType = Integer.parseInt(pStore.get(CONTAINER_TYPE));
			_ResendMessageID = pStore.get(MESSAGE_ID);
			_ResendMessageContainerID = pStore.get(CONTAINER_ID);
			pStore.delete(CONTAINER_ID);
			pStore.delete(CONTAINER_TYPE);
			pStore.delete(MESSAGE_ID);
		}
		else
		{
			_ResendMessageType = -1;
		}
		
		if (pShowPassword)
		{
			_Inited = false;
			_pb.setVisibility(View.GONE);
			_rv.setVisibility(View.GONE);
			__llSend.setVisibility(View.GONE);
			_llCode.setVisibility(View.VISIBLE);
		}
		else
		{
			__Init();
		}
	}
	
	protected final void __UnlockScroll()
	{
		_rv.postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				_pb.setVisibility(View.GONE);
				_ScrollLock = false;
			}
		}, 200);
	}
	
	protected void __Init()
	{
		_ivLock.setOnClickListener(this);
		_ivSearch.setOnClickListener(this);
		_rv.setVisibility(View.VISIBLE);
		_llCode.setVisibility(View.GONE);
		_rv.setAdapter(__Adapter);

		_ScrollLock = true;
		_pb.setVisibility(View.VISIBLE);
		
		try
		{
			__SynchronizeMessages();
		}
		catch (JSONException e)
		{
		}
		
		AbstractMessage message = null;
		switch (_ResendMessageType)
		{
			case 0:
				long userID = Long.parseLong(_ResendMessageContainerID);
				Dialog dialog = __GetMainActivity().GetDialogs().GetDialog(userID);
				message = dialog.GetMessage(_ResendMessageID);
				break;
			case 1:
				Chat chat = __GetMainActivity().GetChats().GetChat(_ResendMessageContainerID);
				message = chat.GetMessage(_ResendMessageID);
				break;
			case 2:
				Delivery delivery = __GetMainActivity().GetDeliveries().GetDelivery(_ResendMessageContainerID);
				message = delivery.GetMessage(_ResendMessageID);
				break;
		}
		try
		{
			__SendMessage(message.GetMessage(), true, message.GetAttachments());
		}
		catch (Exception e)
		{
		}
		_ResendMessageType = -1;
		_Inited = true;
	}

	protected void __ResendMessage(T pMessage)
	{
		if (pMessage.GetStatus() >= AbstractMessage.STATUS_NOTSENDED)
		{
			__GetMainActivity().GetManager().GoToResend(__Adapter.GetContainerID(), __Adapter.GetContainerType(), pMessage);
		}
		else
		{
			__GetMainActivity().OpenDialog(10, __GetMainActivity().getString(R.string.Dialog39Title), __GetMainActivity().getString(R.string.Dialog39Description));
		}
	}


	protected abstract void __SendMessage(String pMessage, boolean pIsAttachmentUploaded, ArrayList<Attachment> pAttachment) throws Exception;
	protected abstract void __SynchronizeMessages() throws JSONException;
	protected abstract void __GetMessagesNext(int pOffset) throws JSONException;
}