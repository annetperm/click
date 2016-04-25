package com.messme.registration;

import org.json.JSONException;
import org.json.JSONObject;

import com.messme.ActivityMain;
import com.messme.R;
import com.messme.socket.CommunicationSocket;
import com.messme.socket.ImageLoader;
import com.messme.socket.MessageItem;
import com.messme.user.User;
import com.messme.view.MessmeFragment;

import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;


public class FragmentRestore extends MessmeFragment implements OnClickListener, CommunicationSocket.iSocketListener
{
	private LinearLayout _llUser = null;
	private ProgressBar _pb = null;
	private Button _btnYES = null;
	private Button _btnNO = null;
	private ImageLoader _Loader = null;
	private TextView _tvLogin = null;
	
	
	private String _Code = "";
	private String _Phone = "";
	private User _User = null;
	
	private boolean _Received;
	private CommunicationSocket _Socket;	
	
	
	public FragmentRestore(ActivityMain pActivity, SparseArray<String> pStore)
	{
		super(pActivity, pStore);
		_Loader = new ImageLoader(__GetMainActivity());
	}

	@Override
	protected View __OnCreateView(LayoutInflater pInflater, ViewGroup pContainer, SparseArray<String> pStore)
	{
		_Code = pStore.get(CODE);
		_Phone = pStore.get(PHONE);
		
		View view = pInflater.inflate(R.layout.restore, pContainer, false);
		
		view.findViewById(R.id.ivRestoreBack).setOnClickListener(this);
		
		_btnYES = (Button) view.findViewById(R.id.btnRestoreYES);
		_btnYES.setOnClickListener(this);
		
		_btnNO = (Button) view.findViewById(R.id.btnRestoreNO);
		_btnNO.setOnClickListener(this);
		
		_llUser = (LinearLayout) view.findViewById(R.id.llRestoreUser);
		_llUser.setVisibility(View.GONE);

		_pb = (ProgressBar) view.findViewById(R.id.pbRestore);
		_pb.setVisibility(View.VISIBLE);
		
		_tvLogin = (TextView) view.findViewById(R.id.tvRestoreLogin);

		_Socket = new CommunicationSocket(this);
		_Socket.Connect("User/?id=" + _Code + _Phone);
		_Received = false;
		
		return view;
	}
	@Override
	protected void __OnDestroy()
	{
		_Socket.Disconnect();
		_Socket = null;
	}

	
	@Override
	public void onClick(View pView)
	{
		switch (pView.getId())
		{
			case R.id.ivRestoreBack:
				__GetMainActivity().GetManager().GoToBack();
				break;
			case R.id.btnRestoreYES:
				if (_User != null && _User.HasImage())
				{
					__GetMainActivity().GetProfile().SetRecovery(__GetMainActivity(), _User);
					__GetMainActivity().GetManager().GoToProfile(true);
				}
				else
				{
					__GetMainActivity().OpenDialog(1, __GetMainActivity().getString(R.string.Dialog19Title), __GetMainActivity().getString(R.string.Dialog19Description));
				}
				break;
			case R.id.btnRestoreNO:
				__GetMainActivity().GetProfile().SetIDandUUID();
				__GetMainActivity().GetManager().GoToWelcome();
				break;
		}
	}

	@Override
	public void OnConnect()
	{
		if (!_Received)
		{
			try
			{
				long id = Long.parseLong(_Code + _Phone);
				JSONObject options = new JSONObject();
				options.put("userid", id);
				options.put("locale", __GetMainActivity().GetProfile().GetLocale());
				_Socket.Send("user.info", options);
			}
			catch (JSONException e)
			{
			}
		}
	}
	
	@Override
	public void OnMessageSendError(MessageItem pItem, boolean pTimeOut)
	{
		if (pItem.GetAction().equals("user.info"))
		{
			__GetMainActivity().OpenDialog(2, __GetMainActivity().getString(R.string.Dialog1Title), __GetMainActivity().getString(R.string.Dialog1Description));
		}
	}
	@Override
	public void OnMessageSendSuccess(MessageItem pItem)
	{
	}
	@Override
	public void OnMessageReceive(String pMID, String pAction, int pStatus, String pResult, int pType, JSONObject pOptions)
	{
		if (pAction.equals("user.info") && pStatus == 1000)
		{
			try
			{
				_User = new User(new JSONObject(pResult));
				if (_User.GetFullName().trim().length() == 0)
				{
					getView().findViewById(R.id.tvRestoreName).setVisibility(View.GONE);
				}
				else
				{
					getView().findViewById(R.id.tvRestoreName).setVisibility(View.VISIBLE);
					((TextView) getView().findViewById(R.id.tvRestoreName)).setText(_User.GetFullName());
				}
				_tvLogin.setText(_User.Login);
				((TextView) getView().findViewById(R.id.tvRestorePhone)).setText(_User.GetPhone(__GetMainActivity().GetProfile().GetLocale()));
				_Loader.Load(((ImageView) getView().findViewById(R.id.ivRestoreAvatar)), _User);
				
				_pb.setVisibility(View.GONE);
				_llUser.setVisibility(View.VISIBLE);
				_btnYES.setEnabled(true);
				_btnNO.setEnabled(true);
			}
			catch (JSONException e)
			{
			}
		}
	}
}