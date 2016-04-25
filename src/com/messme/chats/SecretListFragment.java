package com.messme.chats;

import com.messme.ActivityMain;
import com.messme.R;
import com.messme.chats.messages.AbstractMessageContainer;
import com.messme.view.TextWatcher;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;


public class SecretListFragment extends Fragment implements OnItemClickListener, OnItemLongClickListener
{
	private final ActivityMain _hActivity;
	private final FragmentChats _hFragment;

	private RelativeLayout _rl;
	private ListView _lv;
	private LinearLayout _llEmpty;
	
	private LinearLayout _llCode;
	
	private TextView _tvCodeEmpty;
	private LinearLayout _llCodeEmpty;
	private EditText _etCodeEmpty;
	private ImageView _ivCodeEmpty;
	private Button _btnCodeEmpty;

	private TextView _tvCode;
	private EditText _etCode;
	
	private AdapterChats _Adapter;
	
	private TextWatcher _CodeTextWatcher = null;
	
	public SecretListFragment(ActivityMain pActivity, FragmentChats pFragment)
	{
		_hActivity = pActivity;
		_hFragment = pFragment;
		_Adapter = new AdapterChats(_hActivity, AbstractMessageContainer.SECRET);
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
	public View onCreateView(LayoutInflater pInflater, ViewGroup pContainer, Bundle pSavedInstanceState) 
	{
		View view = pInflater.inflate(R.layout.chats_secret, pContainer, false);
		
		_rl = (RelativeLayout) view.findViewById(R.id.rlSecret2);
		
		_lv = (ListView) view.findViewById(R.id.lvSecret);
		_lv.setOnItemClickListener(this);
		_lv.setOnItemLongClickListener(this);
		_lv.setAdapter(_Adapter);
		
		_llEmpty = (LinearLayout) view.findViewById(R.id.llSecretEmpty);
		
		_llCode = (LinearLayout) view.findViewById(R.id.llSecretCode);
		
		_tvCode = (TextView) view.findViewById(R.id.tvSecretCode);
		_etCode = (EditText) view.findViewById(R.id.etSecretCode);
		_etCode.addTextChangedListener(_CodeTextWatcher);
		
		_tvCodeEmpty = (TextView) view.findViewById(R.id.tvSecretCodeEmpty);
		_llCodeEmpty = (LinearLayout) view.findViewById(R.id.llSecretCodeEmpty);
		_etCodeEmpty = (EditText) view.findViewById(R.id.etSecretCodeEmpty);
		_ivCodeEmpty = (ImageView) view.findViewById(R.id.ivSecretCodeEmpty);
		_btnCodeEmpty = (Button) view.findViewById(R.id.btnSecretCodeEmpty);
		_btnCodeEmpty.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
			}
		});
		view.findViewById(R.id.ivSecretCodeEmpty).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (_etCodeEmpty.getTransformationMethod() instanceof PasswordTransformationMethod)
				{
					_ivCodeEmpty.setImageResource(R.drawable.ic_eye_close);
					_etCodeEmpty.setTransformationMethod(null);
				}
				else
				{
					_ivCodeEmpty.setImageResource(R.drawable.ic_eye_open);
					_etCodeEmpty.setTransformationMethod(new PasswordTransformationMethod());
				}
			}
		});
		
		Update();
		
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
		_hFragment.Click(_Adapter.getItem(pPosition));
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> pParent, View pView, int pPosition, long pID)
	{
		_hFragment.LongClick(_Adapter.getItem(pPosition), AbstractMessageContainer.SECRET);
		return true;
	}
	
	public void Lock()
	{
		if (_llCode != null)
		{
			if (_hActivity.GetProfile().IsSecurityPinInitiated())
			{
				_tvCodeEmpty.setVisibility(View.GONE);
				_llCodeEmpty.setVisibility(View.GONE);
				_btnCodeEmpty.setVisibility(View.GONE);
				_tvCode.setVisibility(View.VISIBLE);
				_etCode.setVisibility(View.VISIBLE);
				_etCode.setText("");
			}
			else
			{
				_tvCodeEmpty.setVisibility(View.VISIBLE);
				_llCodeEmpty.setVisibility(View.VISIBLE);
				_btnCodeEmpty.setVisibility(View.VISIBLE);
				_etCodeEmpty.setText("");
				_tvCode.setVisibility(View.GONE);
				_etCode.setVisibility(View.GONE);
			}
			_llCode.setVisibility(View.VISIBLE);
			_rl.setVisibility(View.GONE);
		}
	}
	
	public void Update()
	{
		if (_Adapter != null)
		{
			_Adapter.notifyDataSetChanged();
			
			if (_llEmpty != null && _lv != null)
			{
				if (_Adapter.getCount() == 0)
				{
					_llEmpty.setVisibility(View.VISIBLE);
					_lv.setVisibility(View.GONE);
				}
				else
				{
					_llEmpty.setVisibility(View.GONE);
					_lv.setVisibility(View.VISIBLE);
				}
			}
		}
	}

	public void SetFilter(String pText)
	{
		if (_Adapter != null)
		{
			_Adapter.SetFilter(pText);
			_Adapter.notifyDataSetChanged();
		}
	}
}