package entg.test.plugin.qtp;

import java.io.*;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;

import java.util.zip.*;
import java.util.*;

import entg.test.TCAgentMain;
import entg.test.TestcaseInfo;
import entg.test.FunctionInfo;
import entg.test.AppSignInfo;

public class QtpTestcase {

	private String xlsFilename;

	AppSignInfo appsign;

	TestcaseInfo testcase;

	String dir; 

	public void setXlsDir(String dir) {
		this.dir = dir;
	}

	public QtpTestcase(TestcaseInfo tc) {
		testcase = tc;
	}
  
	public void setCurrentSignon(AppSignInfo appsign) {
		this.appsign = appsign;
	}

	public String getXlsFilename() {
		return xlsFilename;
	}

	public void setXlsFilename(String xlsFilename) {
		this.xlsFilename = xlsFilename;
	}

	public void overrideReadAndSetTestcaseXls(String logDir) {
		
		System.out.println("****** In Override ");
		String url = entg.test.TCAgentMain.TCProperties
				.getProperty("ATTEST_URL")
				+ "/testresults/"+testcase.getOverrideDataFile();
		
		
		try {
		org.apache.commons.httpclient.URI u = new org.apache.commons.httpclient.URI(
				  url,false);
		url = u.getEscapedURI();
		} catch (Exception ex) {}
			
		HttpClient httpClient = new HttpClient();
		

    	String proxyHost = TCAgentMain.TCProperties.getProperty("HTTP_PROXY_HOST");
    	if (proxyHost != null && !proxyHost.equals("")) {
    		int proxyPort = 80;
    		
    		String p = TCAgentMain.TCProperties.getProperty("HTTP_PROXY_PORT");
    		if (p!= null && !p.equals("")) 
    		   proxyPort = Integer.parseInt(p);
    		
    		httpClient.getHostConfiguration().setProxy(proxyHost, proxyPort);    	
    		httpClient.getParams().setParameter("http.useragent", "Test Client");    		
    	}
    			
		GetMethod getMethod = new GetMethod(url);
 
		try {
			File logDirHandle = new File(logDir);
			if (logDirHandle.isDirectory()) {
				throw new Exception("Dir already exists");
			} else
				logDirHandle.mkdir();
			File logDirHandleRep = new File(logDir + File.separator + "Report");
			if (logDirHandleRep.isDirectory()) {
				throw new Exception("Dir already exists");
			} else
				logDirHandleRep.mkdir();
			
			
			System.out.println("****** Retrieving override XLS. URL: " + url);
			int statusCode = httpClient.executeMethod(getMethod);
			InputStream in = getMethod.getResponseBodyAsStream();
			byte[] b = new byte[1024];
			int len;
			
			OutputStream out = new FileOutputStream(logDir + File.separator + "Report"+File.separator +"Default.xls");
			while ((len = in.read(b)) != -1) {
				// write byte to file
				out.write(b, 0, len);
			}
			out.close();
			in.close();
			System.out.println("****** DONE with override XLS retrieval.");
					
			
			this.readXlsAndSetData(logDir);
		} catch (HttpException e) {
			System.out.println("**** Httpexception " + e.getMessage());
		} catch (Exception e) {
			System.out.println("**** Exception " + e.getMessage());
		} finally {
			// release the connection
			getMethod.releaseConnection();
		}			
	}
	public void continueReadAndSetTestcaseXls(String logDir) {

		System.out.println("****** In Overwrite ");
		String url = entg.test.TCAgentMain.TCProperties
				.getProperty("ATTEST_URL")
				+ "/testresults/report_"
				+ testcase.getTestcaseName().toLowerCase()
				+ "_"
				+ testcase.getLastpassfunc() + ".qjar";
		System.out.println("Getting old report from "+url);
		// String url =
		// "http://yamuna.entegration.com:8180/testresults/report_test3091_1.qjar";

		HttpClient httpClient = new HttpClient();
		

    	String proxyHost = TCAgentMain.TCProperties.getProperty("HTTP_PROXY_HOST");
    	if (proxyHost != null && !proxyHost.equals("")) {
    		int proxyPort = 80;
    		
    		String p = TCAgentMain.TCProperties.getProperty("HTTP_PROXY_PORT");
    		if (p!= null && !p.equals("")) 
    		   proxyPort = Integer.parseInt(p);
    		
    		httpClient.getHostConfiguration().setProxy(proxyHost, proxyPort);    	
    		httpClient.getParams().setParameter("http.useragent", "Test Client");    		
    	}		
		
		GetMethod getMethod = new GetMethod(url);
		Enumeration entries;
		ZipFile zipFile;
		try {
			File logDirHandle = new File(logDir);
			if (logDirHandle.isDirectory()) {
				throw new Exception("Dir already exists");
			} else
				logDirHandle.mkdir();
			File logDirHandleRep = new File(logDir + File.separator + "Report");
			if (logDirHandleRep.isDirectory()) {
				throw new Exception("Dir already exists");
			} else
				logDirHandleRep.mkdir();

			System.out.println("****** Retrieving QJAR. URL: " + url);
			int statusCode = httpClient.executeMethod(getMethod);
			InputStream in = getMethod.getResponseBodyAsStream();
			byte[] b = new byte[1024];
			int len;
			String tempQjarFile = entg.test.TCAgentMain.getTmpDir() + "\\etc"
					+ System.currentTimeMillis() + ".qjar";
			OutputStream out = new FileOutputStream(tempQjarFile);
			while ((len = in.read(b)) != -1) {
				// write byte to file
				out.write(b, 0, len);
			}
			out.close();
			in.close();
			System.out.println("****** DONE with wjar retrieval."
					+ tempQjarFile);

			System.out.println("****** Saving default.xls from qJar");
			zipFile = new ZipFile(tempQjarFile);
			entries = zipFile.entries();
			System.out.println("zip open");

			while (entries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) entries.nextElement();
				System.out.println("Entry " + entry.getName());
				if (entry.getName().equals("Report/Default.xls")) {
					in = zipFile.getInputStream(entry);
					BufferedOutputStream out1 = new BufferedOutputStream(
							new FileOutputStream(logDir + File.separator
									+ "Report" + File.separator + "Default.xls"));
					byte[] buffer = new byte[1024];

					while ((len = in.read(buffer)) >= 0)
						out1.write(buffer, 0, len);

					in.close();
					out1.close();

				}
			}
			this.readXlsAndSetData(logDir);
		} catch (HttpException e) {
			System.out.println("**** Httpexception " + e.getMessage());
		} catch (Exception e) {
			System.out.println("**** Exception " + e.getMessage());
		} finally {
			// release the connection
			getMethod.releaseConnection();
		}
	}

	public void readXlsAndSetData(String logDir) throws Exception {
		System.out.println("***** In readAndSetXLS");
		POIFSFileSystem fs = null;
		try {
			System.out.println("Opening " + logDir + File.separator
					+ "Report\\Default.xls");
			fs = new POIFSFileSystem(new FileInputStream(logDir
					+ File.separator + "Report\\Default.xls"));
		} catch (Exception ex) {
			System.out.println("Exception while opening Default.xls - "+ex.getMessage());
			ex.printStackTrace();
			
			System.out.println("Opening Testcase.xls");
			fs = new POIFSFileSystem(new FileInputStream(dir + File.separator
					+ xlsFilename));
		}

		HSSFWorkbook wb = new HSSFWorkbook(fs);
		HSSFSheet sheet = wb.getSheet("Global");

		HSSFRow row = sheet.getRow((short) 0);
		HSSFRow row1 = sheet.getRow((short) 1);

		Vector dataV = testcase.getDataVect();
		dataV.removeAllElements();
		System.out.println("Clearing Data");
		int c = 0;
		while (true) {
			if (row.getCell((short) c) == null)
				break;
			String cellVal = row.getCell((short) c).getStringCellValue();
			String cell1Val = "";
			if (row1.getCell((short) (c)) == null) {
				cell1Val = "";
				c++;
			} else {
				System.out.println("Trying to get cell1 value");
				HSSFCell curCell = row1.getCell((short) c++);
				int curCellType = curCell.getCellType();

				if (curCellType == HSSFCell.CELL_TYPE_NUMERIC) {
					System.out.println("Numeric cell");
					if (HSSFDateUtil.isCellDateFormatted(curCell)) {
						System.out.println("This is a Date");
						Calendar cal = Calendar.getInstance();
						cal.setTime(HSSFDateUtil.getJavaDate(curCell
								.getNumericCellValue()));
						String value = (String.valueOf(cal.get(Calendar.YEAR)));
						value = cal.get(Calendar.MONTH) + 1 + "/"
								+ cal.get(Calendar.DAY_OF_MONTH) + "/" + value;
						cell1Val = value;
					} else {
						cell1Val = "" + (int) curCell.getNumericCellValue();
					}
				} else if (curCellType == HSSFCell.CELL_TYPE_STRING) {
					cell1Val = curCell.getStringCellValue();
					System.out.println("String cell");
				} else if (curCellType == HSSFCell.CELL_TYPE_FORMULA) {
					System.out.println("Formula Cell");
					try { 
					double icl = curCell.getNumericCellValue();
					if (!Double.isNaN(icl))
						cell1Val = Double.toString(icl);
					else
						cell1Val = curCell.getStringCellValue();
					} catch (IllegalStateException ex) {
						System.out.println("Formula cell with text data");
					   cell1Val = curCell.getStringCellValue();
					}
				} else {

					System.out.println("Some other cell");
				}
				System.out.println("Cell1Val = " + cell1Val);
			}

			if (cellVal != null
					&& (cellVal.equals("") || cellVal.equals("TESTCASE_ID")))
				break;
			System.out.println("Setting data - " + cellVal + "=" + cell1Val);
			dataV.addElement(cellVal + "=" + cell1Val);
		}
	    dataV.addElement("TESTCASE_ID="+testcase.getTestcaseId());
		System.out.println("**** After readAndSetXLS");
	}

	public void addLoginSheet(HSSFWorkbook wb) {
		HSSFSheet sheet = wb.createSheet("Login");

		HSSFCellStyle cellStyle = wb.createCellStyle();
		cellStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("text"));

		Vector dataV = appsign.getDataVect();

		HSSFRow row = sheet.createRow((short) 0);
		HSSFRow row1 = sheet.createRow((short) 1);
		for (int c = 0; c < dataV.size(); c++) {

			HSSFCell cell = row.createCell((short) c);
			HSSFCell cell1 = row1.createCell((short) c);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			cell1.setCellType(HSSFCell.CELL_TYPE_STRING);

			String str = (String) dataV.elementAt(c);
			StringTokenizer st = new StringTokenizer(str, "=");

			String key = st.nextToken();
			String val = "";

			try {
				val = str.substring((key.length() + 1));
			} catch (Exception ex) {
			}
			cell.setCellValue(key);
			if (st.hasMoreTokens())
				cell1.setCellValue(val);
		}

	}

	public void generateXls(String dir, FunctionInfo function, int funcSeq)
			throws Exception {

		System.out.println("*** In generate XLS "
				+ testcase.getTestcaseName().replace(' ', '_').replace('\\', '_').replace('/', '_') + "_" + funcSeq
				+ ".xls");

		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("Global");
		String sheetName = function.getFunctionName().replace(' ', '_');
		if (sheetName.length() > 30)
			sheetName = sheetName.substring(1, 30);
		HSSFSheet sheet1 = wb.createSheet(sheetName);
		addLoginSheet(wb);

		HSSFCellStyle cellStyle = wb.createCellStyle();
		cellStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("text"));

		Vector dataV = testcase.getDataVect();

		HSSFRow row = sheet.createRow((short) 0);
		HSSFRow row1 = sheet.createRow((short) 1);
		for (int c = 0; c < dataV.size(); c++) {

			HSSFCell cell = row.createCell((short) c);
			HSSFCell cell1 = row1.createCell((short) c);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			cell1.setCellType(HSSFCell.CELL_TYPE_STRING);

			String str = (String) dataV.elementAt(c);
			System.out.println("Writing data " + str);

			StringTokenizer st = new StringTokenizer(str, "=");

			String key = st.nextToken();
			String val = "";

			try {
				val = str.substring((key.length() + 1));
				if (val.equals("null"))
					val = "";
			} catch (Exception ex) {
			}

			cell.setCellValue(key);
			cell1.setCellValue(val);
		}

		setXlsDir(dir);
		setXlsFilename(testcase.getTestcaseName().replace(' ', '_').replace('\\', '_').replace('/', '_') + "_"
				+ funcSeq + ".xls");

		FileOutputStream fileOut = new FileOutputStream(dir + File.separator
				+ getXlsFilename());
		wb.write(fileOut);

		fileOut.close();
		System.out.println("*** After generateXLS");

	}
	

	public static void main(String[] args) throws Exception {
		POIFSFileSystem fs = null;
		try {
			fs = new POIFSFileSystem(new FileInputStream(args[0]));
		} catch (Exception ex) {
			System.out.println("Exception while opening Default.xls - "+ex.getMessage());
			ex.printStackTrace();
			
		}

		HSSFWorkbook wb = new HSSFWorkbook(fs);
		HSSFSheet sheet = wb.getSheet("Global");

		HSSFRow row = sheet.getRow((short) 0);
		HSSFRow row1 = sheet.getRow((short) 1);

		int c = 0;
		while (true) {
			if (row.getCell((short) c) == null)
				break;
			String cellVal = row.getCell((short) c).getStringCellValue();
			String cell1Val = "";
			if (row1.getCell((short) (c)) == null) {
				cell1Val = "";
				c++;
			} else {
				System.out.println("Trying to get cell1 value");
				HSSFCell curCell = row1.getCell((short) c++);
				int curCellType = curCell.getCellType();

				if (curCellType == HSSFCell.CELL_TYPE_NUMERIC) {
					System.out.println("Numeric cell");
					if (HSSFDateUtil.isCellDateFormatted(curCell)) {
						System.out.println("This is a Date");
						Calendar cal = Calendar.getInstance();
						cal.setTime(HSSFDateUtil.getJavaDate(curCell
								.getNumericCellValue()));
						String value = (String.valueOf(cal.get(Calendar.YEAR)));
						value = cal.get(Calendar.MONTH) + 1 + "/"
								+ cal.get(Calendar.DAY_OF_MONTH) + "/" + value;
						cell1Val = value;
					} else {
						cell1Val = "" + (int) curCell.getNumericCellValue();
					}
				} else if (curCellType == HSSFCell.CELL_TYPE_STRING) {
					cell1Val = curCell.getStringCellValue();
					System.out.println("Numeric cell");
				} else if (curCellType == HSSFCell.CELL_TYPE_FORMULA) {
					System.out.println("Formula Cell");
					try { 
					double icl = curCell.getNumericCellValue();
					if (!Double.isNaN(icl))
						cell1Val = Double.toString(icl);
					else
						cell1Val = curCell.getStringCellValue();
					} catch (IllegalStateException ex) {
						System.out.println("Formula cell with text data");
					   cell1Val = curCell.getStringCellValue();
					}
				} else {

					System.out.println("Some other cell");
				}
				System.out.println("Cell1Val = " + cell1Val);
			}

			if (cellVal != null
					&& (cellVal.equals("") || cellVal.equals("TESTCASE_ID")))
				break;
			System.out.println("Setting data - " + cellVal + "=" + cell1Val);
		}
		System.out.println("**** After readAndSetXLS");
	}
}
