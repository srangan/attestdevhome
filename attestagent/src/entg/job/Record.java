package entg.job;

import java.util.HashMap;
import java.util.Map;

public class Record {
	
	private String Id;
	private String AttestV2__Test_Case__c;
	private Map<String, String> attributes = new HashMap<String, String>();
	public String getId() {
		return Id;
	}
	public void setId(String id) {
		Id = id;
	}
	public String getAttestV2__Test_Case__c() {
		return AttestV2__Test_Case__c;
	}
	public void setAttestV2__Test_Case__c(String attestV2__Test_Case__c) {
		AttestV2__Test_Case__c = attestV2__Test_Case__c;
	}
	public Map<String, String> getAttributes() {
		return attributes;
	}
	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}

	

}
