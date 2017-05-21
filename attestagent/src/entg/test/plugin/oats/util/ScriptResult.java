package entg.test.plugin.oats.util;

import java.io.File;

public class ScriptResult
{
  ResultType m_overallResult;
  long m_overallDuration;
  File csvReportFile;
  
  public static enum ResultType
  {
    Passed,  Failed,  Warning,  Stopped,  ForcefullyStopped;
  }
  
  public ResultType getOverallResult()
  {
    return this.m_overallResult;
  }
  
  public void setOverallResult(ResultType overallResult)
  {
    this.m_overallResult = overallResult;
  }
  
  public long getOverallDuration()
  {
    return this.m_overallDuration;
  }
  
  public void setOverallDuration(long overallDuration)
  {
    this.m_overallDuration = overallDuration;
  }
  
  public File getCsvReportFile()
  {
    return this.csvReportFile;
  }
  
  public void setCsvReportFile(File csvReportFile)
  {
    this.csvReportFile = csvReportFile;
  }
}
