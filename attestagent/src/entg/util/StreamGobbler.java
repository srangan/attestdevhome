package entg.util;

import java.util.*;
import java.io.*;

public class StreamGobbler extends Thread
{
    InputStream is;
    String type;
    PrintWriter out;
     
    public StreamGobbler(InputStream is, String type)
    {
        this(is, type,null);
    }

    public StreamGobbler(InputStream is, String type, PrintWriter redirect)
    {
        this.is = is;
        this.type = type;
        this.out = redirect;
    }
     
    public void run()
    {
        try
        {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line=null;
            while ( (line = br.readLine()) != null)
            {
            	if (out==null)
            	  System.out.println(type + ">" + line);	
            	else 
                  out.println(type + ">" + line);    
            }
            if (out != null)
            	out.flush();
        } catch (IOException ioe)
            {
            ioe.printStackTrace();  
            }
    }
}