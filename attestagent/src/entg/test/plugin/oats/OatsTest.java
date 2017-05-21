package entg.test.plugin.oats;

 import entg.test.plugin.oats.util.JavaAgentWrapper;
import entg.test.plugin.oats.util.RunParameters;
import entg.test.plugin.oats.util.ScriptResult;

public class OatsTest {
     private String m_jwgFile;
     private JavaAgentWrapper m_qos;
     private ScriptResult m_result;
     private RunParameters m_param;

     private static final String RESULT_FOLDER = "C:\\users\\sushma\\attestoats\\results\\GoogleSearch4";
     
     // the JavaAgent playback process is supposed to finish
     // within this duration specified by users.
     private static final long JAGENT_TIMEOUT = 90000L;

     public OatsTest() {

    	 m_qos = new JavaAgentWrapper( "C:\\OracleATS\\openScript\\runScript.bat" );

          m_jwgFile = getJWGFile();
          m_param = new RunParameters();
          m_param.setScriptJWGFile(m_jwgFile);
          m_param.setAdditionalArgs("-browser.type Firefox");
          m_param.setResultFolder(RESULT_FOLDER);
     }

     private String getJWGFile() {
          return "C:\\users\\Sushma\\attestoats\\GoogleSearch4\\GoogleSearch4.jwg";
     }

     public void runJAWrapperWithTimeout() {
          m_result = m_qos.run(m_param, JAGENT_TIMEOUT);
          if (m_result == null) {
               System.out.println("There are errors when running the script.");
               return;
          }
          System.out.println("Overall Result: " + m_result.getOverallResult());
          System.out.println("Overall Duration: " + m_result.getOverallDuration()
                    + "ms");
          System.out
                    .println("Result Report File: " + m_result.getCsvReportFile());
     }

     public static void main(String[] args) {

    	 OatsTest jaws = new OatsTest();
          jaws.runJAWrapperWithTimeout();
     }
}