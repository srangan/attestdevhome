package entg.test.plugin.oats.util;

public class RunParameters
{
  String m_scriptJWGFile;
  String m_resultFolder;
  String m_additionalArgs;
  
  public String getScriptJWGFile()
  {
    return this.m_scriptJWGFile;
  }
  
  public void setScriptJWGFile(String scriptJWGFile)
  {
    this.m_scriptJWGFile = scriptJWGFile;
  }
  
  public String getResultFolder()
  {
    return this.m_resultFolder;
  }
  
  public void setResultFolder(String resultFolder)
  {
    this.m_resultFolder = resultFolder;
  }
  
  public String getAdditionalArgs()
  {
    return this.m_additionalArgs;
  }
  
  public void setAdditionalArgs(String additionalArgs)
  {
    this.m_additionalArgs = additionalArgs;
  }
}
