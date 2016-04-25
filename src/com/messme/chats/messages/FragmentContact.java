package com.messme.chats.messages;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.messme.ActivityMain;
import com.messme.R;
import com.messme.view.MessmeFragment;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import ezvcard.Ezvcard;
import ezvcard.VCard;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class FragmentContact extends MessmeFragment implements OnClickListener
{
	private File _vCard = null;
	
	
	public FragmentContact(ActivityMain pActivity, SparseArray<String> pStore)
	{
		super(pActivity, pStore);
	}

	@Override
	protected View __OnCreateView(LayoutInflater pInflater, ViewGroup pContainer, SparseArray<String> pStore)
	{
		String filepath = pStore.get(CONTACT_NAME);
		_vCard = new File(filepath);
		
		View view = pInflater.inflate(R.layout.contact, pContainer, false);

		view.findViewById(R.id.ivContactBack).setOnClickListener(this);
		
		if (_vCard.exists())
		{
			view.findViewById(R.id.llContactAdd).setOnClickListener(this);
			view.findViewById(R.id.llContact).setVisibility(View.VISIBLE);
			view.findViewById(R.id.llContactAdd).setVisibility(View.VISIBLE);
			view.findViewById(R.id.tvContactError).setVisibility(View.GONE);
			
			try
			{
				List<VCard> vcards = Ezvcard.parse(_vCard).all();
		        for (VCard vcard : vcards)
		        {
		        	((TextView) view.findViewById(R.id.tvContactName)).setText(vcard.getFormattedName().getValue());
		        	
		        	if (vcard.getTelephoneNumbers() != null && vcard.getTelephoneNumbers().size() != 0)
		    		{
			        	((TextView) view.findViewById(R.id.tvContactPhone)).setText(vcard.getTelephoneNumbers().get(0).getText());
		    		}
		        	
		    		if (vcard.getPhotos() != null && vcard.getPhotos().size() != 0)
		    		{
		    			byte[] bytes = vcard.getPhotos().get(0).getData();
		    			((ImageView) view.findViewById(R.id.ivContactAvatar)).setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
		    		}
		        	break;
		        }
			}
			catch (IOException e)
			{
			}
		}
		else
		{
			view.findViewById(R.id.llContact).setVisibility(View.GONE);
			view.findViewById(R.id.llContactAdd).setVisibility(View.GONE);
			view.findViewById(R.id.tvContactError).setVisibility(View.VISIBLE);
		}
		
		return view;
	}

	@Override
	public void onClick(View pView)
	{
		switch (pView.getId())
		{
			case R.id.ivContactBack:
				__GetMainActivity().GetManager().GoToBack();
				break;
			case R.id.llContactAdd:
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(_vCard),"text/x-vcard"); 
				startActivity(intent);
				break;
		}
	}
}