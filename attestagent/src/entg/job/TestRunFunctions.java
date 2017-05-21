package entg.job;

import java.util.List;
import java.util.ArrayList;

public class TestRunFunctions {
	
	private int totalSize;
	private boolean done;
	private List<Record> records = new ArrayList<Record>();
	public int getTotalSize() {
		return totalSize;
	}
	public void setTotalSize(int totalSize) {
		this.totalSize = totalSize;
	}
	public boolean isDone() {
		return done;
	}
	public void setDone(boolean done) {
		this.done = done;
	}
	public List<Record> getRecords() {
		return records;
	}
	public void setRecords(List<Record> records) {
		this.records = records;
	}
	

	

}
