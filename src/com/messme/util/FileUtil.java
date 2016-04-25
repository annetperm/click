package com.messme.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class FileUtil
{
	
//	public static void CopyFileToAttachment(File pSourceFile, String pNewFilePath) throws IOException
//	{
//	    InputStream in = new FileInputStream(pSourceFile);
//	    OutputStream out = new FileOutputStream(pNewFilePath);
//
//	    byte[] buf = new byte[1024];
//	    int len;
//	    while ((len = in.read(buf)) > 0) 
//	    {
//	        out.write(buf, 0, len);
//	    }
//	    
//	    in.close();
//	    out.close();
//	}
	
	public static File CreateFile(String pFilePath, String pText) throws IOException
	{
		File file = new File(pFilePath);
    	FileOutputStream f = new FileOutputStream(file);
        PrintWriter pw = new PrintWriter(f);
        pw.println(pText);
        pw.flush();
        pw.close();
        f.close();
	    return file;
	}
	public static String ReadFile(String pFilePath) throws IOException
	{
		File file = new File(pFilePath);
		FileInputStream fin = new FileInputStream(file);
		BufferedReader reader = new BufferedReader(new InputStreamReader(fin));
	    StringBuilder sb = new StringBuilder();
	    String line = null;
	    while ((line = reader.readLine()) != null) 
	    {
	      sb.append(line).append("\n");
	    }
	    reader.close();
	    String result = sb.toString();
	    fin.close(); 
	    return result;
	}
}
