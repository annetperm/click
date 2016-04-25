package com.messme.registration;

import org.json.JSONException;
import org.json.JSONObject;

import com.messme.ActivityMain;
import com.messme.R;
import com.messme.socket.CommunicationSocket;
import com.messme.socket.MessageItem;
import com.messme.view.MessmeFragment;
import com.messme.view.TextWatcher;
import com.messme.view.TextWatcher.iOnTextChanged;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;


public class FragmentSMS extends MessmeFragment implements OnClickListener, iOnTextChanged, CommunicationSocket.iSocketListener
{	
	private static final int _ET_CODE = 1000;
	
	
	private BroadcastReceiver _SmsListener = new BroadcastReceiver()
	{
	    @Override
	    public void onReceive(Context pContext, Intent pIntent) 
	    {
	        if(pIntent.getAction().equals("android.provider.Telephony.SMS_RECEIVED"))
	        {
	            Bundle bundle = pIntent.getExtras();           
	            SmsMessage[] messages = null;
	            if (bundle != null)
	            {
	                try
	                {
	                    Object[] pdus = (Object[]) bundle.get("pdus");
	                    messages = new SmsMessage[pdus.length];
	                    for(int i = 0; i < messages.length; i++)
	                    {
	                    	messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
	                        String from = messages[i].getOriginatingAddress();
	                        if (from.equals(""))
	                        {
		                        String body = messages[i].getMessageBody();
		                        _etCode.setText(body);
	                        }
	                    }
	                }
	                catch(Exception e)
	                {
	                }
	            }
	        }
	    }
	};
	
	
	private EditText _etCode = null;
	private LinearLayout _llError = null;
	private Button _btnRepeat = null;
	private Button _btnNext = null;
	private ProgressBar _pb = null;

	private TextWatcher _CodeTextWatcher;
	
	private String _Code;
	private String _Phone;
	private String _Promo;
	private String _CodeValue;
	
	private CommunicationSocket _Socket;	
	
	private boolean _NeedRestore = false;
	
	public FragmentSMS(ActivityMain pActivity, SparseArray<String> pStore)
	{
		super(pActivity, pStore);
		_CodeTextWatcher = new TextWatcher(this, 1);
	}

	@Override
	protected View __OnCreateView(LayoutInflater pInflater, ViewGroup pContainer, SparseArray<String> pStore)
	{
		_CodeValue = pStore.get(_ET_CODE) == null ? "" : pStore.get(_ET_CODE);
		_Code = pStore.get(CODE);
		_Phone = pStore.get(PHONE);
		_Promo = pStore.get(PROMO);
		
		View view = pInflater.inflate(R.layout.sms, pContainer, false);
		
		view.findViewById(R.id.ivSMSBack).setOnClickListener(this);
		
		_btnRepeat = (Button) view.findViewById(R.id.btnSMSRepeat);
		_btnRepeat.setOnClickListener(this);
		
		_btnNext = (Button) view.findViewById(R.id.btnSMSNext);
		_btnNext.setOnClickListener(this);
		
		_pb = (ProgressBar) view.findViewById(R.id.pbSMS);
		_pb.setVisibility(View.GONE);

		_etCode = (EditText) view.findViewById(R.id.etSMSCode);
		_etCode.setText(_CodeValue);
		_etCode.addTextChangedListener(_CodeTextWatcher);
		
		_llError = (LinearLayout) view.findViewById(R.id.llSMSError);
		
		__GetMainActivity().registerReceiver(_SmsListener, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
		
		_Socket = new CommunicationSocket(this);
		_Socket.Connect("User/?id=" + _Code + _Phone);
		
		View rootView = __GetMainActivity().getWindow().getDecorView().findViewById(android.R.id.content);
		rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() 
		{
		    public void onGlobalLayout()
		    {
		        Rect r = new Rect();
		        View view = __GetMainActivity().getWindow().getDecorView();
		        view.getWindowVisibleDisplayFrame(r);
		        int visible = r.bottom - r.top;
		    	int height = view.getHeight() - visible - r.top;
		        
		    	if (height > 150)
		    	{
		    		if (height != __GetMainActivity().GetProfile().GetKeyboardHeight())
		    		{
		    			__GetMainActivity().GetProfile().SetKeyboardHeight((int)(height * 1.3f));
		    		}
		    	}
		    }
		});
		
		return view;
	}

	@Override
	protected void __OnSave(SparseArray<String> pStore)
	{
		pStore.put(_ET_CODE, _CodeValue);
	}
	
	@Override
	protected void __OnDestroy()
	{
		__GetMainActivity().unregisterReceiver(_SmsListener);
		_etCode.removeTextChangedListener(_CodeTextWatcher);
		_Socket.Disconnect();
		_Socket = null;
	}

	@Override
	public void OnTextChanged(int pID, String pText)
	{
		_CodeValue = pText;
	}

	@Override
	public void onClick(View pView)
	{
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
	    imm.hideSoftInputFromWindow(_etCode.getWindowToken(), 0);
		switch (pView.getId())
		{
			case R.id.ivSMSBack:
				__GetMainActivity().GetManager().GoToBack();
				break;
			case R.id.btnSMSRepeat:
				_pb.setVisibility(View.VISIBLE);
				_btnRepeat.setVisibility(View.GONE);
				_btnNext.setVisibility(View.GONE);
				
				_Socket.Send("user.getactivationcode", null);
				break;
			case R.id.btnSMSNext:
				if (_etCode.getText().toString().length() == 0)
				{
					__GetMainActivity().OpenDialog(1, getActivity().getText(R.string.SMSMessageRepeatHeader).toString(), getActivity().getText(R.string.SMSMessageRepeatText).toString());
				}
				else
				{
					_pb.setVisibility(View.VISIBLE);
					_btnRepeat.setVisibility(View.GONE);
					_btnNext.setVisibility(View.GONE);

					try
					{
						// теперь проверим что за пользователь
						JSONObject options = new JSONObject();
						options.put("userid", Long.parseLong(_Code + _Phone));
						_Socket.Send("user.checkuser", options);
					}
					catch (JSONException e)
					{
					}
				}
				break;
		}
	}

	@Override
	public void OnConnect()
	{
	}
	@Override
	public void OnMessageSendError(MessageItem pItem, boolean pTimeOut)
	{
		_pb.setVisibility(View.GONE);
		_btnRepeat.setVisibility(View.VISIBLE);
		_btnNext.setVisibility(View.VISIBLE);
		
		if (pItem.GetAction().equals("user.getactivationcode"))
		{
			__GetMainActivity().OpenDialog(1, getActivity().getText(R.string.SMSMessageRepeatHeader).toString(), getActivity().getText(R.string.SMSMessageRepeatText).toString());
		}
		else if (pItem.GetAction().equals("user.checkuser") || pItem.GetAction().equals("user.register") || pItem.GetAction().equals("user.activate"))
		{
			__GetMainActivity().OpenDialog(1, getActivity().getText(R.string.SMSMessageContinueHeader).toString(), getActivity().getText(R.string.SMSMessageContinueText).toString());
		}
	}
	@Override
	public void OnMessageSendSuccess(MessageItem pItem)
	{
	}
	@Override
	public void OnMessageReceive(String pMID, String pAction, int pStatus, String pResult, int pType, JSONObject pOptions)
	{
		if (pAction.equals("user.getactivationcode") && pStatus == 1000)
		{
			_pb.setVisibility(View.GONE);
			_btnRepeat.setVisibility(View.VISIBLE);
			_btnNext.setVisibility(View.VISIBLE);
			// Ждите получения кода
		}
		else if (pAction.equals("user.checkuser"))
		{
			if (pStatus == 1016 || pStatus == 1000)
			{
				if (pStatus == 1016)
				{
					//востановление
					_NeedRestore = true;
				}
				else
				{
					_NeedRestore = false;
				}

				try
				{
					JSONObject options = new JSONObject();
					options.put("invite", _Promo);
					_Socket.Send("user.register", options);
				}
				catch (JSONException e)
				{
				}
			}
			else
			{
				__GetMainActivity().OpenDialog(1, __GetMainActivity().getString(R.string.Dialog20Title), __GetMainActivity().getString(R.string.Dialog20Description));
			}
		}
		else if (pAction.equals("user.register") && pStatus == 1000)
		{
			try
			{
				int code = Integer.parseInt(_etCode.getText().toString());
				
				JSONObject options = new JSONObject();
				options.put("code", code);
				_Socket.Send("user.activate", options);

				_llError.setVisibility(View.INVISIBLE);
				_etCode.setBackgroundResource(R.drawable.drawable_et_line);
			}
			catch (NumberFormatException e)
			{
			}
			catch (JSONException e)
			{
			}
		}
		else if (pAction.equals("user.activate"))
		{
			if (pStatus == 1003)
			{
				// код не верен
				_pb.setVisibility(View.GONE);
				_btnRepeat.setVisibility(View.VISIBLE);
				_btnNext.setVisibility(View.VISIBLE);
				_llError.setVisibility(View.VISIBLE);
				_etCode.setBackgroundResource(R.drawable.drawable_et_line_error);
			}
			else if (pStatus == 1000)
			{
				__GetMainActivity().GetProfile().SetUUID(pResult);
				if (_NeedRestore)
				{
					__GetMainActivity().GetManager().GoToRestore(_Code, _Phone);
				}
				else
				{
					__GetMainActivity().GetProfile().SetIDandUUID();
					__GetMainActivity().GetManager().GoToWelcome();
				}
			}
			else
			{
				__GetMainActivity().OpenDialog(2, __GetMainActivity().getString(R.string.Dialog21Title), __GetMainActivity().getString(R.string.Dialog21Description));
			}
		}
	}
}