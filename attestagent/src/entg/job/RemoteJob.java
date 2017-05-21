package entg.job;


/**
 * 
 * @author ravi
 *
 */
public class RemoteJob {
	
	private String Id;
	private RemoteJobAttributes attributes;
	private String AttestV2__Test_Run__c;
	private String AttestV2__Job_Manager__c;

	
	public String getId() {
		return Id;
	}
	public void setId(String id) {
		Id = id;
	}
	public RemoteJobAttributes getAttributes() {
		return attributes;
	}
	public void setAttributes(RemoteJobAttributes attributes) {
		this.attributes = attributes;
	}
	public String getAttestV2__Test_Run__c() {
		return AttestV2__Test_Run__c;
	}
	public void setAttestV2__Test_Run__c(String attestV2__Test_Run__c) {
		AttestV2__Test_Run__c = attestV2__Test_Run__c;
	}
	public String getAttestV2__Job_Manager__c() {
		return AttestV2__Job_Manager__c;
	}
	public void setAttestV2__Job_Manager__c(String attestV2__Job_Manager__c) {
		AttestV2__Job_Manager__c = attestV2__Job_Manager__c;
	}



}
