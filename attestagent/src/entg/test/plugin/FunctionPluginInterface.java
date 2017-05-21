package entg.test.plugin;

import entg.test.*;

public interface FunctionPluginInterface {
   

   public void startTestcase(TestcaseInfo testcase);
   public String doSignon(AppSignInfo appsign, int funcSeq) throws Exception;
   
   public String runFunction(FunctionInfo func, int funcSeq) throws Exception;
   
   public void doCleanup() throws Exception ;
   
   public String getType();
}
