package entg.test;

public class SFDCTestMain {
	
	public static void main(String args[]) throws Exception {
		 
		TCAgentMain.loadProps();
		
	  System.out.println(UpdateSFDCStatus.getPasswordFromSfdc("a07G000000jwtiDIAQ","database_sfdc_id"));	
		
	}

}
