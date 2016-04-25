package com.messme.contacts;


public class Contact implements Comparable<Contact>
{
	public String ID;
	public String Name;
	public String NameLowerCase;
	public String Group = " ";
	public long Phone;
	
	
	public Contact(String pID, String pName, long pPhone)
	{
		ID = pID;
		Name = pName;
		if (Name == null)
		{
			Name = "";
		}
		NameLowerCase = Name.toLowerCase();
		
		if (Name.length() > 0)
		{
			Group = Name.substring(0, 1).toUpperCase();
		}
		else
		{
			Group = " ";
		}
		
		Phone = pPhone;
	}
	
	@Override
	public int compareTo(Contact pAnother)
	{
		return NameLowerCase.compareTo(pAnother.NameLowerCase);
	}

	public boolean Update(Contact pValue)
	{
		if (Name.equals(pValue.Name))
		{
			return false;
		}
		else
		{
			Name = pValue.Name;
			NameLowerCase = Name.toLowerCase();
			
			if (Name.length() > 0)
			{
				Group = Name.substring(0, 1).toUpperCase();
			}
			else
			{
				Group = " ";
			}
			return true;
		}
	}
}