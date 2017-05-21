package entg.test.plugin.oats.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import entg.test.plugin.oats.util.ProcessRunner;
import entg.test.plugin.oats.util.ResultReportUtil;
// import oracle.oats.utilities.OpenScriptInstallation;
  
public class JavaAgentWrapper
  implements IJavaAgentWrapper
{
  private static Logger g_logger = Logger.getLogger(JavaAgentWrapper.class.getName());
  public static int DEFAULT_JA_PROCESS_TIMEOUT = 900000;
  private static final String RESULT_REPORT_FOLDER = "-resultReportFolder";
  private static final String RESULT_REPORT_FOLDER_NAME = "result";
  private static final String CSV_REPORT_FILE_NAME = "generalReport.csv";
  private String m_runScriptFile;
  private Process m_process = null;
  private String m_resultFolder;
  private boolean m_bTerminated;
  private boolean m_bStopped;
  private ScriptResult m_result = null;
  
  public JavaAgentWrapper()
  {
    setRunScriptFile(loadPathOfRunScript());
  }
  
  public JavaAgentWrapper(String runScriptFile)
  {
    setRunScriptFile(runScriptFile);
  }
  
  private String loadPathOfRunScript()
  {
    return "C:\\OracleATS\\openScript\\plugins" + File.separator + "runScript.bat";
  }
  
  public String getRunScriptFile()
  {
    return this.m_runScriptFile;
  }
  
  public void setRunScriptFile(String runScriptFile)
  {
    this.m_runScriptFile = runScriptFile;
  }
  
  public ScriptResult run(RunParameters param)
  {
    String jagentCmd = null;
    if (this.m_runScriptFile == null)
    {
      g_logger.error("The path to runScript.bat is not set.");
      return null;
    }
    if (!new File(this.m_runScriptFile).exists())
    {
      g_logger.error("runScript.bat can't be found at " + this.m_runScriptFile);
      return null;
    }
    jagentCmd = constructCommandLine(param);
    if (jagentCmd == null) {
      return null;
    }
    try
    {
      this.m_bTerminated = false;
      this.m_bStopped = false;
      this.m_process = ProcessRunner.execWinCmd(jagentCmd, null);
    }
    catch (IOException e)
    {
      g_logger.error("Unable to create the Java Agent process.", e);
      return null;
    }
    ScriptResult result = null;
    try
    {
      this.m_process.waitFor();
      if (this.m_bTerminated)
      {
        result = new ScriptResult();
        result.setOverallResult(ScriptResult.ResultType.ForcefullyStopped);
      }
      else if (this.m_bStopped)
      {
        result = ResultReportUtil.constructExecutionResult(this.m_resultFolder, "generalReport.csv");
        if (result != null) {
          result.setOverallResult(ScriptResult.ResultType.Stopped);
        }
      }
      else
      { 
        result = ResultReportUtil.constructExecutionResult(this.m_resultFolder, "generalReport.csv");
      }
    }
    catch (InterruptedException e)
    {
      g_logger.error(e);
      return null;
    }
    return result;
  }
  
  public void stop(boolean force, long timeout)
  {
    if (this.m_process == null) {
      return;
    }
    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(this.m_process.getOutputStream()));
    try
    {
      if (force)
      {
        this.m_bTerminated = true;
        bw.write("exit");
      }
      else
      {
        this.m_bStopped = true;
        bw.write("abort");
      }
      bw.newLine();
      bw.flush();
      
      Thread.sleep(timeout); return;
    }
    catch (IOException e)
    {
      g_logger.error("Unable to send command to the Java Agent process to terminate it.", e);
      terminateJavaAgent();
      try
      {
        bw.close();
      }
      catch (IOException e1)
      {
        g_logger.error(e1);
      }
    }
    catch (InterruptedException e)
    {
      g_logger.error(e);
      terminateJavaAgent();
      try
      {
        bw.close();
      }
      catch (IOException e1)
      {
        g_logger.error(e1);
      }
    }
    finally
    {
      try
      {
        bw.close();
      }
      catch (IOException e)
      {
        g_logger.error(e);
      }
    }
  }
  
  private void terminateJavaAgent()
  {
    this.m_bTerminated = true;
    this.m_process.destroy();
  }
  
  private String constructCommandLine(RunParameters param)
  {
    StringBuilder sb = new StringBuilder();
    
    sb.append(this.m_runScriptFile);
    String jwgFile = param.getScriptJWGFile();
    if (jwgFile == null)
    {
      g_logger.error("JWG file is not specified.");
      return null;
    }
    sb.append(" ").append(param.getScriptJWGFile());
    if (param.getResultFolder() != null)
    {
      this.m_resultFolder = param.getResultFolder();
    }
    else
    {
      this.m_resultFolder = (new File(jwgFile).getParent() + File.separator + "result");
      g_logger.info("resultFolder is not set. Use the default.");
    }
    sb.append(" ").append("-resultReportFolder").append(" ").append(this.m_resultFolder);
    
    File folder = new File(this.m_resultFolder);
    if (!folder.exists()) {
      folder.mkdirs();
    }
    if (param.getAdditionalArgs() != null) {
      sb.append(" ").append(param.getAdditionalArgs());
    } else {
      printUsage();
    }
    return sb.toString();
  }
  
  public ScriptResult run(RunParameters param, long timeout)
  {
    final JavaAgentWrapper wrapper = this;
    final RunParameters p = param;
    

    Thread thread = new Thread()
    {
      public void run()
      {
        JavaAgentWrapper.this.m_result = wrapper.run(p);
      }
    };
    thread.start();
    try
    {
      thread.join(timeout);
      if (thread.isAlive())
      {
        thread.join();
      }
    }
    catch (InterruptedException e)
    {
      g_logger.error(e);
      return null;
    }
    return this.m_result;
  }
  
  private static void printUsage()
  {
    g_logger.info("Additional arguments for running JavaAgent Wrapper: \n\t-iterations n\n\t\tRun n iterations of the script\n\t-dboptions alias:index:mode,...\n\t\tSpecify which databank records to use when playing back the script\n\t-logLocalVUDisplay\n\t\tCreate VUDisplay.txt and VUDisplay.csv output files in the jagent folder\n\t-iterationDelay n\n\t\tPause for n seconds between iterations\n\t-delayPercentage mode\n\t\tSpecify how long to delay between steps in a script\n");
  }
}
