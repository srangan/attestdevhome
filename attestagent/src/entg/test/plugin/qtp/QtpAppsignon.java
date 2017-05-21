package entg.test.plugin.qtp;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import entg.test.*;


import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.hssf.record.*;
import org.apache.poi.hssf.model.*;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.*;

import java.util.*;

public class QtpAppsignon {

	AppSignInfo appsign;
	
	String xlsFilename;
	
	public QtpAppsignon(AppSignInfo appsign) {
		this.appsign = appsign;
	}
	
	public void generateXls (String dir,int funcSeq) throws Exception {
		
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("Login");
		
		HSSFCellStyle cellStyle = wb.createCellStyle();
        cellStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("text"));

        Vector dataV = appsign.getDataVect();  
        
        HSSFRow row = sheet.createRow((short) 0);
        HSSFRow row1 = sheet.createRow((short) 1);
        for (int c=0;c<dataV.size();c++) {
          
          String str = (String) dataV.elementAt(c);
		  StringTokenizer st = new StringTokenizer(str, "=");

		  String key = st.nextToken();
		  String val = "";

  		  try {
				val = str.substring((key.length() + 1));
		  } catch (Exception ex) {
		  }
          
          HSSFCell cell = row.createCell((short) c);
          HSSFCell cell1 = row1.createCell((short) c);
          cell.setCellType(HSSFCell.CELL_TYPE_STRING);
          cell1.setCellType(HSSFCell.CELL_TYPE_STRING);
          cell.setCellValue(key);
          cell1.setCellValue(val);
        }
        
        setXlsFilename (appsign.getAppSignOnScript().replace(' ', '_').replace('\\', '_').replace('/', '_')+"_"+funcSeq+".xls");
        		
		FileOutputStream fileOut = new FileOutputStream(dir + 
					File.separator
					+ getXlsFilename());
		wb.write(fileOut);

		fileOut.close();
		
		
        
	}

	public String getXlsFilename() {
		return xlsFilename;
	}

	public void setXlsFilename(String xlsFilename) {
		this.xlsFilename = xlsFilename;
	}
	
	
}
