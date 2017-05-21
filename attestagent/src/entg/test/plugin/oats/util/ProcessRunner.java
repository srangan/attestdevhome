package entg.test.plugin.oats.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProcessRunner
{
  public static String HOME_DIR = "HOME_DIR";
  private static final Logger m_logger = Logger.getLogger(ProcessRunner.class.getName());
  
  public static void exec(String command)
    throws IOException
  {
    command = format(command);
    
    Process pro = Runtime.getRuntime().exec(command);
    if (pro != null) {
      pro.exitValue();
    }
  }
  
  private static String format(String command)
  {
    ArrayList<String> properties = new ArrayList();
    

    Pattern p1 = Pattern.compile("\\$\\{[^\\$]+\\}");
    Matcher m1 = p1.matcher(command);
    while (m1.find())
    {
      String tag = m1.group();
      
      properties.add(tag.substring(2, tag.length() - 1));
    }
    for (String property : properties)
    {
      String value = null;
      if (property.equals(HOME_DIR)) {
        value = System.getProperty(property);
      }
      if (value != null) {
        command = command.replaceAll("${" + property + "}", Matcher.quoteReplacement(value));
      }
    }
    return command;
  }
  
  public static Process execWinCmd(String command, String[] args)
    throws IOException
  {
    try
    {
      String osName = System.getProperty("os.name");
      int iArgs = args == null ? 0 : args.length;
      
      String[] cmd = new String[3 + iArgs];
      if (osName.equals("Windows NT"))
      {
        cmd[0] = "cmd.exe";
        cmd[1] = "/C";
      }
      else if (osName.equals("Windows 95"))
      {
        cmd[0] = "command.com";
        cmd[1] = "/C";
      }
      else
      {
        cmd[0] = "cmd.exe";
        cmd[1] = "/C";
      }
      cmd[2] = command;
      for (int i = 3; i < cmd.length; i++) {
        cmd[i] = args[(i - 3)];
      }
      Runtime rt = Runtime.getRuntime();
      m_logger.info("Execing " + cmd[0] + " " + cmd[1] + " " + cmd[2]);
      Process proc = rt.exec(cmd);
      

      StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");
      

      StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT");
      

      errorGobbler.start();
      outputGobbler.start();
      
      return proc;
    }
    catch (Exception e)
    {
      m_logger.error(e);
    }
    return null;
  }
  
  static class StreamGobbler
    extends Thread
  {
    InputStream is;
    String type;
    
    StreamGobbler(InputStream is, String type)
    {
      this.is = is;
      this.type = type;
    }
    
    public void run()
    {
      try
      {
        InputStreamReader isr = new InputStreamReader(this.is);
        BufferedReader br = new BufferedReader(isr);
        String line = null;
        while ((line = br.readLine()) != null) {
          ProcessRunner.m_logger.info(this.type + ">" + line);
        }
      }
      catch (IOException ioe)
      {
        ProcessRunner.m_logger.error(ioe);
      }
    }
  }
}
