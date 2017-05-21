package entg.test.plugin.oats.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import entg.test.plugin.oats.util.ScriptResult;
import entg.test.plugin.oats.util.ScriptResult.ResultType;
import org.apache.log4j.Logger;

public class ResultReportUtil
{
  private static Logger g_logger = Logger.getLogger(ResultReportUtil.class.getName());
  private static final String SESSION = "Session";
  private static final int INDEX_REPORT_DURATION = 0;
  private static final int INDEX_REPORT_RESULT = 4;
  private static final String BasicReporter_Passed = "Passed";
  private static final String BasicReporter_Warning = "Warning";
  private static final String BasicReporter_Failed = "Failed";
  
  public static ScriptResult constructExecutionResult(String resultFolder, String reportFilename)
  {
    File resultFile = new File(resultFolder + File.separator + getSessionFolderName(resultFolder) + File.separator + reportFilename);
    if (!resultFile.exists()) {
      return null;
    }
    String errMsg = "Failed when reading result report file " + resultFile.getAbsolutePath();
    ScriptResult result = new ScriptResult();
    result.setCsvReportFile(resultFile);
    String line = "";
    try
    {
      BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(resultFile)));
      line = reader.readLine();
      if (line == null)
      {
        g_logger.error(errMsg);
        return null;
      }
      String[] elems = line.split(",");
      
      result.setOverallDuration(Long.parseLong(elems[0]));
      
      String resultStatus = elems[4];
      if (resultStatus.equalsIgnoreCase("Passed")) {
        result.setOverallResult(ScriptResult.ResultType.Passed);
      } else if (resultStatus.equalsIgnoreCase("Warning")) {
        result.setOverallResult(ScriptResult.ResultType.Warning);
      } else if (resultStatus.equalsIgnoreCase("Failed")) {
        result.setOverallResult(ScriptResult.ResultType.Failed);
      }
    }
    catch (FileNotFoundException e)
    {
      g_logger.error(errMsg, e);
      return null;
    }
    catch (IOException e)
    {
      g_logger.error(errMsg, e);
      return null;
    }
    catch (NumberFormatException e)
    {
      g_logger.error(errMsg, e);
      return null;
    }
    catch (IndexOutOfBoundsException e)
    {
      g_logger.error(errMsg, e);
      return null;
    }
    return result;
  }
  
  private static String getSessionFolderName(String outputFolder)
  {
    return "Session" + getMaxSessionId(outputFolder);
  }
  
  private static int getMaxSessionId(String outputFolder)
  {
    File resultsFolder = new File(outputFolder);
    File[] folders = resultsFolder.listFiles();
    int maxSessionId = 0;
    for (File folder : folders) {
      if ((folder.isDirectory()) && (folder.getName().startsWith("Session"))) {
        try
        {
          int sessionId = Integer.parseInt(folder.getName().substring("Session".length()));
          maxSessionId = sessionId > maxSessionId ? sessionId : maxSessionId;
        }
        catch (Exception localException) {}
      }
    }
    return maxSessionId;
  }
}
