 package entg.test;

import java.util.Vector;

public class FunctionInfo {
   private String  functionName;
   private String functionScript;
   private String functionType;
   private String locationName;
   public boolean isSignon=false;
   
   Vector functionParams;
   
   
   public String getFunctionType() {
	return functionType;
}


public void setFunctionType(String functionType) {
	this.functionType = functionType;
}


public Vector getFunctionParams() {
	return this.functionParams;
}
public FunctionInfo() {
		functionParams = new Vector();
	}
	
	public void addFunctionParam(FunctionParamInfo p) {
		functionParams.add(p);
	}

	
	
	public void setFunctionName(String newfunction){
		functionName=newfunction;
	}
	public String getFunctionName(){
		return functionName;
	}
	
	public void setFunctionScript(String newscript){
		functionScript=newscript;
	}
	public String  getFunctionScript(){
		return functionScript;
	}


	public String getLocation() {
		return locationName;
	}


	public void setLocation(String locationName) {
		this.locationName = locationName;
	}
}  //  @jve:decl-index=0:visual-constraint="148,37"
