package com.messme.chats.messages.resend;

import com.messme.ActivityMain;
import com.messme.R;
import com.messme.chats.messages.chat.Chat;
import com.messme.chats.messages.dialog.Dialog;
import com.messme.view.TextWatcher;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.AdapterView.OnItemClickListener;


public class SecretListFragment extends Fragment implements OnItemClickListener
{
	private ListView _lv;
	private RelativeLayout _rl;
	private LinearLayout _llCode;
	private EditText _etCode;
	
	private TextWatcher _CodeTextWatcher = null;
	
	private final ActivityMain _hActivity;
	private final String _ContainerID;
	private final int _ContainerType;
	private final String _MessageID;
	private AdapterResendChats _Adapter = null;
	
	public SecretListFragment(ActivityMain pActivity, String pContainerID, int pContainerType, String pMessageID)
	{
		_hActivity = pActivity;
		_ContainerID = pContainerID;
		_ContainerType = pContainerType;
		_MessageID = pMessageID;
		_CodeTextWatcher = new TextWatcher(new TextWatcher.iOnTextChanged()
		{
			@Override
			public void OnTextChanged(int pID, String pText)
			{
				if (_hActivity.GetProfile().CheckSecurityPin(pText))
				{
					InputMethodManager imm = (InputMethodManager) _hActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
				    imm.hideSoftInputFromWindow(_etCode.getWindowToken(), 0);
					_llCode.setVisibility(View.GONE);
					_rl.setVisibility(View.VISIBLE);
				}
			}
		}, 1);
	}
	
	@Override
	public View onCreateView(LayoutInflater pInflater, ViewGroup pContainer, Bundle savedInstanceState)
	{
		View view = pInflater.inflate(R.layout.resend_fragment_secret, pContainer, false);
		
		_rl = (RelativeLayout) view.findViewById(R.id.rlResendSecret2);
		
		_llCode = (LinearLayout) view.findViewById(R.id.llResendSecretCode);
		_etCode = (EditText) view.findViewById(R.id.etResendSecretCode);
		_etCode.addTextChangedListener(_CodeTextWatcher);
		
		_lv = (ListView) view.findViewById(R.id.lvResendSecret);
		_lv.setOnItemClickListener(this);
		_Adapter = new AdapterResendChats(_hActivity, _ContainerID, _ContainerType, false);
		_lv.setAdapter(_Adapter);
		
		Lock();
		
	    return view;
	}
	@Override
	public void onDestroy()
	{
		_etCode.removeTextChangedListener(_CodeTextWatcher);
		super.onDestroy();
	}

	@Override
	public void onItemClick(AdapterView<?> pParent, View pView, int pPosition, long pID)
	{
		if (_Adapter.getItem(pPosition) instanceof Dialog)
		{
			Dialog dialog = (Dialog) _Adapter.getItem(pPosition);
			_hActivity.GetManager().GoToDialog(dialog.GetUserID(), true, _ContainerID, _ContainerType, _MessageID);
		}
		else
		{
			Chat chat = (Chat) _Adapter.getItem(pPosition);
			_hActivity.GetManager().GoToChat(chat, true, _ContainerID, _ContainerType, _MessageID);
		}
	}
	
	public void Lock()
	{
		if (_llCode != null)
		{
			_etCode.setText("");
			_llCode.setVisibility(View.VISIBLE);
			_rl.setVisibility(View.GONE);
		}
	}
}