package entg.test.plugin.oats.util;


public abstract interface IJavaAgentWrapper
{
  public abstract ScriptResult run(RunParameters paramRunParameters);
  
  public abstract ScriptResult run(RunParameters paramRunParameters, long paramLong);
  
  public abstract void stop(boolean paramBoolean, long paramLong);
}
